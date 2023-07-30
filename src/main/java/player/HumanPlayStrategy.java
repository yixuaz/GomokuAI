package player;

import UI.Piece;
import common.Position;

public class HumanPlayStrategy implements IPlayStrategy {
    public static HumanPlayStrategy INSTANCE = new HumanPlayStrategy();
    private HumanPlayStrategy() {
    }

    @Override
    public Position decidePiecePos() {
        // cannot decide, wait UI
        return Position.EMPTY;
    }

    @Override
    public void setPieceCallback(Piece piece, boolean isAI) {
        // do nothing
    }

    @Override
    public boolean isAI() {
        return false;
    }
}
