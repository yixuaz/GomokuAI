package scorecalculator;

import chessboardalgo.IChessboardAIAlgo;
import common.InputValidator;
import player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static common.PositionConverter.*;

public class AlmostVCXWinCachedScoreManager extends CachedScoreManager {
    private int humanColor;
    private int aiColor;
    public AlmostVCXWinCachedScoreManager(IChessboardAIAlgo chessBoardAIAlgo, int humanColor) {
        super(chessBoardAIAlgo);
        InputValidator.checkColorValid(humanColor);
        this.humanColor = humanColor;
        this.aiColor = 3 - humanColor;
    }

    public List<Integer> findBlockOrNonBlockFour(int role, Set<Integer> usedBlockFour) {
        List<Integer> myFours = new ArrayList<>();
        List<Integer> importants = new ArrayList<>();
        List<Integer> lessImp = new ArrayList<>();
        List<Integer> myBlockedFours = new ArrayList<>();
        List<int[]> twos = new ArrayList<>();
        List<Integer> ones = new ArrayList<>();
        int white = Player.WHITE.getId();
        int[][] myScore = role == white ? whiteScore : blackScore;
        int[][] enemyScore = role == white ? blackScore : whiteScore;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (chessBoardAlgo.getValInBoard(x, y) != 0) continue;
                if (!chessBoardAlgo.hasNeighbor(x, y, 1, 1)) {
                    if (!chessBoardAlgo.hasNeighbor(x, y, 2, 1) || myScore[y][x] < Score.TWO.value) {
                        continue;
                    }
                }
                int scoreMe = myScore[y][x];
                int scoreEnemy = enemyScore[y][x];
                int idx = convertToIdx(y, x, size);
                if (scoreMe >= Score.FIVE.value) {
                    // 不是VCX要处理的情况
                    return List.of(-(idx+1));
                } else if (scoreEnemy >= Score.FIVE.value) {
                    return usedBlockFour != null ? List.of() : List.of(idx);
                } else if (scoreMe >= Score.FOUR.value) {
                    myFours.add(idx);
                } else if (scoreEnemy >= Score.FOUR.value) {
                    importants.add(idx);
                } else if (scoreMe >= Score.BLOCKED_FOUR.value) {
                    if (usedBlockFour != null && !usedBlockFour.contains(idx)) {
                        myBlockedFours.add(packTwoInts(idx, echoBlockFourPos(myScore, idx, role == aiColor)));
                    }
                } else if (scoreEnemy >= Score.BLOCKED_FOUR.value) {
                    importants.add(idx);
                } else if (scoreMe >= Score.THREE.value || scoreEnemy >= Score.THREE.value) {
                    importants.add(idx);
                } else if (scoreMe >= Score.TWO.value) {
                    lessImp.add(idx);
                } else if (scoreEnemy >= Score.TWO.value) {
                    twos.add(new int[]{idx, scoreEnemy});
                } else {
                    ones.add(idx);
                }
            }
        }
        if (usedBlockFour != null) {
            return myBlockedFours;
        }
        if (!myFours.isEmpty()) return myFours;
        importants.addAll(lessImp);
        if (importants.size() > 20) return importants.subList(0, 20);
        twos.sort((a,b)-> Integer.compare(b[1],a[1]));
        if (!twos.isEmpty()) {
            for (int[] i : twos) {
                importants.add(i[0]);
            }
        } else {
            importants.addAll(ones);
        }
        if (importants.size() > 20) return importants.subList(0, 20);
        return importants;
    }

    private static int packTwoInts(int firstInt, int secondInt) {
        assert firstInt >= 0 && firstInt < 225;
        assert secondInt >= 0 && secondInt < 225;
        // Shift the firstInt to the left by 32 bits and bitwise OR with the secondInt
        return (firstInt << 8) + secondInt;
    }

    private int echoBlockFourPos(int[][] score, int idx, boolean isAI) {
        int y = getY(idx, size), x = getX(idx, size);

        chessBoardAlgo.setPiece(x, y, isAI ? aiColor : humanColor);
        updateScore(y, x);

        int radius = 4;
        for (Direction dir : Direction.values()) {
            for (int i = -radius; i <= radius; i++) {
                int ny = y + dir.dy * i;
                int nx = x + dir.dx * i;
                if (ny < 0 || nx < 0 || ny >= size || nx >= size || chessBoardAlgo.getValInBoard(nx, ny) != 0) continue;
                if (score[ny][nx] >= Score.FIVE.value) {
                    chessBoardAlgo.setPiece(x, y, 0);
                    updateScore(y, x);
                    return convertToIdx(ny, nx, size);
                }
            }
        }
        throw new IllegalStateException();
    }
}
