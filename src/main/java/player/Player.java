package player;

import UI.Chessboard;
import UI.Piece;
import common.Position;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
public enum Player {
    WHITE(Color.white, 1), BLACK(Color.black, 2);

    private Color color;
    private int id;

    @Setter @Getter
    private IPlayStrategy playStrategy;

    Player(Color color, int id) {
        this.color = color;
        this.id = id;
    }

    public Player doSwitch() {
        return this == WHITE ? BLACK : WHITE;
    }

    public static int enemyColor(int role) {
        assert role == 1 || role == 2;
        return 3 - role;
    }

    public boolean play(Piece piece, Chessboard chessboard, int previousGameId) {
        if (playStrategy == null) throw new IllegalStateException("invalid area, miss playStrategy");
        if (piece == null) return false;
        if (chessboard.addPiece(piece, previousGameId)) {
            for (Player p : Player.values()) {
                p.getPlayStrategy().setPieceCallback(piece, getPlayStrategy().isAI());
            }
            return true;
        }
        return false;
    }

    public Piece decidePieceIfFailReturnNull() {
        if (playStrategy == null) throw new IllegalStateException("invalid area, miss playStrategy");
        Position pos = playStrategy.decidePiecePos();
        if (pos == Position.EMPTY) return null;
        return new Piece(pos.x, pos.y, this);
    }
}
