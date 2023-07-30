package aidecorator;

import aialgo.IAIAlgo;
import chessboardalgo.IChessboardAIAlgo;
import common.Position;
import common.ThreadPoolContext;
import lombok.Getter;
import scorecalculator.IScoreManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class AIAlgoDecorator implements IAIAlgo {
    protected IAIAlgo decoratedAIAlgo;
    @Getter
    protected IChessboardAIAlgo chessboardAlgo;
    @Getter
    protected int humanColor;
    @Getter
    protected IScoreManager scoreManager;

    public AIAlgoDecorator(IAIAlgo iaiAlgo){
        this.decoratedAIAlgo = iaiAlgo;
        chessboardAlgo = iaiAlgo.getChessboardAlgo();
        humanColor = iaiAlgo.getHumanColor();
        scoreManager = iaiAlgo.getScoreManager();
    }

    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        decoratedAIAlgo.setPieceCallBack(y, x, player, isAI);
    }

    public boolean isWinning() {
        if (decoratedAIAlgo instanceof AIAlgoDecorator) {
            return ((AIAlgoDecorator) decoratedAIAlgo).isWinning();
        }
        return false;
    }

    protected Position runInParallel(Callable<Position> finalResult, WinningAlgoTask... potentialWin) {
        int received = 0, taskCount = 1 + potentialWin.length;
        // important, since interrupt will not clear the queue, if it is a class level object, the interrupted task will be take() next time
        CompletionService<Position> completionService = new ExecutorCompletionService<>(ThreadPoolContext.threadPool);
        Future<Position> finalRes = completionService.submit(finalResult);
        Map<Future<Position>, Consumer<Position>> task2Callback = new HashMap<>();
        task2Callback.put(finalRes, DO_NOTHING);
        for (WinningAlgoTask i : potentialWin) {
            task2Callback.put(completionService.submit(i.winningTask), i.winningCallback);
        }
        Position end = Position.EMPTY;
        try {
            //System.out.println(getClass().getSimpleName() + " taskCount: " + taskCount);
            while (received < taskCount) {
                Future<Position> future = completionService.take();
                if (future.isCancelled()) {
                    //System.out.println(getClass().getSimpleName() + " cancelled: " + future);
                    received++;
                    continue;
                }
                Position result = future.get();
                task2Callback.get(future).accept(result);
                received++;
                if (result != Position.EMPTY && result.winning) {
                    //System.out.println(getClass().getSimpleName() + " winning: " + result);
                    end = result;
                    break;
                }
            }
            if (end == Position.EMPTY) {
                end = finalRes.get();
            }
        } catch (InterruptedException e) {
            //System.out.println(getClass().getSimpleName() + " interrupt error");
            // ignore
        } catch (Exception e) {
            System.err.println(getClass().getSimpleName() + " result error");
            e.printStackTrace();
        }
        //System.out.println(getClass().getSimpleName() + " cancel ");
        task2Callback.keySet().forEach(i -> i.cancel(true));
        //System.out.println(getClass().getSimpleName() + " return " + end);
        return end;
    }
    private static Consumer<Position> DO_NOTHING = pos -> {};
    class WinningAlgoTask {

        Callable<Position> winningTask;
        Consumer<Position> winningCallback;

        public WinningAlgoTask(Callable<Position> winningTask, Consumer<Position> winningCallback) {
            this.winningTask = winningTask;
            this.winningCallback = winningCallback;
        }
    }
}
