package aialgo;

import chessboardalgo.IChessboardAIAlgo;
import common.Position;
import scorecalculator.IScoreManager;

public interface IAIAlgo {

    void setPieceCallBack(int y, int x, int player, boolean isAI);

    Position aiFindPos();

    IChessboardAIAlgo getChessboardAlgo();

    IScoreManager getScoreManager();

    int getHumanColor();
}
