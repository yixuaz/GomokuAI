package common;

import java.util.List;
import java.util.function.Predicate;

public class DebugContext {
    public static DebugContext DISABLE = new DebugContext(false, List.of());
    public static DebugContext ENABLE = new DebugContext(true, List.of());
    public boolean enableDebug;
    public List<Integer> debugSteps;

    public DebugContext(boolean enableDebug, List<Integer> debugSteps) {
        this.enableDebug = enableDebug;
        this.debugSteps = debugSteps;
    }

    public void debugStartInfo(int depth, int firstDepth) {
        if (enableDebug && depth == firstDepth - debugSteps.size()) {
            System.out.println("第" + (debugSteps.size() + 1) + "层结果：");
        }
    }

    public void debugInfo(String info) {
        if (enableDebug) {
            System.out.println(info);
        }
    }

    public void debugInfoWithNonEmptyDebugSteps(String info) {
        if (enableDebug && !debugSteps.isEmpty()) {
            System.out.println(info);
        }
    }

    public boolean isInDebugStep(int depth, int firstDepth, int pos) {
        if (enableDebug && debugSteps.size() > firstDepth - depth) {
            return debugSteps.get(firstDepth - depth) == pos;
        }
        return true;
    }

    public void debugResultInfo(int depth, int y, int x, int firstDepth, String result) {
        if (enableDebug && depth == firstDepth - debugSteps.size()) {
            System.out.println(String.format("y:%s, x:%s, %s",y, x, result));
        }
    }
}
