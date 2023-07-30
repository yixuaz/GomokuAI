package aidecorator;

import aialgo.IAIAlgo;
import common.Position;
import consistent.IConsistentAlgo;
import consistent.PositionTranslator;
import lombok.Getter;

public class ConsistentDecorator extends AIAlgoDecorator implements IConsistentAlgo {
    @Getter
    protected PositionTranslator positionTranslator;

    public ConsistentDecorator(IAIAlgo iaiAlgo) {
        super(iaiAlgo);
    }

    @Override
    public Position aiFindPos() {
        setPositionTranslatorIfNull();
        Position res = decoratedAIAlgo.aiFindPos();
        return positionTranslator == null ? res : positionTranslator.originDeTranslateThenOrigin(res);
    }

    @Override
    public void setPieceCallBack(int y, int x, int player, boolean isAI) {
        setPositionTranslatorIfNull();
        if (positionTranslator == null) {
            super.setPieceCallBack(y, x, player, isAI);
            return;
        }
        Position position = positionTranslator.originTranslateThenOrigin(y, x);
        super.setPieceCallBack(position.y, position.x, player, isAI);
    }

    protected void setPositionTranslatorIfNull() {
        if (positionTranslator != null) return;
        positionTranslator = ((IConsistentAlgo) chessboardAlgo).getPositionTranslator();
    }
}
