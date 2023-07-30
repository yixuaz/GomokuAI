package aialgo.mcts;

import chessboardalgo.IChessboardAIAlgo;
import common.Position;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;
import aialgo.vcx.VCX;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VCXBasedMCTsAlgo extends MCTsBasedAIAlgo {
    private int timeLimitMs = 10_000;
    private int simulationVCXDepth = 7;

    private int simCnt = 0;
    private int wholeVCXCnt = 0;
    private int vcxSuccessCnt = 0;
    private Random random;

    public VCXBasedMCTsAlgo(IChessboardAIAlgo chessboardAlgo, int humanColor, int timeLimitMs, int simulationVCXDepth) {
        super(chessboardAlgo, humanColor);
        this.timeLimitMs = timeLimitMs;
        this.simulationVCXDepth = simulationVCXDepth;
    }

    public VCXBasedMCTsAlgo(IChessboardAIAlgo chessboardAlgo, int humanColor) {
        super(chessboardAlgo, humanColor);
    }


    @Override
    protected int simulate(MCTSNode node, int rootStep) {
        simCnt++;
        IChessboardAIAlgo b = node.getBoard();
        int lastStepDonePlayer = node.getPlayer();
        IChessboardAIAlgo board = b.clone();
        if (board.isTerminal(node.x, node.y)) {
            node.setWinner(lastStepDonePlayer);
            return lastStepDonePlayer;
        }
        if (node.step > 10 && node.step > rootStep + 1) {
            wholeVCXCnt++;
            Position vcx = new VCX(board.clone(), lastStepDonePlayer,
                    node.step > rootStep + 3 ? simulationVCXDepth : simulationVCXDepth + 2).aiFindPos();

            if (vcx != Position.EMPTY) {
                node.setWinner(3 - lastStepDonePlayer);
                vcxSuccessCnt++;
                return 3 - lastStepDonePlayer;
            }
        }
        int nextPlayer = 3 - lastStepDonePlayer;
        IScoreManager tmpScoreManager = new CachedScoreManager(board);

        while (true) {
            List<Integer> legalMoves = tmpScoreManager.generateCandidatePiece(nextPlayer);
            if (legalMoves.isEmpty()) {
                return 0; // Draw
            }

            int randomMove = legalMoves.get(random.nextInt(Math.min(5, legalMoves.size())));
            int y = randomMove / size;
            int x = randomMove % size;

            board.setPiece(x, y, nextPlayer);

            tmpScoreManager.updateScore(y, x);

            if (board.isTerminal(x, y)) {
                return nextPlayer; // Winner
            }
            nextPlayer = 3 - nextPlayer;
        }
    }

    @Override
    protected boolean getBestMoveEnd(int i, MCTSNode root, long startTime) {
        if (root.getChildren().size() == 1) {
            return true;
        }
        if (root.getWinner() != 0) {
            return true;
        }
        if (i % 1000 == 0) {
            if (i > 10000) {
                if (gapBetweenMaxAndSecond(root.getChildren()) >= 4.0) return true;
            }
            if (System.currentTimeMillis() - startTime >= timeLimitMs) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected MCTSNode aiFindPos(MCTSNode root) {
        random = new Random(16);
        doMCTS(root);
        if (root.getWinner() != 0) {
            return root.getWinChildMove();
        }

        return getMostVisitedChildMove(root);
    }

    @Override
    protected void printDebugInfo(MCTSNode res) {
        System.out.println(wholeVCXCnt);
        System.out.println(vcxSuccessCnt);
        System.out.println("simCnt:" + simCnt);

        System.out.println("root: " + res.getParent());
        Collections.sort(res.getParent().getChildren(), (a, b) -> Integer.compare(b.getVisits(), a.getVisits()));
        for (var i : res.getParent().getChildren())
            System.out.println(i);
        int k = 0;
        while (res.getChildren() != null && !res.getChildren().isEmpty()) {
            res = getMostVisitedChildMove(res);
            System.out.println(k + "," + res);
            k++;
        }
    }

    private double gapBetweenMaxAndSecond(List<MCTSNode> children) {
        if (children.size() <= 1) return 1;
        int firstMax = Integer.MIN_VALUE;
        int secondMax = Integer.MIN_VALUE;

        for (MCTSNode node : children) {
            int num = node.getVisits();
            if (num > firstMax) {
                secondMax = firstMax;
                firstMax = num;
            } else if (num > secondMax) {
                secondMax = num;
            }
        }

        return (double) firstMax / secondMax;
    }
}
