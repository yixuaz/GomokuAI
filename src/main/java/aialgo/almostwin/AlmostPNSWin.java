package aialgo.almostwin;

import aialgo.IAIAlgo;
import aialgo.IWinningAlgo;
import aialgo.almostwin.proofnumbersearch.Node;
import aialgo.almostwin.proofnumbersearch.ProofNumberSearch;
import aialgo.vcx.VCX;
import chessboardalgo.IChessboardAIAlgo;
import common.InputValidator;
import common.Position;
import lombok.Getter;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;

import java.util.List;

import static aialgo.almostwin.proofnumbersearch.Node.Status.*;
import static aialgo.almostwin.proofnumbersearch.Node.Type.OR;
import static common.PositionConverter.getX;
import static common.PositionConverter.getY;
import static player.Player.enemyColor;

public class AlmostPNSWin extends ProofNumberSearch implements IAIAlgo, IWinningAlgo {
    @Getter
    protected IChessboardAIAlgo chessboardAlgo;
    @Getter
    protected IScoreManager scoreManager;
    @Getter
    protected int humanColor;
    protected int aiColor;
    protected int size;
    private final long timeLimitInMs;
    private final int vcxDepth;
    private final int vcxCountLimit;
    private Node previousStep;

    public AlmostPNSWin(IChessboardAIAlgo chessboardAlgo, int humanColor, int timeLimitInMs, int vcxCountLimit, int vcxDepth) {
        InputValidator.checkColorValid(humanColor);
        this.timeLimitInMs = timeLimitInMs;
        this.vcxDepth = vcxDepth;
        this.chessboardAlgo = chessboardAlgo;
        this.scoreManager = new CachedScoreManager(chessboardAlgo);
        this.humanColor = humanColor;
        this.aiColor = 3 - humanColor;
        this.size = chessboardAlgo.getSize();
        this.vcxCountLimit = vcxCountLimit;
    }

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        scoreManager.updateScore(y, x);
        if (previousStep != null) {
            previousStep = previousStep.findChildren(y, x);
        }
    }

    @Override
    public Position aiFindPos() {
        if (previousStep == null) {
            previousStep = new Node(null, -1, -1, humanColor, chessboardAlgo.steps());
        }
        Node.Status status = previousStep.getValue();
        if (status == UNKNOWN) {
            pns(previousStep, chessboardAlgo.clone());
        }
        status = previousStep.getValue();
        if (status == PROVEN) {
            previousStep = previousStep.findWinMove();
            return new Position(previousStep.y, previousStep.x, true);
        }
        return Position.EMPTY;
    }

    @Override
    protected void generateChildren(Node n, IChessboardAIAlgo chessboardAIAlgo) {

        int enemyColor = enemyColor(n.color);
        List<Integer> candidates = new CachedScoreManager(chessboardAIAlgo).generateCandidatePiece(enemyColor);

        for (int candidate : candidates) {
            int x = getX(candidate, size), y = getY(candidate, size);
            Node chd = new Node(n, y, x, enemyColor, n.step + 1);
            n.children.add(chd);
        }
    }

    @Override
    protected boolean resourcesAvailable(long startTimeInMs, int count) {
        return System.currentTimeMillis() - startTimeInMs < timeLimitInMs && count < vcxCountLimit;
    }

    @Override
    protected void evaluate(Node n, IChessboardAIAlgo chessboardAIAlgo) {
        System.out.println(n.x + "," + n.y + "," + n.step);
        //chessboardAIAlgo.print();
        Position p = new VCX(chessboardAIAlgo, n.color, vcxDepth).aiFindPos();
        if (p != Position.EMPTY) {
            n.setValue(n.type == OR ? PROVEN : DISPROVEN);
        }
    }
}
