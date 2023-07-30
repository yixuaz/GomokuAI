package common;

import java.util.Objects;

public final class Position {
    public static Position EMPTY = null;
    public final int y;
    public final int x;
    public final boolean winning;

    public Position(int y, int x) {
        this.y = y;
        this.x = x;
        winning = false;
    }

    public Position(int y, int x, boolean winning) {
        this.y = y;
        this.x = x;
        this.winning = winning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return y == position.y && x == position.x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(y, x);
    }

    @Override
    public String toString() {
        return "(" +
                "y=" + y +
                ", x=" + x +
                ", w=" + winning +
                ')';
    }
}
