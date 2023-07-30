package chessboardalgo;

import UI.Piece;
import common.Position;

import java.util.Deque;

public interface IChessboardUIAlgo {
    boolean isLegalMove(Piece piece);

    void setPiece(Piece piece);

    boolean isTerminal(Piece piece);

    String generateStepsCode();

    Deque<Position> getAllSteps();

}
