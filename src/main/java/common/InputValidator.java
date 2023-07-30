package common;

public class InputValidator {
    public static void checkColorValid(int color) {
        if (color != 1 && color != 2) throw new IllegalArgumentException("invalid color " + color);
    }
}
