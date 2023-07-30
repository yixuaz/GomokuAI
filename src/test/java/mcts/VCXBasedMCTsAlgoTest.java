package mcts;

import aialgo.mcts.VCXBasedMCTsAlgo;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;

import java.util.Set;

import static common.PositionConverter.convertToIdx;
import static vcx.VCXTest.debugInit;

public class VCXBasedMCTsAlgoTest {
    private int size = 15;
    public int testTemplate(int testNum, String input, Set<Integer> validAns, int timeMs, int vcxDepth) {
        int expected = 0;
        System.out.println("testing intput: " + input);
        for (int i = 0; i < testNum; i++) {
            IChessboardAlgo board = new ChessboardByteArrayAlgo(size);
            debugInit(board, input);
            int step = (input.length() + 1) / 3;
            Position res = new VCXBasedMCTsAlgo(board, step % 2 == 0 ? Player.WHITE.getId() : Player.BLACK.getId(), timeMs, vcxDepth).aiFindPos();
            if (validAns.contains(convertToIdx(res.y, res.x, board.getSize()))) {
                expected++;
            } else {
                System.out.println("not match the intput: ");
                System.out.println("y=" + res.y + ", x=" + res.x);
            }
        }
        return expected;
    }
    public int testTemplate(int testNum, String input, Set<Integer> validAns) {
        return testTemplate(testNum, input, validAns, 15_000, 7);
    }

    private int getIdx(int x, int y) {
        return y * size + x;
    }

    @Test // 15
    public void test1() {
        int res = testTemplate(1, "H8 I8 I9 Ja G7 H9 H7 I7", Set.of(getIdx(6, 7)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void test2() {
        int res = testTemplate(1, "H8 I8 I9 Ja G7 H9 H7 F6", Set.of(getIdx(6,7)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase19_0() {
        int res = testTemplate(1, "H8 I8 I9 Ja G7 H9 H7 H6", Set.of(getIdx(6,7)));
        Assert.assertEquals(1, res);
    }

    @Test // 25
    public void testCase11() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 G8 J7 H9 J8 J6", Set.of(getIdx(7,5)), 25_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 25
    public void testCase20() {
        int res = testTemplate(1, "H8 I8 I9 H9 Ga Ha Gb G7 ", Set.of(getIdx(7,4)));
        Assert.assertEquals(1, res);
    }



    @Test // 25
    public void testCase10() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 G8 J7 G9 G6 J8 H6 H5", Set.of(getIdx(8,5),getIdx(9,5)));
        Assert.assertEquals(1, res);
    }

    @Test // 25
    public void testCase12() {
        int res = testTemplate(1, "H8 I8 I9 H9 Ga Ha Gb Hc Hb G9", Set.of(getIdx(6,3)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase24() {
        int res = testTemplate(1, "H8 I8 I9 G7 J9 K9 Ha H9", Set.of(getIdx(6,5)));
        Assert.assertEquals(1, res);
    }

    @Test // 45
    public void testCase27() {
        int res = testTemplate(1, "H8 I7 I9 Ja G8 J8 H6 J7 J9 H9", Set.of(getIdx(6,8)), 45_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase28() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 H9 J8 J7 K7 L6 I8 K8", Set.of(getIdx(8,4)));
        Assert.assertEquals(1, res);
    }

    @Test // 25
    public void testCase31() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 G9 J8 G8", Set.of(getIdx(6,5)));
        Assert.assertEquals(1, res);
    }

    @Test // 25 sec -> 11
    public void testCase32() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 G9 J8 G8 Ga J7 H7 Ha", Set.of(getIdx(10,7),getIdx(8,4)), 25_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase34() {
        int res = testTemplate(1, "H8 I8 I9 Ga H9 J9 Ja G7", Set.of(getIdx(10,5)));
        Assert.assertEquals(1, res);
    }
    @Test // 15
    public void testCase36() {
        int res = testTemplate(1, "H8 I6 I9 G7 H9 H7 G9 F9 G8 F8 ", Set.of(getIdx(8,8)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase37() {
        int res = testTemplate(1, "H8 I6 I9 G8 G7 F6 H7 H6 G6 F7 ", Set.of(getIdx(7,5)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase38() {
        int res = testTemplate(1, "H8 I6 I9 H7 G7 F6 G9 J9 ", Set.of(getIdx(8,7)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCase42() {
        int res = testTemplate(1, "H8 J8 F8 F9 G9 Ha", Set.of(getIdx(6,8)));
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCasegp1() {
        int res = testTemplate(1, "H8 I8 I9 J9 Ja Kb H7 G7", Set.of(getIdx(7,5)), 15_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCasegp2() {
        int res = testTemplate(1, "H8 I8 I9 Ja G7 H7 G6 F6", Set.of(getIdx(6,6)), 15_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 45
    public void testCasegp3() {
        int res = testTemplate(1, "H8 I7 I9 H9 G7 F6 Ha Gb", Set.of(getIdx(9,5)), 45_000, 15);
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCasegp4() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 J7 H7 H9 I8 Ka J8 K8 K7 L6", Set.of(getIdx(8,4)), 15_000, 13);
        Assert.assertEquals(1, res);
    }

    @Test // 15
    public void testCasegp6() {
        int res = testTemplate(1, "H8 I7 I9 G7 J9 J7 H7 H9 I8 Ka J8 K8", Set.of(getIdx(10,8)), 45_000, 15);
        Assert.assertEquals(1, res);
    }

}