package aidecorator;

import UI.Chessboard;
import aialgo.IAIAlgo;
import aialgo.mcts.MCTsBasedAIAlgo;
import aialgo.mcts.VCXBasedMCTsAlgo;
import common.Position;

import java.util.ArrayList;
import java.util.List;

public class SecondPlayBeginningDecorator extends AIAlgoDecorator {
    private int center = Chessboard.CHESSBOARD_SIZE / 2;
    private int[] stepGap = new int[]{0,-1,-1,-1,-1,-2,-2,-3};
    private MCTsBasedAIAlgo beginningAlgo;
    public SecondPlayBeginningDecorator(IAIAlgo iaiAlgo) {
        super(iaiAlgo);
        beginningAlgo = new VCXBasedMCTsAlgo(iaiAlgo.getChessboardAlgo(), iaiAlgo.getHumanColor());
    }

    @Override
    public Position aiFindPos() {
        int step = chessboardAlgo.steps();
        if (step % 2 != 1) {
            throw new IllegalStateException("it is a second go AI");
        }
        Position res = RuleBasedLookup();
        if (res == Position.EMPTY) {
            if (step < 4 || step > 8)
                res = decoratedAIAlgo.aiFindPos();
            else
                res = beginningAlgo.aiFindPos();
        }
        return res;
    }

    private Position RuleBasedLookup() {
        List<Position> allSteps = new ArrayList<>(chessboardAlgo.getAllSteps());
        Position res = Position.EMPTY;
        if (chessboardAlgo.steps() == 1) {
            return StepGapRule(allSteps.get(0));
        } else if (chessboardAlgo.steps() == 3) {
            res = PrefixLookup(chessboardAlgo.generateStepsCode(), chessboardAlgo.getAllSteps().peekLast());
        }
        return res;
    }

    private Position PrefixLookup(String statePrefix, Position last) {
        int lastNum = last.y * 100 + last.x;
        if (statePrefix.startsWith("H8 I8")) {
            switch (lastNum) {
                case 607:
                case 609: return new Position(5, 7);
                case 608: return new Position(5, 9);
                case 706: return new Position(8, 6);
                case 606:
                case 709: return new Position(8, 8);
                case 806: return new Position(6, 8);
                case 807: return new Position(9, 7);
                case 809:
                case 808: return new Position(9, 9);
                default: return Position.EMPTY;
            }
        } else {

        }
        return Position.EMPTY;
    }

    private Position StepGapRule(Position position) {
        int dx = position.x - center, dy = position.y - center;
        if (dx == 0 && dy == 0) return new Position(7, 8);
        int nx = position.x + (dx < 0 ? -stepGap[-dx] : stepGap[dx]);
        int ny = position.y + (dy < 0 ? -stepGap[-dy] : stepGap[dy]);
        return new Position(ny, nx);
    }


    private boolean twoPointClose(Position a, Position b, int distance) {
        return Math.abs(a.x-b.x) <= distance && Math.abs(a.y - b.y) <= distance;
    }

}
