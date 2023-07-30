package scorecalculator;

public enum Direction {
    HORIZONTAL(0,1), VERTICAL(1,0), DIAGONAL(1,1), ANTI_DIAGONAL(1,-1);
    public final int dy, dx;
    Direction(int dy, int dx) {
        this.dy = dy;
        this.dx = dx;
    }
    public boolean match(Direction other) {
        if (other == null) return true;
        return other == this;
    }
}
