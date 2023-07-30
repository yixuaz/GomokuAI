package vcx;

import UI.Piece;
import aialgo.vcx.VCX;
import aialgo.vcx.VCXOptimization;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;

public class VCXTest {
    public static void debugInit(IChessboardAlgo chessboard, String input) {
        //"H8 I7 I9 G7 J9 G8 J7 G9 G6 J8 K9 L9 ";//H8 I8 I9 Ja G7 H9 H7 J7 Ga J8 J9 I7 H6 I5 I6
        // _h8_i8_i7_j6_g9_h7_f10_g8
        String[] steps = input.split(" ");
        char[] m = new char[256];
        for (int i = 1; i <= 6; i++) m['a' + i - 1] = (char) ('9' + i);
        for (char i = '1'; i <= '9'; i++) m[i] = i;
        Player curP = Player.BLACK;
        for (String step : steps) {
            char x = step.charAt(0), y = m[step.charAt(1)];
            chessboard.setPiece(new Piece(x - 'A', '9' + 6 - y, curP));
            curP = curP.doSwitch();
        }

        // chessboard.addPiece(new Piece(8,5, Player.WHITE));

    }

    public boolean testVCXTemplate(String input, int depth) {
        return testVCXTemplate(input, depth, VCXOptimization.FAST);
    }

    public boolean testVCXTemplate(String input, int depth, VCXOptimization optimization) {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgo(15);
        debugInit(chessboard, input);
        String res = chessboard.generateStepsCode();
        int human = ((res.length() + 1) / 3) % 2 == 1 ? Player.BLACK.getId() : Player.WHITE.getId();
        VCX vcx = new VCX(chessboard, human, depth, optimization);
        return vcx.aiFindPos() != Position.EMPTY;
    }

    @Test
    public void testCase1() {
        Assert.assertTrue(testVCXTemplate("H8 I8 I9 Ja G7 H9 H7 Ga J7 I7 I6 Ec Fb Ia Ha G8 Jb H6 J8 K7 F7 E7 ", 27));
    }

    @Test
    public void testCase2() { // 需要不用Z字优化，才能找到解
        Assert.assertFalse(testVCXTemplate("H8 I8 I9 Ja G7 H9 H7 F6 H6 F8 G8 F7 ", 25));
    }

    @Test
    public void testCase3() { // 需要不用Z字优化，才能找到解
        Assert.assertTrue(testVCXTemplate("H8 I8 I9 Ja G7 H9 H7 F6 H6 F8 G8 F7 ", 25, VCXOptimization.SLOW));
        Assert.assertTrue(testVCXTemplate("H8 I8 I9 G7 J9 K9 Ha J8 Hb H9 Ia K8 Ga Ja Ea Fa Ib L8 M8 M7 N6 F9", 7));
    }
    @Test
    public void testCase4() {
        Assert.assertTrue(testVCXTemplate("H8 I8 I9 G7 J9 K9 Ha H9 Ga J8 Ia Ja Hb K8 Fa Ea Gc Fd L8",13));
        // Assert.assertTrue(testVCXTemplate("H8 I7 I9 G7 J9 G8 J7 Ga", 23, VCXOptimization.SLOW));
        Assert.assertTrue(testVCXTemplate("H8 J7 G7 I8 I9 Ja G9 G6 H7 J8 J9 H9 Ga I6 ", 27));
        Assert.assertTrue(testVCXTemplate("H8 K8 F8 I8 E9 I6 G7 H6 I9 F6 G6 G8 H9 G9 H7 Hb ", 27));
        Assert.assertTrue(testVCXTemplate("H8 K8 F8 I8 E9 I6 G7 H6 I9 F6 G6 G8 H9 G9 H7 Hb F7 E7 ", 27));

    }


}