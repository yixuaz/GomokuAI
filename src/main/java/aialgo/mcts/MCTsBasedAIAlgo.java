package aialgo.mcts;

import aialgo.IAIAlgo;
import chessboardalgo.IChessboardAIAlgo;
import common.InputValidator;
import common.Position;
import lombok.Getter;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MCTsBasedAIAlgo implements IAIAlgo {
    protected double explorationParam = 1.4;
    @Getter
    protected IChessboardAIAlgo chessboardAlgo;
    @Getter
    protected IScoreManager scoreManager;
    @Getter
    protected int humanColor;
    protected int aiColor;
    protected int size;
    protected MCTSNode previousStep;

    public MCTsBasedAIAlgo(IChessboardAIAlgo chessboardAlgo, int humanColor) {
        InputValidator.checkColorValid(humanColor);
        this.chessboardAlgo = chessboardAlgo;
        this.scoreManager = new CachedScoreManager(chessboardAlgo);
        this.humanColor = humanColor;
        this.aiColor = 3 - humanColor;
        this.size = chessboardAlgo.getSize();
    }

    protected abstract MCTSNode aiFindPos(MCTSNode root);

    private MCTSNode aiFindPos(int rootPlayer, int step) {
        return aiFindPos(new MCTSNode(chessboardAlgo, rootPlayer, -1, -1, step));
    }

    @Override
    public Position aiFindPos() {
        Position pos = chessboardAlgo.getAllSteps().getLast();
        int rootPlayer = chessboardAlgo.getValInBoard(pos.x, pos.y);
        MCTSNode result = aiFindPos(rootPlayer, chessboardAlgo.steps());
        previousStep = result;
        if (result == null) return Position.EMPTY;
        printDebugInfo(result);
        return new Position(result.y, result.x);
    }

    protected abstract void printDebugInfo(MCTSNode result);

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        scoreManager.updateScore(y, x);
        if (previousStep != null && player == humanColor)
            previousStep = previousStep.findChildren(y, x);
    }

    protected void expand(MCTSNode node) {
        expand(node, false);
    }
    protected void expand(MCTSNode node, boolean importantOnly) {
        IChessboardAIAlgo board = node.getBoard();
        int nextPlayer = 3 - node.getPlayer();
        int size = board.getSize();
        List<Integer> res = new CachedScoreManager(board).generateCandidatePiece(nextPlayer, importantOnly);
        for (int i : res) {
            int y = i / size;
            int x = i % size;
            IChessboardAIAlgo childBoard = board.clone();
            childBoard.setPiece(x, y, nextPlayer);
            MCTSNode child = new MCTSNode(childBoard, nextPlayer, y, x, node.step + 1);
            child.setParent(node);
            node.getChildren().add(child);
        }
    }

    protected MCTSNode selectNode(MCTSNode node) {
        if (node.getWinner() != 0) return node;
        while (!node.getChildren().isEmpty()) {
            node = getNonVisitOrBestChild(node);
            if (node.getVisits() == 0 || node.getWinner() != 0) {
                return node;
            }
        }
        expand(node);
        return getNonVisitOrBestChild(node);
    }

    private MCTSNode getNonVisitOrBestChild(MCTSNode node) {
        var nonVisit = node.getChildren().stream().filter(y -> y.getVisits() == 0).collect(Collectors.toList());
        if (!nonVisit.isEmpty()) return nonVisit.get(0);
        return getBestChild(node);
    }

    protected MCTSNode getBestChild(MCTSNode node) {
        return node.getChildren().stream()
                .max(Comparator.comparingDouble(chd -> uctValue(node, chd)))
                .orElse(null);
    }

    protected double uctValue(MCTSNode parent, MCTSNode child) {
        assert child.getVisits() != 0;
        double winRate = (double) child.getWins() / (double) child.getVisits();
        return winRate  +
                explorationParam * Math.sqrt(Math.log(parent.getVisits()) / (double) child.getVisits());
    }

    protected abstract int simulate(MCTSNode node, int rootStep);

    protected void backpropagate(MCTSNode node, int winner) {
        while (node != null) {
            node.incrementVisits();
            if (node.getPlayer() == winner) {
                node.incrementWins();
            }
            node = node.getParent();
        }
    }

    protected MCTSNode getMostVisitedChildMove(MCTSNode node) {
        return node.getChildren().stream().max(Comparator.comparing(MCTSNode::getVisits)).orElse(null);
    }

    protected void doMCTS(MCTSNode root) {
        long st = System.currentTimeMillis();
        int i = 0;
        for (; true; i++) {
            if (getBestMoveEnd(i, root, st)) break;
            MCTSNode node = selectNode(root);
            int winner = node.getWinner();
            if (winner == 0)
                winner = simulate(node, root.step);
            backpropagate(node, winner);
        }
        if (i != 0)
            System.out.println("MCTS time cost: " + (System.currentTimeMillis() - st) + ",play count: " + i);
    }

    protected abstract boolean getBestMoveEnd(int i, MCTSNode root, long startTime);

}
