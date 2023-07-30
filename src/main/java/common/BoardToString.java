package common;

import chessboardalgo.IChessboardAlgo;

import java.util.*;

public class BoardToString {
    private static final char[] m = new char[256];
    private static char[] X = new char[15];
    private static char[] Y = new char[15];
    private static Map<String, Integer> patch = new HashMap<>();
    static {
        for (int i = 1; i <= 6; i++) m['a' + i - 1] = (char) ('9' + i);
        for (char i = '1'; i <= '9'; i++) m[i] = i;
        for (int i = 0; i < 15; i++) X[i] = (char) ('A' + i);
        for (int i = 0; i <= 5; i++) Y[i] = (char) ('f' - i);
        for (int i = 6; i < 15; i++) Y[i] = (char) ('0' + (15 - i));
    }

    public static String serialize(Deque<Position> steps) {
        StringBuilder sb = new StringBuilder();
        for (Position res : steps) {
            sb.append(X[res.x]).append(Y[res.y]).append(" ");
        }
        return sb.toString();
    }

    public static List<Position> strDecode(String s) {
        List<Position> res = new ArrayList<>();
        String[] steps = s.split(" ");
        for (String step : steps) {
            if (step.length() == 0) continue;
            char x = step.charAt(0), y = m[step.charAt(1)];
            res.add(new Position('9' + 6 - y, x - 'A'));
        }
        return res;
    }
}
