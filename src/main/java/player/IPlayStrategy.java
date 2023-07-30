package player;

import UI.Piece;
import common.Position;

public interface IPlayStrategy {

    Position decidePiecePos();

    void setPieceCallback(Piece piece, boolean isAI);

    boolean isAI();
}
