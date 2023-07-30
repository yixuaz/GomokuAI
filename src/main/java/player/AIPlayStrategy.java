package player;


import UI.IUICallback;
import UI.MessagingPanel;
import UI.Piece;
import aialgo.IAIAlgo;
import aidecorator.ConsistentDecorator;
import common.Position;
import consistent.IConsistentAlgo;

public class AIPlayStrategy implements IPlayStrategy {
    private IAIAlgo aiAlgo;
    private IUICallback uiCallback;
    public AIPlayStrategy(IAIAlgo aiAlgo, IUICallback uiCallback) {
        if (aiAlgo.getChessboardAlgo() instanceof IConsistentAlgo && (! (aiAlgo instanceof IConsistentAlgo))) {
            this.aiAlgo = new ConsistentDecorator(aiAlgo);
        } else {
            this.aiAlgo = aiAlgo;
        }
        this.uiCallback = uiCallback;
    }

    @Override
    public Position decidePiecePos() {
        if (uiCallback != null)
            uiCallback.onAIThinkingStart();
        Position res = aiAlgo.aiFindPos();
        if (uiCallback != null)
            uiCallback.onAIThinkingDone();

        return res;
    }

    @Override
    public void setPieceCallback(Piece piece, boolean isAI) {
        aiAlgo.setPieceCallBack(piece.getY(), piece.getX(), piece.getPlayer().getId(), isAI);
    }

    @Override
    public boolean isAI() {
        return true;
    }


}
