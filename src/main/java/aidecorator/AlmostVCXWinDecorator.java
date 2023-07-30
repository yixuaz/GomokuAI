package aidecorator;

import aialgo.IAIAlgo;
import aialgo.almostwin.AlmostVCXWin;
import common.Position;
import lombok.Getter;
import scorecalculator.Score;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static common.ThreadPoolContext.threadPool;

public class AlmostVCXWinDecorator extends AIAlgoDecorator {
    @Getter
    private boolean almostVCXWin = false;
    private int depth;
    public AlmostVCXWinDecorator(IAIAlgo iaiAlgo, int depth) {
        super(iaiAlgo);
        this.depth = depth;
    }

    @Override
    public Position aiFindPos() {
        if (almostVCXWin) {
            // 对面在做冲4，只要交给MINMAX 防掉冲4，之后VCX可算杀
            System.out.println("almostVCXWin");
            return decoratedAIAlgo.aiFindPos();
        }
        AlmostVCXWin vcx = new AlmostVCXWin(chessboardAlgo.clone(), humanColor, depth);
        return runInParallel(() -> decoratedAIAlgo.aiFindPos(),
                new WinningAlgoTask(
                        () -> vcx.aiFindPos(),
                        result -> {
                            if (result != Position.EMPTY) {
                                almostVCXWin = true;
                                System.out.println("逼近算杀");
                            } else {
                                System.out.println("逼近算杀失败");
                            }
                        }));

    }

    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        if (almostVCXWin && player == humanColor &&
                (scoreManager.getScore(y, x, player) < Score.BLOCKED_FOUR.value
                 && scoreManager.getScore(y, x, 3 - player) < Score.FOUR.value)) {
            System.out.println("逼近算杀bug");
            almostVCXWin = false;
        }
        super.setPieceCallBack(y, x, player, isAI);
    }

    @Override
    public boolean isWinning() {
        return almostVCXWin || super.isWinning();
    }
}
