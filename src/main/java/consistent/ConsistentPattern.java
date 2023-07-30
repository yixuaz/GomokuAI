package consistent;

import common.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsistentPattern {

    public static Position findBestMove(List<Position> shape, Map<String, Position> patchedKey) {
        return findBestMoveInternal(shape.stream().map(i -> new int[]{i.x, i.y}).collect(Collectors.toList()), patchedKey);
    }

    private static Position findBestMoveInternal(List<int[]> shape, Map<String, Position> patchedKey) {
        int[] blackXs = new int[(shape.size() + 1) >> 1];
        int[] blackYs = new int[(shape.size() + 1) >> 1];
        int[] whiteXs = new int[shape.size() >> 1];
        int[] whiteYs = new int[shape.size() >> 1];
        int[] blackOuts = new int[blackXs.length];
        int[] whiteOuts = new int[whiteXs.length];

        for (int rotateType = 0; rotateType < 8; ++rotateType) {
            // 先旋转
            fulfillXYs(shape, rotateType, blackXs, blackYs, whiteXs, whiteYs);
            // 再位移
            int[] myx = fulfillOuts(blackXs, blackYs, whiteXs, whiteYs, blackOuts, whiteOuts);

            String candidate = getString(blackOuts, whiteOuts);
            if (!patchedKey.containsKey(candidate)) continue;
            Position bestRawMovePos = patchedKey.get(candidate);
            int[] bestRawMove = new int[]{bestRawMovePos.x, bestRawMovePos.y};
            // 先位移
            bestRawMove[0] += myx[0];
            bestRawMove[1] += myx[1];
            // 再旋转
            deRotate(rotateType, bestRawMove);

            return new Position(bestRawMove[1], bestRawMove[0]);
        }
        return null;
    }

    private static void rotate(int rotateType, int[] bestRawMove) {
        //x y, x -y, -x y, -x -y, y x, y -x, -y x, -y -x
        int x = bestRawMove[0], y = bestRawMove[1];
        int nx = rotateType <=1 ? x : rotateType <=3 ? -x : rotateType <=5 ? y : -y;
        int ny = rotateType <=3 ? (rotateType %2==0 ? y : -y) : (rotateType %2==0 ? x : -x);
        bestRawMove[0] = nx;
        bestRawMove[1] = ny;
    }

    private static void deRotate(int rotateType, int[] bestRawMove) {
        //after: x y, x -y, -x y, -x -y, y x, y -x, -y x, -y -x
        //apply: x y, x -y, -x y, -x -y, y x, -y x, y -x, -y -x
        //res  : x y, x  y,  x y,  x  y, x y,  x y, x  y, x  y
        int x = bestRawMove[0], y = bestRawMove[1];
        int nx = rotateType <=1 ? x : rotateType <=3 ? -x : rotateType %2 == 0 ? y : -y;
        int ny = rotateType <=3 ? (rotateType %2==0 ? y : -y) : (rotateType <= 5 ? x : -x);
        bestRawMove[0] = nx;
        bestRawMove[1] = ny;
    }

    public static String canonical(List<Position> shape) {
        return canonicalInternal(shape.stream().map(i -> new int[]{i.x, i.y}).collect(Collectors.toList()), new int[2]);
    }

    public static String canonical(List<Position> shape, int[] bestXandY) {
        return canonicalInternal(shape.stream().map(i -> new int[]{i.x, i.y}).collect(Collectors.toList()), bestXandY);
    }

    private static String canonicalInternal(List<int[]> shape, int[] bestXandY) {
        String ans = "";
        int[] blackXs = new int[(shape.size() + 1) >> 1];
        int[] blackYs = new int[(shape.size() + 1) >> 1];
        int[] whiteXs = new int[shape.size() >> 1];
        int[] whiteYs = new int[shape.size() >> 1];
        int[] blackOuts = new int[blackXs.length];
        int[] whiteOuts = new int[whiteXs.length];
        int[] tmpBest = bestXandY.clone();
        for (int rotateType = 0; rotateType < 8; ++rotateType) {
            fulfillXYs(shape, rotateType, blackXs, blackYs, whiteXs, whiteYs);

            int[] myx = fulfillOuts(blackXs, blackYs, whiteXs, whiteYs, blackOuts, whiteOuts);

            String candidate = getString(blackOuts, whiteOuts);
            if (ans.compareTo(candidate) < 0) {
                ans = candidate;
                int[] tmp = bestXandY.clone();
                rotate(rotateType, tmp);
                tmp[0] -= myx[0];
                tmp[1] -= myx[1];
                tmpBest = tmp;
            }
        }
        bestXandY[0] = tmpBest[0];
        bestXandY[1] = tmpBest[1];
        return ans;
    }

    private static String getString(int[] blackOuts, int[] whiteOuts) {
        String candidate = new StringBuilder(Arrays.toString(blackOuts)).append(":").append(Arrays.toString(whiteOuts)).toString() ;
        return candidate;
    }

    private static int[] fulfillOuts(int[] blackXs, int[] blackYs, int[] whiteXs, int[] whiteYs, int[] blackOuts, int[] whiteOuts) {
        int mx = blackXs[0], my = blackYs[0];
        for (int x: blackXs) mx = Math.min(mx, x);
        for (int x: whiteXs) mx = Math.min(mx, x);

        for (int y: blackYs) my = Math.min(my, y);
        for (int y: whiteYs) my = Math.min(my, y);

        for (int j = 0; j < blackOuts.length; j++) {
            blackOuts[j] = (blackYs[j] - my) * 15 + (blackXs[j] - mx);
        }
        for (int j = 0; j < whiteOuts.length; j++) {
            whiteOuts[j] = (whiteYs[j] - my) * 15 + (whiteXs[j] - mx);
        }
        Arrays.sort(blackOuts);
        Arrays.sort(whiteOuts);
        return new int[]{mx, my};
    }


    private static void fulfillXYs(List<int[]> shape, int c, int[] blackXs,  int[] blackYs, int[] whiteXs, int[] whiteYs) {
        int t = 0;
        for (int i = 0; i < shape.size(); i += 2) {
            int[] z = shape.get(i);
            int x = z[0], y = z[1];
            //x y, x -y, -x y, -x -y, y x, y -x, -y x, -y -x
            blackXs[t] = c <=1 ? x : c <=3 ? -x : c <=5 ? y : -y;
            blackYs[t++] = c <=3 ? (c %2==0 ? y : -y) : (c %2==0 ? x : -x);
        }
        t = 0;
        for (int i = 1; i < shape.size(); i += 2) {
            int[] z = shape.get(i);
            int x = z[0], y = z[1];
            //x y, x -y, -x y, -x -y, y x, y -x, -y x, -y -x
            whiteXs[t] = c <=1 ? x : c <=3 ? -x : c <=5 ? y : -y;
            whiteYs[t++] = c <=3 ? (c %2==0 ? y : -y) : (c %2==0 ? x : -x);
        }
    }
}
