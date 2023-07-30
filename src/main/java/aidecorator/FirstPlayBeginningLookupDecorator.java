package aidecorator;

import aialgo.IAIAlgo;
import common.BoardToString;
import common.Position;
import consistent.ConsistentPattern;
import consistent.IConsistentAlgo;
import consistent.PositionTranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static consistent.ConsistentPattern.canonical;

public class FirstPlayBeginningLookupDecorator extends ConsistentDecorator {

    private static final Map<String, Position> chessboarsState2Pos = new HashMap<>();
    static {
        initChessboardState2Pos(FirstPlayBeginningLookupDecorator.class.getClassLoader().getResourceAsStream("game_start.data"));
    }
    public FirstPlayBeginningLookupDecorator(IAIAlgo iaiAlgo) {
        super(iaiAlgo);
        if (!(chessboardAlgo instanceof IConsistentAlgo)) {
            throw new IllegalArgumentException("BeginningLookupDecorator must use Consistent chess board algo");
        }
    }

    private static void initChessboardState2Pos(InputStream resourceAsStream) {
        try  (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] ss = line.split(",");
                List<Position> input = BoardToString.strDecode(ss[0]);
                String[] pos = ss[1].split(":");
                int[] bestXandY = new int[]{Integer.parseInt(pos[0]), Integer.parseInt(pos[1])};
                String key = canonical(input, bestXandY);
                chessboarsState2Pos.put(key, new Position(bestXandY[1], bestXandY[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Position aiFindPos() {
        setPositionTranslatorIfNull();
        if (chessboardAlgo.steps() % 2 != 0) {
            throw new IllegalStateException("it is a first go AI");
        }

        Position res = lookupSolution();
        if (res == Position.EMPTY) {
            res = decoratedAIAlgo.aiFindPos();
        }
        return positionTranslator == null ? res : positionTranslator.originDeTranslateThenOrigin(res);
    }

    private Position lookupSolution() {
        int center = chessboardAlgo.getSize() / 2;
        if (chessboardAlgo.steps() == 0) return new Position(center, center);
        Position res = ConsistentPattern.findBestMove(new ArrayList<>(chessboardAlgo.getAllSteps()), chessboarsState2Pos);
        return res == Position.EMPTY ? RuleBasedLookup() : res;

    }

    private Position RuleBasedLookup() {
        if (chessboardAlgo.steps() == 2) {
            return new Position(6, 6);
        } else if (chessboardAlgo.steps() == 4) {
            List<Position> allSteps = new ArrayList<>(chessboardAlgo.getAllSteps());
            if (!twoPointClose(allSteps.get(0), allSteps.get(1), 3) &&
                    twoPointClose(allSteps.get(1), allSteps.get(3), 2))
                return new Position(7, 5);
            if (!twoPointClose(allSteps.get(0), allSteps.get(1), 2) &&
                    !twoPointClose(allSteps.get(0), allSteps.get(3), 2))
                return new Position(7, 5);
        }
        return Position.EMPTY;
    }

    private boolean twoPointClose(Position a, Position b, int distance) {
        return Math.abs(a.x-b.x) <= distance && Math.abs(a.y - b.y) <= distance;
    }

}
