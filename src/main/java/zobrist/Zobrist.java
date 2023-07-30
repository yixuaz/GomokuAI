package zobrist;

import chessboardalgo.IChessboardAIAlgo;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Zobrist {
    public static int DISABLE_CACHE_MASK = 100_000_000;
    private final long[] zobristTable;
    @Getter
    private Map<Long, ResultAndDepth> transpositionTable;

    @Getter
    private long hash = 0;

    @Getter
    private int cacheMatch = 0;

    private int size;
    public Zobrist(IChessboardAIAlgo chessBoardAlgo) {
        size = chessBoardAlgo.getSize();
        zobristTable = new long[size * size * 2];
        transpositionTable = new HashMap<>();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < zobristTable.length; i++) {
            zobristTable[i] = secureRandom.nextLong();
        }
        calculateInitialHash(chessBoardAlgo);
    }

    public void updateHash(int y, int x, int role) {
        if (role < 1 || role > 2) {
            throw new IllegalArgumentException("Invalid move");
        }
        hash ^= zobristTable[(y * size + x) * 2 + (role - 1)];
    }

    private void calculateInitialHash(IChessboardAIAlgo chessBoardAlgo) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = y * size + x;
                int val = chessBoardAlgo.getValInBoard(x, y);
                if (val != 0) {
                    hash ^= zobristTable[(i << 1) + (val - 1)];
                }
            }
        }
    }

    public Optional<Integer> tryGet(int depth) {
        if (!transpositionTable.containsKey(hash)) return Optional.empty();
        ResultAndDepth scoreAndDepth = transpositionTable.get(hash);
        if (scoreAndDepth.depth >= depth) {
            cacheMatch++;
            return Optional.of(scoreAndDepth.result);
        }
        return Optional.empty();
    }

    public int setAndReturnScore(int result, int depth) {
        if (Math.abs(result) != DISABLE_CACHE_MASK)
            transpositionTable.put(hash, new ResultAndDepth(result, depth));
        return result;
    }

    private class ResultAndDepth {
        int result;
        int depth;

        public ResultAndDepth(int result, int depth) {
            this.result = result;
            this.depth = depth;
        }
    }

}
