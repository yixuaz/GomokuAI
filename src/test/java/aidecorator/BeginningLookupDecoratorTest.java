package aidecorator;

import aialgo.MinMaxAIAlgo;
import chessboardalgo.ChessboardByteArrayAlgoConsistent;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import org.junit.Assert;
import org.junit.Test;
import player.Player;

import static vcx.VCXTest.debugInit;

public class BeginningLookupDecoratorTest {

    public Position testTemplate(String input) {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgoConsistent(15);
        debugInit(chessboard, input);

        int human = Player.WHITE.getId();

        FirstPlayBeginningLookupDecorator ai = new FirstPlayBeginningLookupDecorator(new MinMaxAIAlgo(chessboard, human));
        Position res = ai.aiFindPos();
        return res;
    }

    @Test
    public void testLookupSuccess() {
        Assert.assertEquals(
                new Position(6, 6),
                testTemplate("H8 I6 I9 I7 Ha G8")
        );

        Assert.assertEquals(
                new Position(5, 7),
                testTemplate("H8 I7 I9 G7 J9 G8 J7 H9 J8 J6")
        );
    }

    @Test
    public void testLookupRotateSuccess() {
        Assert.assertEquals(
                new Position(8, 7),
                testTemplate("H8 J7 G7 J6 H6 G8")
        );
    }

}