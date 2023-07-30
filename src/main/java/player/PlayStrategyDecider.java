package player;

import UI.Chessboard;
import UI.IUICallback;
import aialgo.ThreadSafeMinMaxAIAlgo;
import aidecorator.*;
import chessboardalgo.ChessboardByteArrayAlgoConsistent;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAlgo;
import aialgo.vcx.NegamaxVCXEnhancedContext;

public class PlayStrategyDecider {
    public static final String HUMAN = "human";
    public static final String FAST_AI = "fast AI";
    public static final String SMART_AI = "smart AI";

    public static IChessboardAlgo decideChessboardAlgo(String blackStrategyName, String whiteStrategyName) {
        if (SMART_AI.equals(blackStrategyName)) { // 黑棋先手，且是最强AI时
            return new ChessboardByteArrayAlgoConsistent(Chessboard.CHESSBOARD_SIZE);
        } else {
            return new ChessboardByteArrayAlgo(Chessboard.CHESSBOARD_SIZE);
        }
    }

    public static IPlayStrategy buildPlayStrategy(String name, IChessboardAlgo chessboardAlgo, int enemyColor, IUICallback uiCallback) {
        switch (name) {
            case HUMAN: return HumanPlayStrategy.INSTANCE;
            case FAST_AI: return new AIPlayStrategy(new VCXDecorator(new ThreadSafeMinMaxAIAlgo(chessboardAlgo, enemyColor), 23), uiCallback);
            case SMART_AI:
            {
                if (enemyColor == Player.WHITE.getId()) {
                    return new AIPlayStrategy(
                            new FirstPlayBeginningLookupDecorator(
                                    new VCXDecorator(
                                            new AlmostMCTSWinDecorator(
                                                new AlmostVCXWinDecorator(
                                                        new ThreadSafeMinMaxAIAlgo(chessboardAlgo, enemyColor, 9, NegamaxVCXEnhancedContext.ATTACK),
                                                        23),
                                                2500,
                                                9
                                            ),
                                            27
                                    )
                            ),
                            uiCallback
                    );
                } else {
                    return new AIPlayStrategy(
                            new SecondPlayBeginningDecorator(
                            new VCXDecorator(new ThreadSafeMinMaxAIAlgo(chessboardAlgo, enemyColor, 9, NegamaxVCXEnhancedContext.DEFENSE), 27)),
                            uiCallback);
                }
            }
        }
        throw new IllegalStateException("invalid area");
    }


}
