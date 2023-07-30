package consistent;

import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import org.junit.Test;

import java.util.ArrayList;

import static vcx.VCXTest.debugInit;

public class ConsistentPatternTest {

    @Test
    public void testSame() {
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgo(15);
        debugInit(chessboard, "H8 I6 I9 I7 Ha G8");
        String key = ConsistentPattern.canonical(new ArrayList<>(chessboard.getAllSteps()));
        System.out.println(key);

    }

}