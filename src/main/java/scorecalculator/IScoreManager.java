package scorecalculator;

import java.util.List;

public interface IScoreManager {
    int evaluation(boolean isAI, int humanColor);

    List<Integer> generateCandidatePiece(int role);

    void updateScore(int y, int x);

    void initScore();

    int getScore(int y, int x, int role);
}
