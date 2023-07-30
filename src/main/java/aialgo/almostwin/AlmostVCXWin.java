package aialgo.almostwin;

import aialgo.IWinningAlgo;
import aialgo.RecursiveBaseAIAlgo;
import chessboardalgo.IChessboardAIAlgo;
import common.BoardToString;
import common.DebugContext;
import common.Position;
import scorecalculator.AlmostVCXWinCachedScoreManager;
import aialgo.vcx.VCX;
import aialgo.vcx.VCXOptimization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

import static consistent.ConsistentPattern.canonical;

public class AlmostVCXWin extends RecursiveBaseAIAlgo implements IWinningAlgo {
    private static final int TIME_LIMIT_MS = 45_000;
    public long startTime;
    private int depth;
    protected AlmostVCXWinCachedScoreManager myScoreManager;
    private DebugContext debugContext = DebugContext.DISABLE;
    private static final Map<String, Integer> VCXManager2Depth = new HashMap<>();
    static {
        loadVCXManager(AlmostVCXWin.class.getClassLoader().getResourceAsStream("game_almost_vcx_win.data"));
    }

    public AlmostVCXWin(IChessboardAIAlgo chessboardAlgo, int humanColor, int depth) {
        super(chessboardAlgo, new AlmostVCXWinCachedScoreManager(chessboardAlgo, humanColor), humanColor);
        myScoreManager = (AlmostVCXWinCachedScoreManager) scoreManager;
        String key = canonical(new ArrayList<>(chessboardAlgo.getAllSteps()));
        this.depth = VCXManager2Depth.getOrDefault(key, depth);
    }

    public Position aiFindPos() {
        System.out.println("逼近算杀开始");
        int nextPoint = attack();
        if (nextPoint == -1)
            return Position.EMPTY;
        return new Position(getY(nextPoint), getX(nextPoint), true);
    }

    public int attack() {
        startTime = System.currentTimeMillis();
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(aiColor, null);
        if (!candidates.isEmpty() && candidates.get(0) < 0) return -candidates.get(0)-1;
        // 忽略 黑棋冲4 布局，防止指数爆炸
        // 黑棋非冲4 布局

        debugContext.debugInfo("进攻层结果：");

        for (int candidate : candidates) {
            if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS || Thread.currentThread().isInterrupted()) break;
            if (!debugContext.isInDebugStep(0,0, candidate)) continue;
            int y = getY(candidate), x = getX(candidate);
            addPiece(y, x, true);
            boolean mustWin = blockFourDefendFail(candidate, new HashSet<>());
            removePiece(y, x, true);
            debugContext.debugInfo(String.format("y:%s,x:%s,must win:%s",getY(candidate),getX(candidate),mustWin));
            if (mustWin) return candidate;
        }
        return -1;

    }

    private boolean blockFourDefendFail(int attackStep, Set<Integer> usedBlockFours) {
        assert !chessboardAlgo.isTerminal(getX(attackStep), getY(attackStep));
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(humanColor, usedBlockFours);
        if (!candidates.isEmpty() && candidates.get(0) < 0) return false;
        if (candidates.size() > 8) {
            return blockFourDefendFailFast();
        } else if (candidates.size() > 4) {
            return blockFourDefendFailMedium();
        }
        return blockFourDefendFailComplete(attackStep, usedBlockFours);
    }

    // 只计算单步冲四防御 比如8个冲4，2 * 8 = 16, (1111,1112,1121,1122,...)
    private boolean blockFourDefendFailFast() {
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(humanColor, Set.of());
        if (!candidates.isEmpty() && candidates.get(0) < 0) return false;
        if (!nonBlockFourDefendFail()) return false;
        for (int candidatePair: candidates) {
            int humanPos = candidatePair >> 8, aiPos = (candidatePair & 0x0ff);
            addPiece(humanPos, false);
            addPiece(aiPos, true);
            boolean defendFail = nonBlockFourDefendFail();
            removePiece(humanPos, false);
            removePiece(aiPos, true);
            if (!defendFail) return false;
        }
        return true;
    }

    // 枚举把冲四用完去防守，只要有一种冲4用完的策略能防下来就算防住，时间复杂度低。 比如4个冲4，2 ^ 4 = 16, (1111,1112,1121,1122,...)
    private boolean blockFourDefendFailMedium() {
        if (!nonBlockFourDefendFail()) return false;
        return blockFourDefendFailMediumInternal();
    }

    private boolean blockFourDefendFailMediumInternal() {
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(humanColor, Set.of());
        if (!candidates.isEmpty() && candidates.get(0) < 0) return false;
        if (candidates.isEmpty()) return nonBlockFourDefendFail();
        int candidatePair = candidates.get(0);
        int humanPos = candidatePair >> 8, aiPos = (candidatePair & 0x0ff);
        int hy = getY(humanPos), hx = getX(humanPos);
        int ay = getY(aiPos), ax = getX(aiPos);

        // 这一冲4 ，可以下2个位置
        addPiece(hy, hx, false);
        addPiece(ay, ax, true);
        if (!blockFourDefendFailMediumInternal()) return false;
        removePiece(hy, hx, false);
        removePiece(aiPos, true);

        addPiece(ay, ax, false);
        addPiece(hy, hx, true);
        if (!blockFourDefendFailMediumInternal()) return false;
        removePiece(ay, ax, false);
        removePiece(hy, hx, true);

        return true;
    }

    // 枚举全部情况，时间复杂度高。 比如2个冲4，会有9（3 ^ 2)种可能 (00,01,02,11,12,21,22,10,20), 2个冲4,27种可能
    private boolean blockFourDefendFailComplete(int attackStep, Set<Integer> usedBlockFours) {
        // 白棋冲4 防守
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(humanColor, usedBlockFours);
        if (!candidates.isEmpty() && candidates.get(0) < 0) return false;
        if (!nonBlockFourDefendFail()) return false;
        Set<Integer> tmp = new HashSet<>(usedBlockFours);
        for (int candidatePair: candidates) {
            int humanPos = candidatePair >> 8, aiPos = (candidatePair & 0x0ff);
            int hy = getY(humanPos), hx = getX(humanPos);
            int ay = getY(aiPos), ax = getX(aiPos);
            boolean isFirstAdd = tmp.add(humanPos);
            assert isFirstAdd;
            addPiece(hy, hx, false);
            addPiece(ay, ax, true);
            boolean defendFail = blockFourDefendFailComplete(aiPos, tmp);
            removePiece(hy, hx, false);
            removePiece(ay, ax, true);
            if (!defendFail) return false;
        }
        return true;
    }

    // 白棋非冲4 防守
    private boolean nonBlockFourDefendFail() {
        if (System.currentTimeMillis() - startTime >= TIME_LIMIT_MS || Thread.currentThread().isInterrupted()) return false;
        List<Integer> candidates = myScoreManager.findBlockOrNonBlockFour(humanColor, null);
        if (!candidates.isEmpty() && candidates.get(0) < 0) return false;
        debugContext.debugInfoWithNonEmptyDebugSteps("防御层:" + chessboardAlgo.generateStepsCode());

        assert !candidates.isEmpty();
        ExecutorService threadPool = Executors.newFixedThreadPool(candidates.size());
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(threadPool);
        for (int candidate : candidates) {
            debugContext.debugInfoWithNonEmptyDebugSteps(String.format("y:%s,x:%s",getY(candidate),getX(candidate)));
            addPiece(candidate, false);
            completionService.submit(asyncVCX(depth));
            removePiece(candidate, false);
        }
        // 对手只要任何一个算杀不成功，我们都算防御住了，防御住了, return false
        boolean res = !anyVCXFail(threadPool, completionService, candidates.size());
        debugContext.debugInfoWithNonEmptyDebugSteps("结果:" + res);
        return res;
    }

    private boolean anyVCXFail(ExecutorService threadPool, CompletionService<Boolean> completionService, int taskCount) {
        try {
            boolean anyVCXFail = false;
            int received = 0;

            while (received < taskCount) {
                Future<Boolean> future = completionService.take();
                boolean vcxSuccess = future.get();
                received++;
                if (!vcxSuccess) {
                    anyVCXFail = true;
                    break;
                }
            }
            return anyVCXFail;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            threadPool.shutdownNow();
        }
    }

    private Callable<Boolean> asyncVCX(int depth) {
        VCX tmp = new VCX(chessboardAlgo.clone(), humanColor, depth);
        VCX tmp2 = new VCX(chessboardAlgo.clone(), humanColor, Math.min(21, depth - 6), VCXOptimization.SLOW);
        return () -> (tmp.aiFindPos() != Position.EMPTY || tmp2.aiFindPos() != Position.EMPTY);
    }

    private static void loadVCXManager(InputStream resourceAsStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] ss = line.split(",");
                List<Position> input = BoardToString.strDecode(ss[0]);
                String key = canonical(input);
                VCXManager2Depth.put(key, Integer.parseInt(ss[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
