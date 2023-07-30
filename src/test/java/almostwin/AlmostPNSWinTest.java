package almostwin;

import aialgo.almostwin.AlmostPNSWin;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;

import static vcx.VCXTest.debugInit;

public class AlmostPNSWinTest {
    public Position testAlmostPNSTemplate(String input, int time, int count, int depth) {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgo(15);
        debugInit(chessboard, input);
        chessboard.generateStepsCode();
        int human = Player.WHITE.getId();

        AlmostPNSWin apns = new AlmostPNSWin(chessboard, human, time, count, depth);
        Position res = apns.aiFindPos();
        return res;
    }

    @Test
    public void testCase1() {
        Assert.assertEquals(
                new Position(4, 7),
                testAlmostPNSTemplate("H8 I8 I9 G7 J9 K9 Ha H9 Ga J8 Ia Ja ", 600_000, 180_000, 9)
        );
    }
}