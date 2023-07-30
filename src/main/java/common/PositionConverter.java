package common;

public class PositionConverter {
    private PositionConverter() {}
    public static int convertToIdx(int y, int x, int size) {
        return y * size + x;
    }

    public static Position convertToPos(int idx, int size) {
        return new Position(getY(idx, size), getX(idx, size));
    }

    public static int getX(int idx, int size) {
        return idx % size;
    }

    public static int getY(int idx, int size) {
        return idx / size;
    }
}
