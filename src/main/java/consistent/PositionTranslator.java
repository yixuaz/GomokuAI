package consistent;

import UI.Piece;
import common.Position;

public enum PositionTranslator {
    CLOCKWISE_0{
        @Override
        Position translate(int y, int x) {
            return new Position(y, x);
        }

        @Override
        Position deTranslate(int y, int x) {
            return CLOCKWISE_0.translate(y, x);
        }
    }, CLOCKWISE_90{
        @Override
        Position translate(int y, int x) {
            return new Position(-x, y);
        }

        @Override
        Position deTranslate(int y, int x) {
            return CLOCKWISE_270.translate(y, x);
        }
    }, CLOCKWISE_180{
        @Override
        Position translate(int y, int x) {
            return new Position(-y,-x);
        }

        @Override
        Position deTranslate(int y, int x) {
            return CLOCKWISE_180.translate(y, x);
        }
    }, CLOCKWISE_270{
        @Override
        Position translate(int y, int x) {
            return new Position(x, -y);
        }

        @Override
        Position deTranslate(int y, int x) {
            return CLOCKWISE_90.translate(y, x);
        }
    };

    abstract Position translate(int y, int x);
    abstract Position deTranslate(int y, int x);

    public static int toDeltaX(int x) {
        return x - 7;
    }
    public static int toDeltaY(int y) {
        return 7 - y;
    }
    public static int toOriginX(int dx) {
        return dx + 7;
    }
    public static int toOriginY(int dy) {
        return 7 - dy;
    }

    public Position originDeTranslateThenOrigin(Position p) {
        Position res = deTranslate(toDeltaY(p.y), toDeltaX(p.x));
        return new Position(toOriginY(res.y), toOriginX(res.x), p.winning);
    }

    public Position originTranslateThenOrigin(int y, int x) {
        Position res = translate(toDeltaY(y), toDeltaX(x));
        return new Position(toOriginY(res.y), toOriginX(res.x));
    }

    public Piece originTranslateThenOrigin(Piece p) {
        int y = p.getY(), x = p.getX();
        Position res = originTranslateThenOrigin(y, x);
        return new Piece(res.x, res.y, p.getPlayer());
    }

    public String translateToString(int dy, int dx) {
        Position pos = translate(dy, dx);
        return toString(toOriginY(pos.y) , toOriginX(pos.x));
    }

    public static String toString(int y, int x) {
        assert y >= 0 && y < 15;
        assert x >= 0 && x < 15;
        String ys = (y + 1) + "";
        char xs = (char) ('a' + x);
        return xs + ys;
    }

    public static PositionTranslator select(int dy, int dx) {
        if (dx > 0 && dy <= 0) return CLOCKWISE_0;
        else if (dx >= 0 && dy > 0) return CLOCKWISE_90;
        else if (dx < 0 && dy >= 0) return CLOCKWISE_180;
        else if (dx <= 0 && dy < 0) return CLOCKWISE_270;
        return CLOCKWISE_0;
    }

    public static PositionTranslator selectByOrigin(int y, int x) {
        return select(toDeltaY(y), toDeltaX(x));
    }
}
