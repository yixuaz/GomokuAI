package aialgo;

import chessboardalgo.IChessboardAIAlgo;
import common.DebugContext;
import common.Position;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;
import scorecalculator.Score;
import aialgo.vcx.NegamaxVCXEnhancedContext;
import aialgo.vcx.VCX;
import zobrist.Zobrist;

import java.util.List;
import java.util.Optional;

import static zobrist.Zobrist.DISABLE_CACHE_MASK;

public class MinMaxAIAlgo extends RecursiveBaseAIAlgo {

    protected int firstDepth;
    protected NegamaxVCXEnhancedContext VCXContext;
    protected int nextPoint = -1;
    protected Zobrist zobrist;
    protected DebugContext debugContext = DebugContext.ENABLE;

    public MinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor, int depth, NegamaxVCXEnhancedContext context) {
        super(chessBoardAlgo, new CachedScoreManager(chessBoardAlgo), enemyColor);
        zobrist = new Zobrist(chessBoardAlgo);
        firstDepth = depth;
        VCXContext = context;
    }

    public MinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor, int depth) {
        this(chessBoardAlgo, enemyColor, depth, NegamaxVCXEnhancedContext.DISABLE);
    }

    public MinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor) {
        this(chessBoardAlgo, enemyColor, 9, NegamaxVCXEnhancedContext.DISABLE);
    }

    public MinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor, NegamaxVCXEnhancedContext context) {
        this(chessBoardAlgo, enemyColor, 9, context);
    }

    protected int negamax(boolean isAI, int depth, int alpha, int beta, int idx) {
        Optional<Integer> cached = zobrist.tryGet(depth);
        if (depth != firstDepth && !cached.isEmpty()) {
            return cached.get();
        }

        if ((idx != -1 && chessboardAlgo.isTerminal(getX(idx), getY(idx))) || depth == 0) {
            return zobrist.setAndReturnScore(scoreManager.evaluation(isAI, humanColor), depth);
        }
        if (Thread.currentThread().isInterrupted()) return isAI ? -DISABLE_CACHE_MASK : DISABLE_CACHE_MASK;

        List<Integer> blankList = generateCandidatePiece(isAI);

        if (chessboardAlgo.steps() > VCXContext.startSteps && depth == firstDepth - VCXContext.applyDeltaDepth) {
            VCX vcx = new VCX(chessboardAlgo.clone(), isAI ? humanColor : aiColor, VCXContext.vcxDepth);
            if (vcx.aiFindPos() != Position.EMPTY) {
                return zobrist.setAndReturnScore(Score.FIVE.value, depth);
            }
        }
        debugContext.debugStartInfo(depth, firstDepth);

        int resVal = Integer.MIN_VALUE;
        for (int nextStep : blankList) {
            if (Thread.currentThread().isInterrupted()) return isAI ? -DISABLE_CACHE_MASK : DISABLE_CACHE_MASK;

            if (!debugContext.isInDebugStep(depth, firstDepth, nextStep)) continue;
            int y = getY(nextStep), x = getX(nextStep);
            addPiece(y, x, isAI);
            int value = -negamax(!isAI, depth - 1, -beta, -alpha, nextStep);
            removePiece(y, x, isAI);

            debugContext.debugResultInfo(depth, y, x, firstDepth, String.format("value:%s", value));

            resVal = Math.max(resVal, value);
            if (value > alpha) {
                if (depth == firstDepth) {
                    nextPoint = nextStep;
                    if (resVal >= Score.FIVE.value) return zobrist.setAndReturnScore(resVal, depth);
                }
                if (value >= beta) return value;
                alpha = value;
            }
        }

        return zobrist.setAndReturnScore(resVal, depth);
    }

    @Override
    protected void removePiece(int y, int x, boolean isAI) {
        int role = chessboardAlgo.getValInBoard(x, y);
        super.removePiece(y, x, isAI);
        zobrist.updateHash(y, x, role);
    }

    @Override
    protected void addPiece(int y, int x, boolean isAI) {
        super.addPiece(y, x, isAI);
        int role = isAI ? aiColor : humanColor;
        zobrist.updateHash(y, x, role);
    }

    private List<Integer> generateCandidatePiece(boolean isAI) {
        return scoreManager.generateCandidatePiece(isAI ? aiColor : humanColor);
    }


    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        zobrist.updateHash(y, x, player);
        super.setPieceCallBack(y, x, player, isAI);
    }

    @Override
    public Position aiFindPos() {
        int oriDep = firstDepth;
        firstDepth = chessboardAlgo.steps() < 8 ? Math.min(firstDepth, 7) : firstDepth;
        int score = negamax(true, firstDepth, -99999999, 99999999, -1);
        firstDepth = oriDep;
        return new Position(getY(nextPoint), getX(nextPoint), score >= Score.FIVE.value);
    }
}
