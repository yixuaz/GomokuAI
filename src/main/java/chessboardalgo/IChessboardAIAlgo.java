package chessboardalgo;

import common.Position;

import java.util.Deque;

public interface IChessboardAIAlgo extends Cloneable {
    Deque<Position> getAllSteps();

    int getSize();

    boolean isTerminal(int x, int y);

    boolean isLegalMove(int x, int y);

    int steps();

    int getValInBoard(int x, int y);

    boolean hasNeighbor(int x, int y, int distance, int count);

    void setPiece(int x, int y, int color);

    IChessboardAIAlgo clone();

    void print();

    String generateStepsCode();
}
