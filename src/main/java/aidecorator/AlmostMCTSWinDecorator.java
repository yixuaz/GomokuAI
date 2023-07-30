package aidecorator;

import aialgo.IAIAlgo;
import aialgo.almostwin.AlmostMCTSWin;
import common.Position;
import lombok.Getter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static common.ThreadPoolContext.threadPool;

public class AlmostMCTSWinDecorator extends AIAlgoDecorator {
    @Getter
    private boolean almostMCTSWin = false;
    private AlmostMCTSWin almostMCTSWinAlgo;
    private int eachChdTimeLimitMs;
    private int depth;
    public AlmostMCTSWinDecorator(IAIAlgo iaiAlgo, int eachChdTimeLimitMs, int depth) {
        super(iaiAlgo);
        this.eachChdTimeLimitMs = eachChdTimeLimitMs;
        this.depth = depth;
    }

    @Override
    public Position aiFindPos() {
        AlmostMCTSWin tmp;
        if (!almostMCTSWin)
            tmp = new AlmostMCTSWin(chessboardAlgo.clone(), humanColor, eachChdTimeLimitMs, depth);
        else
            tmp = almostMCTSWinAlgo;
        return runInParallel(() -> decoratedAIAlgo.aiFindPos(),
                new WinningAlgoTask(
                        () -> tmp.aiFindPos(),
                        result -> {
                            if (result != Position.EMPTY) {
                                almostMCTSWin = true;
                                almostMCTSWinAlgo = tmp;
                                System.out.println("MCTS逼近算杀");
                            } else {
                                System.out.println("MCTS逼近算杀失败");
                                almostMCTSWin = false;
                            }
                        })
        );
    }

    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        if (almostMCTSWinAlgo != null)
            almostMCTSWinAlgo.setPieceCallBack(y, x, player, isAI);
        decoratedAIAlgo.setPieceCallBack(y, x, player, isAI);
    }

    @Override
    public boolean isWinning() {
        return almostMCTSWin || super.isWinning();
    }
}
