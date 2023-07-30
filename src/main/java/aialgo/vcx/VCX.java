package aialgo.vcx;

import aialgo.IWinningAlgo;
import aialgo.RecursiveBaseAIAlgo;
import chessboardalgo.IChessboardAIAlgo;
import common.DebugContext;
import common.Position;
import common.ThreadPoolContext;
import lombok.Setter;
import player.Player;
import scorecalculator.Score;
import scorecalculator.VCXCachedScoreManager;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static scorecalculator.VCXCachedScoreManager.pos;
import static scorecalculator.VCXCachedScoreManager.score;

public class VCX extends RecursiveBaseAIAlgo implements IWinningAlgo {

    private final int TIME_LIMIT_MS = 55_000;
    private int nextPoint = -1;
    private final long[] zobristTable;
    private long hash = 0;
    private long startTime = 0;
    private Map<Long, Boolean> zobristCache;
    private int firstDepth;
    private int timeFactor;
    private VCXCachedScoreManager vcxCachedScoreManager;

    @Setter
    private DebugContext debugContext = DebugContext.DISABLE;

    public VCX(IChessboardAIAlgo chessBoardAlgo, int humanColor) {
        this(chessBoardAlgo, humanColor, 23, VCXOptimization.FAST);
    }
    public VCX(IChessboardAIAlgo chessBoardAlgo, int humanColor, int firstDepth) {
        this(chessBoardAlgo, humanColor, firstDepth, VCXOptimization.FAST);
    }
    public VCX(IChessboardAIAlgo chessBoardAlgo, int humanColor, int firstDepth, VCXOptimization killOptimization) {
        super(chessBoardAlgo, new VCXCachedScoreManager(chessBoardAlgo, humanColor, killOptimization), humanColor);
        this.vcxCachedScoreManager = (VCXCachedScoreManager) scoreManager;
        this.firstDepth = firstDepth;
        this.timeFactor = killOptimization.factor;
        zobristTable = new long[size * size * 2];
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < zobristTable.length; i++)
            zobristTable[i] = secureRandom.nextLong();
    }

    @Override
    public boolean isAbsoluteForcedWin() {
        return true;
    }

    @Override
    public Position aiFindPos() {
        nextPoint = -1;
        int oriFirstDepth = firstDepth;
        startTime = System.currentTimeMillis();
        // 迭代加深
        for (int i = Math.max(oriFirstDepth - 16, 5); i <= oriFirstDepth; i += 4) {
            firstDepth = i;
            zobristCache = new HashMap<>();
            if (aiWin(aiColor, i, -1)) break;
            if ((System.currentTimeMillis() - startTime) * timeFactor > TIME_LIMIT_MS) break;
        }
        firstDepth = oriFirstDepth;
        if (nextPoint == -1)
            return Position.EMPTY;
        return new Position(getY(nextPoint), getX(nextPoint), true);
    }

    // lastMaxPoint, lastAIMaxPointScore 为了优化性能, 方便剪枝
    boolean aiWin(int role, int depth, int lastMaxPoint) {
        // 因为会根据lastMaxPoint 进行进攻剪枝，所以CACHE里的输不一定是输
        Boolean cached = zobristCache.get(hash);
        if (cached != null) return cached;

        if (depth <= 0) return setAndReturn(false);

        List<Long> candidates = vcxCachedScoreManager.findAIKillSteps(lastMaxPoint);
        if (!candidates.isEmpty() && score(candidates.get(0)) >= Score.FOUR.value) {
            if (depth == firstDepth)
                nextPoint = pos(candidates.get(0));
            return setAndReturn(true);
        }

        if (candidates.isEmpty()) return setAndReturn(false);

        debugContext.debugStartInfo(depth, firstDepth);

        int maxPoint = -1, aIMaxPointScore = 0;
        for (long p : candidates) {
            if (Thread.currentThread().isInterrupted()) return false;
            if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) return false;
            int pos = pos(p), score = score(p);

            if (!debugContext.isInDebugStep(depth, firstDepth, pos)) continue;

            int y = getY(pos), x = getX(pos);
            addPiece(y, x, pos,true);
            if (score > -Score.FIVE.value) {
                maxPoint = pos;
                aIMaxPointScore = score;
            }
            boolean humanLose = humanLose(Player.enemyColor(role), depth - 1, maxPoint, aIMaxPointScore);

            debugContext.debugResultInfo(depth, y, x, firstDepth, String.format("human lose: %s", humanLose));

            removePiece(y, x, pos, true);
            if (humanLose) {
                if (depth == firstDepth)
                    nextPoint = pos;
                return setAndReturn(true);
            }
        }
        return setAndReturn(false);
    }

    private boolean humanLose(int role, int depth, int lastMaxPoint, int lastAIMaxPointScore) {
        Boolean cached = zobristCache.get(hash);
        if (cached != null) return cached;
        // 超过回合数，代表防守成功
        if (depth <= 0) return setAndReturn(false);
        List<Long> candidates = vcxCachedScoreManager.findHumanDefendSteps(lastAIMaxPointScore);
        // 如果发现对面没有进攻手段（活三，冲四，活四），则代表防守成功
        if (candidates.isEmpty()) return setAndReturn(false);
        // 如果对面能成五，发现自己有成五；
        // 如果对面不能成五，发现自己有活四；
        if (-1 * score(candidates.get(0)) >= Score.FOUR.value)
            return setAndReturn(false);
        debugContext.debugStartInfo(depth, firstDepth);
        for (long p : candidates) {
            int pos = pos(p);
            if (!debugContext.isInDebugStep(depth, firstDepth, pos)) continue;
            int y = getY(pos), x = getX(pos);
            addPiece(y, x, pos, false);
            boolean aiWin = aiWin(Player.enemyColor(role), depth - 1, lastMaxPoint);
            debugContext.debugResultInfo(depth, y, x, firstDepth, String.format("ai Win:%s",aiWin));
            removePiece(y, x, pos, false);
            if (!aiWin) return setAndReturn(false);
        }
        return setAndReturn(true);
    }


    protected void addPiece(int y, int x, int nextStep, boolean isAI) {
        int color = isAI ? aiColor : humanColor;
        hash ^= zobristTable[(nextStep << 1) | (color - 1)];
        super.addPiece(y, x, isAI);

    }

    protected void removePiece(int y, int x, int nextStep, boolean isAI) {
        int color = isAI ? aiColor : humanColor;
        hash ^= zobristTable[(nextStep << 1) | (color - 1)];
        super.removePiece(y, x, isAI);
    }

    private boolean setAndReturn(boolean res) {
        zobristCache.put(hash, res);
        return res;
    }

    public static Future<Position> asyncVCX(int depth, IChessboardAIAlgo chessBoardAlgo, int humanColor) {
        VCX tmp = new VCX(chessBoardAlgo.clone(), humanColor, depth);
        Callable<Position> futureTask = () -> tmp.aiFindPos();
        return ThreadPoolContext.threadPool.submit(futureTask);
    }
}
