package aialgo;

import chessboardalgo.IChessboardAIAlgo;
import common.InputValidator;
import common.PositionConverter;
import lombok.Getter;
import scorecalculator.IScoreManager;

import static common.PositionConverter.convertToIdx;

public abstract class RecursiveBaseAIAlgo implements IAIAlgo {
    @Getter
    protected IChessboardAIAlgo chessboardAlgo;
    @Getter
    protected IScoreManager scoreManager;
    @Getter
    protected int humanColor;
    protected int aiColor;
    protected int size;
    public RecursiveBaseAIAlgo(IChessboardAIAlgo chessboardAlgo, IScoreManager scoreManager, int humanColor) {
        InputValidator.checkColorValid(humanColor);
        this.chessboardAlgo = chessboardAlgo;
        this.scoreManager = scoreManager;
        this.humanColor = humanColor;
        this.aiColor = 3 - humanColor;
        this.size = chessboardAlgo.getSize();
    }

    protected void removePiece(int y, int x, boolean isAI) {
        chessboardAlgo.setPiece(x, y, 0);
        scoreManager.updateScore(y, x);
    }

    protected void addPiece(int y, int x, boolean isAI) {
        int role = isAI ? aiColor : humanColor;
        chessboardAlgo.setPiece(x, y, role);
        scoreManager.updateScore(y, x);
    }

    protected void removePiece(int pos, boolean isAI) {
        int y = getY(pos), x = getX(pos);
        removePiece(y, x, isAI);
    }

    protected void addPiece(int pos, boolean isAI) {
        int y = getY(pos), x = getX(pos);
        addPiece(y, x, isAI);
    }

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        scoreManager.updateScore(y, x);
    }

    protected int getX(int pos) {
        return PositionConverter.getX(pos, size);
    }

    protected int getY(int pos) {
        return PositionConverter.getY(pos, size);
    }

    protected int getIdx(int y, int x) {
        return convertToIdx(y, x, size);
    }
}
