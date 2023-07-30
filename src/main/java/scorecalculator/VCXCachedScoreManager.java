package scorecalculator;

import chessboardalgo.IChessboardAIAlgo;
import player.Player;
import aialgo.vcx.VCXOptimization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static common.PositionConverter.*;

public class VCXCachedScoreManager extends CachedScoreManager {

    private int[][] aiScore;
    private int[][] humanScore;
    private final VCXOptimization killOptimization;

    public VCXCachedScoreManager(IChessboardAIAlgo chessBoardAIAlgo, int humanColor, VCXOptimization vcxOptimization) {
        super(chessBoardAIAlgo);
        aiScore = humanColor != Player.BLACK.getId() ? blackScore : whiteScore;
        humanScore = humanColor == Player.BLACK.getId() ? blackScore : whiteScore;
        killOptimization = vcxOptimization;
    }

    public List<Long> findAIKillSteps(int lastMaxPoint) {
        List<Long> results = new ArrayList<>();
        List<Long> fives = new LinkedList<>();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (chessBoardAlgo.getValInBoard(x, y) != 0) continue;
                int pos = convertToIdx(y, x, size);
                if (aiScore[y][x] >= Score.FIVE.value) {
                    return List.of(encode(aiScore[y][x], pos));
                } else if (humanScore[y][x] >= Score.FIVE.value) {
                    fives.add(encode(-humanScore[y][x], pos));
                } else {
                    if (lastMaxPoint == -1
                            || y == getY(lastMaxPoint, size)
                            || x == getX(lastMaxPoint, size)
                            || Math.abs(y - getY(lastMaxPoint, size)) == Math.abs(x - getX(lastMaxPoint, size))) {
                        // 连续进攻
                        if (aiScore[y][x] >= Score.THREE.value)
                            results.add(encode(aiScore[y][x], pos));
                    } else if (humanScore[y][x] >= Score.FOUR.value && aiScore[y][x] >= Score.THREE.value) {
                        // 连守带攻
                        results.add(encode(aiScore[y][x], pos));
                    } else if (killOptimization.matchKillSteps(aiScore[y][x])) {
                        results.add(encode(aiScore[y][x], pos));
                    }
                }
            }
        }
        if (!fives.isEmpty()) return fives;
        results.sort((a, b)-> Integer.compare(score(b), score(a)));
        return results;
    }



    public List<Long> findHumanDefendSteps(int lastAIMaxPointScore) {
        List<Long> fives = new ArrayList<>();
        LinkedList<Long> fours = new LinkedList<>();
        List<Long> blockedFours = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (chessBoardAlgo.getValInBoard(x, y) != 0) continue;
                int pos = convertToIdx(y, x, size), hs = humanScore[y][x], as = aiScore[y][x];
                if (hs >= Score.FIVE.value) {
                    return List.of(encode(-hs, pos));
                }
                if (as >= Score.FIVE.value) {
                    fives.add(encode(as, pos));
                } else if (hs >= Score.FOUR.value) {
                    fours.addFirst(encode(-hs, pos));
                } else if (as >= Score.FOUR.value) {
                    fours.add(encode(as, pos));
                } else if (hs >= Score.BLOCKED_FOUR.value) {
                    fours.add(encode(-hs, pos));
                } else if (as >= Score.BLOCKED_FOUR.value) {
                    blockedFours.add(encode(as, pos));
                }
            }
        }
        // 挡对面的冲四
        if (!fives.isEmpty()) return fives;
        // 形成自己的活四，挡对面的活三, 形成自己的冲四
        if (!fours.isEmpty()) {
            // 有时对面的杀棋，是活三的一个冲四位而非活四位
            fours.addAll(blockedFours);
        }
        return fours;
    }

    public static long encode(int score, int pos) {
        return (((long)score << 32) | pos);
    }

    public static int pos(long p) {
        return (int) (p & 0xffffffff);
    }

    public static int score(long p) {
        return (int) (p >>> 32);
    }
}
