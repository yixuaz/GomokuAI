package aialgo;

import aialgo.vcx.NegamaxVCXEnhancedContext;
import chessboardalgo.IChessboardAIAlgo;
import common.Position;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;

import java.util.concurrent.Semaphore;

public class ThreadSafeMinMaxAIAlgo extends MinMaxAIAlgo {
    private IChessboardAIAlgo originalChessboardAlgo;
    private IScoreManager originalScoreManager;
    private Semaphore semaphore = new Semaphore(1);

    public ThreadSafeMinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor) {
        this(chessBoardAlgo, enemyColor, 9, NegamaxVCXEnhancedContext.DISABLE);
    }

    public ThreadSafeMinMaxAIAlgo(IChessboardAIAlgo chessBoardAlgo, int enemyColor, int depth, NegamaxVCXEnhancedContext context) {
        super(chessBoardAlgo.clone(), enemyColor, depth, context);
        originalChessboardAlgo = chessBoardAlgo;
        originalScoreManager = new CachedScoreManager(chessBoardAlgo);
    }

    @Override
    public IScoreManager getScoreManager() {
        return originalScoreManager;
    }

    @Override
    public IChessboardAIAlgo getChessboardAlgo() {
        return originalChessboardAlgo;
    }

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        originalScoreManager.updateScore(y, x);
        super.setPieceCallBack(y, x, player, isAI);
    }

    @Override
    public Position aiFindPos() {
        try {
            cloneStatefulComponent();
            Position res = super.aiFindPos();
            semaphore.release();
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Position.EMPTY;
    }

    private void cloneStatefulComponent() throws InterruptedException {
        semaphore.acquire();
        chessboardAlgo = originalChessboardAlgo.clone();
        scoreManager = new CachedScoreManager(chessboardAlgo);
    }
}
