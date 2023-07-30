package almostwin;

import aialgo.almostwin.AlmostMCTSWin;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;

import static vcx.VCXTest.debugInit;

public class AlmostMCTSWinTest {

    public Position testAlmostMCTSTemplate(String input, int time, int depth) {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgo(15);
        debugInit(chessboard, input);
        chessboard.generateStepsCode();
        int human = Player.WHITE.getId();

        AlmostMCTSWin apns = new AlmostMCTSWin(chessboard, human, time, depth);
        Position res = apns.aiFindPos();
        return res;
    }

    @Test
    public void testCase1() {
        Assert.assertEquals(
                new Position(3, 6),
                testAlmostMCTSTemplate("H8 I8 I9 G7 J9 K9 Ha H9 Ga J8 Ia Ja ", 2500, 9)
        );
    }

}