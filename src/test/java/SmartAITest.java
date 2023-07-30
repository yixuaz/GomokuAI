import UI.Piece;
import aialgo.IAIAlgo;
import aialgo.ThreadSafeMinMaxAIAlgo;
import aialgo.vcx.NegamaxVCXEnhancedContext;
import aidecorator.*;
import chessboardalgo.ChessboardByteArrayAlgoConsistent;
import chessboardalgo.IChessboardAIAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import common.ThreadPoolContext;
import consistent.IConsistentAlgo;
import consistent.PositionTranslator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import player.Player;
import scorecalculator.CachedScoreManager;
import scorecalculator.IScoreManager;
import scorecalculator.Score;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static common.PositionConverter.getX;
import static common.PositionConverter.getY;
import static vcx.VCXTest.debugInit;

public class SmartAITest {
    private static int size = 15;
    private long sumTimeCost = 0;
    private int count = 0;
    private int neiDist = 2;
    private int limit = 40;

    public void testMustWinTemplate(String input) {
        testMustWinTemplate(input, 0, false);
    }

    public void testMustWinTemplate(String input, int nextCnt) {
        testMustWinTemplate(input, nextCnt, false);
    }

    public void testMustWinTemplate(String input, boolean rotate) {
        testMustWinTemplate(input, 0, rotate);
    }
    public void testMustWinTemplate(String input, int nextCnt, boolean rotate) {
        if (((input.length()+1) / 3) % 2 != 1) throw new IllegalStateException();
        IChessboardAlgo chessboard = new ChessboardByteArrayAlgoConsistent(size);
        debugInit(chessboard, input);
        PositionTranslator positionTranslator = ((IConsistentAlgo) chessboard).getPositionTranslator();
        int human = Player.WHITE.getId();
        int cnt = 0;
        List<String> failed = new ArrayList<>();
        for (int pos : new CachedScoreManager(chessboard).generateCandidatePiece(human, false, neiDist, limit)) {
            if (cnt < nextCnt) {
                cnt++;
                continue;
            }
            IChessboardAIAlgo tmp = chessboard.clone();
            AIAlgoDecorator ai = buildSmartAI(chessboard, human);

            setPiece(pos, positionTranslator, ai, chessboard, rotate);

            boolean mustWin = dfs(chessboard, ai, 0, failed, 17, false, rotate);
            if (!mustWin) {
                System.err.println(msg(pos));
            } else {
                System.out.println(cnt + " pass");
            }
            chessboard = (IChessboardAlgo) tmp;
            cnt++;
        }
        for (String s : failed) System.err.println(s);
        Assert.assertTrue(failed.isEmpty());
        System.out.println("avg time cost:" + ((double)sumTimeCost / count));
    }

    private void setPiece(int pos, PositionTranslator positionTranslator, IAIAlgo ai, IChessboardAlgo chessboard, boolean rotate) {
        int y = getY(pos,size), x = getX(pos,size);
        if (rotate) {
            Position position = new Position(y, x);
            position = positionTranslator.originDeTranslateThenOrigin(position);
            Piece p = new Piece(position.x, position.y, Player.WHITE);
            chessboard.setPiece(p);
            ai.setPieceCallBack(p.getY(), p.getX(), p.getPlayer().getId(), false);
        } else {
            chessboard.setPiece(new Piece(x, y, Player.WHITE));
            ai.setPieceCallBack(y, x, Player.WHITE.getId(),false);
        }
    }

    private AIAlgoDecorator buildSmartAI(IChessboardAlgo chessboard, int human) {
        ThreadPoolContext.waitActiveThreadNumberLowerEqualThan(4);
        return new FirstPlayBeginningLookupDecorator(
                new VCXDecorator(
                        new AlmostMCTSWinDecorator(
                                new AlmostVCXWinDecorator(
                                        new ThreadSafeMinMaxAIAlgo(chessboard, human, 9, NegamaxVCXEnhancedContext.ATTACK),
                                        23),
                                2500,
                                9
                        ),
                        27
                )
        );
    }

    private boolean dfs(IChessboardAlgo chessboard, AIAlgoDecorator ai, int depth, List<String> failed, int dfsLimit, boolean humanBlockFour, boolean rotate) {
        long st = System.currentTimeMillis();
        Position res = ai.aiFindPos();
        long timeCost = System.currentTimeMillis() - st;
        sumTimeCost += timeCost;
        count++;
        System.out.println("ai time cost: " + timeCost + ",active thread:" + ((ThreadPoolExecutor) ThreadPoolContext.threadPool).getActiveCount());
        if (res.winning) {
            System.out.println(chessboard.generateStepsCode());
            return true;
        }
        if (dfsLimit <= chessboard.steps()) {
            failed.add(chessboard.generateStepsCode());
            return false;
        }
        chessboard.setPiece(new Piece(res.x, res.y, Player.BLACK));
        PositionTranslator positionTranslator = ((IConsistentAlgo) chessboard).getPositionTranslator();
        int human = Player.WHITE.getId();
        CachedScoreManager scoreManager = new CachedScoreManager(chessboard);
        List<Integer> poss = scoreManager.generateCandidatePiece(human, false, neiDist, limit);
        boolean AIBlockFour = false;
        // AI走完，是个冲四
        if (poss.size() == 1) {
            AIBlockFour = true;
            dfsLimit += humanBlockFour ? 1 : 2;
        }

        for (int pos : poss) {
            humanBlockFour = false;
            IChessboardAIAlgo tmp = chessboard.clone();
            ai = buildSmartAI(chessboard, human);

            int x = getX(pos, size), y = getY(pos, size);
            if (scoreManager.getScore(y, x, Player.WHITE.getId()) >= Score.BLOCKED_FOUR.value) {
                humanBlockFour = true;
                dfsLimit += AIBlockFour ? 1 : 2;
            }

            setPiece(pos, positionTranslator, ai, chessboard, rotate);

            if (chessboard.isTerminal(getX(pos, size), getY(pos, size))) {
                failed.add(chessboard.generateStepsCode());
                return false;
            }
            boolean mustWin = dfs(chessboard, ai, depth + 1, failed, dfsLimit, humanBlockFour, rotate);
            if (!mustWin) {
                System.err.println("depth:" + depth + ",pos:" + msg(pos));
                System.out.println(failed);
                return false;
            }
            chessboard = (IChessboardAlgo) tmp;
        }
        return true;
    }

    private String msg(int pos) {
        return "y:" + getY(pos, size) + ", x:" + getX(pos, size);
    }

    @Test
    public void testCaseHard1() {
        testMustWinTemplate("H8 I7 I9");
    }

    @Test
    public void testCaseHard1_rotate() {
        testMustWinTemplate("H8 G9 G7", true);
    }

    @Test
    public void testCaseHard2() {
        testMustWinTemplate("H8 I8 I9");
    }

    @Test
    public void testCaseHard3() {
        testMustWinTemplate("H8 J8 F8");
    }

    @Test
    public void testCaseHard4() {
        testMustWinTemplate("H8 J7 G7");
    }

    @Test
    public void testCaseHard5() {
        testMustWinTemplate("H8 J6 I9");
    }

    @Test
    public void testCaseHard6() {
        testMustWinTemplate("H8 K8 G9");
    }

    @Test
    public void testCaseHard7() {
        testMustWinTemplate("H8 K7 G9");
    }

    @Test
    public void testCaseHard8() {
        testMustWinTemplate("H8 K6 G9");
    }

    @Test
    public void testCaseHard9() {
        testMustWinTemplate("H8 K5 G9");
    }
}
