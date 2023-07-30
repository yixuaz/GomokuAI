package aidecorator;

import aialgo.IAIAlgo;
import common.Position;
import aialgo.vcx.VCX;

import java.util.concurrent.Future;

public class VCXDecorator extends AIAlgoDecorator {
    public VCX mustWin = null;
    private int vcxDepth;

    public VCXDecorator(IAIAlgo decoratedAIAlgo, int vcxDepth) {
        super(decoratedAIAlgo);
        this.vcxDepth = vcxDepth;
    }

    @Override
    public Position aiFindPos() {
        if (mustWin != null) {
            Position pos = mustWin.aiFindPos();
            if (pos == Position.EMPTY) {
                System.out.println("vcx has bug");
                chessboardAlgo.generateStepsCode();
                mustWin = null;
                scoreManager.initScore();
                return decoratedAIAlgo.aiFindPos();
            }
            return pos;
        }

        VCX vcx = new VCX(chessboardAlgo.clone(), humanColor, vcxDepth);

        Position res = runInParallel(() -> decoratedAIAlgo.aiFindPos(),
                new WinningAlgoTask(() -> vcx.aiFindPos(),
                        pos -> {
                            if (pos != Position.EMPTY) {
                                mustWin = new VCX(chessboardAlgo, humanColor, vcxDepth - 2);
                                System.out.println("算杀必胜");
                            } else {
                                System.out.println("算杀失败");
                            }
                        }));
        System.out.println("vcx calculation finished");
        return res;
    }

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        if (mustWin != null) {
            mustWin.setPieceCallBack(y,x,player, isAI);
        }
        decoratedAIAlgo.setPieceCallBack(y, x, player, isAI);
    }

    @Override
    public boolean isWinning() {
        return mustWin != null || super.isWinning();
    }
}
