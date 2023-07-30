package almostwin;

import aialgo.almostwin.AlmostVCXWin;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;
import aialgo.vcx.VCX;

import static org.junit.Assert.*;
import static vcx.VCXTest.debugInit;

public class AlmostVCXWinTest {
    public Position testAlmostVCXTemplate(String input, int depth) {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgo(15);
        debugInit(chessboard, input);
        chessboard.generateStepsCode();
        int human = Player.WHITE.getId();
        VCX vcx = new VCX(chessboard.clone(), human, 27);
        assertEquals(Position.EMPTY, vcx.aiFindPos());

        AlmostVCXWin avcx = new AlmostVCXWin(chessboard, human, depth);
        Position res = avcx.aiFindPos();
        System.out.println("time cost:" + (System.currentTimeMillis() - avcx.startTime));
        return res;
    }

    @Test
    public void testCase1() {
        Assert.assertEquals(
                new Position(9, 7),
                testAlmostVCXTemplate("H8 I8 I9 Ja G7 H9 H7 J7 Ga J8 J9 K6 L5 I7 ", 23)
        );
    }

}