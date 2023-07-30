package scorecalculator;

import chessboardalgo.IChessboardAIAlgo;

public class PointEvaluator {
    private int size;
    private IChessboardAIAlgo chessBoardAlgo;
    private int[][][][] scoreCache;
    private int leftEmptyPos = -1, rightEmptyPos = -1, leftCnt = 0, rightCnt = 0;
    private boolean leftBlock = false, rightBlock = false;

    public PointEvaluator(int size, IChessboardAIAlgo chessBoardAlgo, int[][][][] scoreCache) {
        this.size = size;
        this.chessBoardAlgo = chessBoardAlgo;
        this.scoreCache = scoreCache;
    }

    public int scorePoint(int y, int x, int role) {
        return scorePoint(y, x, role, null);
    }

    public int scorePoint(int y, int x, int role, Direction requiredDir) {
        int result = 0;
        for (Direction curDir : Direction.values()) {
            if (!curDir.match(requiredDir)) {
                result += scoreCache[role][curDir.ordinal()][y][x];
                continue;
            }
            reset();
            for (int i = 1; true; i++) {
                int ny = y + i * curDir.dy, nx = x + i * curDir.dx;
                if (outOfBound(ny, nx)) {
                    rightBlock = true;
                    break;
                }
                int val = chessBoardAlgo.getValInBoard(nx, ny);
                if (val == 0) {
                    int nny = ny + curDir.dy, nnx = nx + curDir.dx;
                    if (rightEmptyPos == -1 && !outOfBound(nny, nnx)
                            && chessBoardAlgo.getValInBoard(nnx, nny) == role) {
                        rightEmptyPos = rightCnt;
                        continue;
                    } else {
                        break;
                    }
                } else if (val == role) {
                    rightCnt++;
                    continue;
                } else {
                    rightBlock = true;
                    break;
                }
            }

            for (int i = -1; true; i--) {
                int ny = y + i * curDir.dy, nx = x + i * curDir.dx;
                if (outOfBound(ny, nx)) {
                    leftBlock = true;
                    break;
                }
                int val = chessBoardAlgo.getValInBoard(nx, ny);
                if (val == 0) {
                    int nny = ny - curDir.dy, nnx = nx - curDir.dx;
                    if (leftEmptyPos == -1 && !outOfBound(nny, nnx)
                            && chessBoardAlgo.getValInBoard(nnx, nny) == role) {
                        leftEmptyPos = 0;
                        continue;
                    } else {
                        break;
                    }
                } else if (val == role) {
                    leftCnt++;
                    if (leftEmptyPos != -1) leftEmptyPos++;
                    continue;
                } else {
                    leftBlock = true;
                    break;
                }
            }
            scoreCache[role][curDir.ordinal()][y][x] = countToScore();
            result += scoreCache[role][curDir.ordinal()][y][x];
        }
        return result;
    }

    private int countToScore() {
        if (leftEmptyPos == -1 && rightEmptyPos == -1) {
            int count = leftCnt + rightCnt + 1;
            if (count >= 5) return Score.FIVE.value;
            if (!leftBlock && !rightBlock) return Score.calculateNonEmpty(count, false);
            else if (!leftBlock || !rightBlock) return Score.calculateNonEmpty(count, true);
        } else if (leftEmptyPos >= 0 && rightEmptyPos == -1) {
            int count = leftCnt + rightCnt + 1;
            return Score.calculateSingleEmpty(leftEmptyPos, count, leftBlock, rightBlock);
        } else if (leftEmptyPos == -1 && rightEmptyPos >= 0) {
            int count = leftCnt + rightCnt + 1;
            return Score.calculateSingleEmpty(rightCnt - rightEmptyPos, count, rightBlock, leftBlock);
        } else if (leftEmptyPos >= 0 && rightEmptyPos >= 0) {
            if (leftEmptyPos > rightCnt - rightEmptyPos)
                return Score.calculateDoubleEmpty(rightCnt - rightEmptyPos, rightEmptyPos, rightBlock, leftCnt - leftEmptyPos, leftEmptyPos, leftBlock);
            return Score.calculateDoubleEmpty(leftEmptyPos, leftCnt - leftEmptyPos, leftBlock, rightEmptyPos, rightCnt - rightEmptyPos, rightBlock);
        } else {
            throw new IllegalStateException("!!!");
        }
        return 0;
    }

    private boolean outOfBound(int y, int x) {
        return y < 0 || x < 0 || y >= size || x >= size;
    }

    private void reset() {
        leftEmptyPos = -1;
        rightEmptyPos = -1;
        leftCnt = 0;
        rightCnt = 0;
        leftBlock = false;
        rightBlock = false;
    }
}
