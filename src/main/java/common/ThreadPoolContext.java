package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolContext {
    public static ExecutorService threadPool = Executors.newFixedThreadPool(16);
    public static void waitActiveThreadNumberLowerEqualThan(int threshold) {
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) threadPool;
        while (tpe.getActiveCount() > threshold) Thread.yield();
    }
}
