package scorecalculator;

import chessboardalgo.IChessboardAIAlgo;
import common.PositionConverter;
import player.Player;

import java.util.ArrayList;
import java.util.List;

import static common.PositionConverter.convertToIdx;

public class CachedScoreManager implements IScoreManager {
    protected int[][][][] scoreCache;
    protected int[][] blackScore;
    protected int[][] whiteScore;
    protected PointEvaluator pointEvaluator;
    protected int size;
    protected IChessboardAIAlgo chessBoardAlgo;
    public CachedScoreManager(IChessboardAIAlgo chessBoardAIAlgo) {
        chessBoardAlgo = chessBoardAIAlgo;
        size = chessBoardAlgo.getSize();
        scoreCache = new int[3][4][size][size];
        blackScore = new int[size][size];
        whiteScore = new int[size][size];
        pointEvaluator = new PointEvaluator(size, chessBoardAlgo, scoreCache);
        initScore();
    }

    @Override
    public void initScore() {
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                int val = chessBoardAlgo.getValInBoard(i, j);
                if (val == 0) {
                    if (chessBoardAlgo.hasNeighbor(i, j, 2, 1)) {
                        blackScore[j][i] = pointEvaluator.scorePoint(j, i, Player.BLACK.getId());
                        whiteScore[j][i] = pointEvaluator.scorePoint(j, i, Player.WHITE.getId());
                    }
                } else if (val == Player.BLACK.getId()) {
                    blackScore[j][i] = pointEvaluator.scorePoint(j, i, Player.BLACK.getId());
                    whiteScore[j][i] = 0;
                } else if (val == Player.WHITE.getId()) {
                    blackScore[j][i] = 0;
                    whiteScore[j][i] = pointEvaluator.scorePoint(j, i, Player.WHITE.getId());
                } else {
                    throw new IllegalStateException("??");
                }
            }
        }
    }

    @Override
    public int getScore(int y, int x, int role) {
        return role == Player.WHITE.getId() ? whiteScore[y][x] : blackScore[y][x];
    }

    @Override
    public void updateScore(int y, int x) {
        int radius = 10;
        for (Direction dir : Direction.values()) {
            for (int i = -radius; i <= radius; i++) {
                int ny = y + dir.dy * i;
                int nx = x + dir.dx * i;
                if (ny < 0 || nx < 0 || ny >= size || nx >= size) continue;
                updatePointInDirection(ny, nx, dir);
            }
        }
    }

    private void updatePointInDirection(int y, int x, Direction dir) {
        int role = chessBoardAlgo.getValInBoard(x, y);
        if (role != Player.WHITE.getId()) {
            int bs = pointEvaluator.scorePoint(y, x, Player.BLACK.getId(), dir);
            blackScore[y][x] = bs;
        } else {
            blackScore[y][x] = 0;
        }
        if (role != Player.BLACK.getId()) {
            int ws = pointEvaluator.scorePoint(y, x, Player.WHITE.getId(), dir);
            whiteScore[y][x] = ws;
        } else {
            whiteScore[y][x] = 0;
        }
    }

    @Override
    public List<Integer> generateCandidatePiece(int role) {
        return generateCandidatePiece(role, false, 1, 20);
    }

    public List<Integer> generateCandidatePiece(int role, boolean importOnly) {
        return generateCandidatePiece(role, importOnly, 1, 20);
    }

    public List<Integer> generateCandidatePiece(int role, boolean importOnly, int neiDist, int limit) {
        List<Integer> fives = new ArrayList<>();
        List<Integer> myFours = new ArrayList<>();
        List<Integer> enemyFours = new ArrayList<>();
        List<Integer> myBlockedFours = new ArrayList<>();
        List<Integer> enemyBlockedFours = new ArrayList<>();
        List<Integer> myTwoThrees = new ArrayList<>();
        List<Integer> enemyTwoThrees = new ArrayList<>();
        List<Integer> myThrees = new ArrayList<>();
        List<Integer> enemyThrees = new ArrayList<>();
        List<int[]> myTwos = new ArrayList<>();
        List<int[]> enemyTwos = new ArrayList<>();
        List<Integer> ones = new ArrayList<>();
        int white = Player.WHITE.getId();
        int[][] myScore = role == white ? whiteScore : blackScore;
        int[][] enemyScore = role == white ? blackScore : whiteScore;
        int farAttackPotential = chessBoardAlgo.steps() <= 7 ? 2 * Score.TWO.value : Score.TWO.value;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (chessBoardAlgo.getValInBoard(x, y) != 0) continue;
                if (!chessBoardAlgo.hasNeighbor(x, y, neiDist, 1)) {
                    if (chessBoardAlgo.hasNeighbor(x, y, 2, 1) && myScore[y][x] >= farAttackPotential) {
                        // exception 1 -> (1) 0 1 1 0

                        // exception 2 -> (1) 0 1 1 2
                        //             ->  0  0 0 0 0
                        //             ->  0  0 1 0 0
                        //             ->  0  0 0 0 0
                    } else {
                        continue;
                    }

                }

                int scoreMe = myScore[y][x];
                int scoreEnemy = enemyScore[y][x];
                int idx = convertToIdx(y, x, size);
                if (scoreMe >= Score.FIVE.value || scoreEnemy >= Score.FIVE.value) {
                    fives.add(idx);
                } else if (scoreMe >= Score.FOUR.value) {
                    myFours.add(idx);
                } else if (scoreEnemy >= Score.FOUR.value) {
                    enemyFours.add(idx);
                } else if (scoreMe >= Score.BLOCKED_FOUR.value) {
                    myBlockedFours.add(idx);
                } else if (scoreEnemy >= Score.BLOCKED_FOUR.value) {
                    enemyBlockedFours.add(idx);
                } else if (scoreMe >= 2 * Score.THREE.value) {
                    myTwoThrees.add(idx);
                } else if (scoreEnemy >= 2 * Score.THREE.value) {
                    enemyTwoThrees.add(idx);
                } else if (scoreMe >= Score.THREE.value) {
                    myThrees.add(idx);
                } else if (scoreEnemy >= Score.THREE.value) {
                    enemyThrees.add(idx);
                } else if (scoreMe >= Score.TWO.value / 2) {
                    myTwos.add(new int[]{idx, scoreMe});
                } else if (scoreEnemy >= Score.TWO.value / 2) {
                    enemyTwos.add(new int[]{idx, scoreEnemy});
                } else {
                    ones.add(idx);
                }
            }
        }
        if (!fives.isEmpty()) return fives;

        // 自己能活四，则直接活四，不考虑冲四
        if (!myFours.isEmpty()) return myFours;

        // 对面有活四，自己冲四都没，
        if (!enemyFours.isEmpty() && myBlockedFours.isEmpty()) {
            // 遇到 XX 0 X 的情况，除了防中间的活四位，也可以防左右的冲四位
            enemyFours.addAll(enemyBlockedFours);
            return enemyFours;
        }

        // 自己没活四，对面有活四且自己有冲四

        if (!enemyFours.isEmpty()) {
            enemyFours.addAll(myBlockedFours);
            enemyFours.addAll(enemyBlockedFours);
            return enemyFours;
        }

        List<Integer> results = new ArrayList<>();
        results.addAll(myTwoThrees);
        results.addAll(enemyTwoThrees);
        results.addAll(myBlockedFours);
        results.addAll(myThrees);
        if (importOnly && !results.isEmpty()) {
            return results;
        }
        results.addAll(enemyBlockedFours);
        results.addAll(enemyThrees);


        if (!results.isEmpty() && (!enemyTwoThrees.isEmpty() || !myTwoThrees.isEmpty()))
            return results;

        List<int[]> twos = new ArrayList<>();
        twos.addAll(myTwos);
        twos.addAll(enemyTwos);
        twos.sort((a,b)-> Integer.compare(b[1],a[1]));
        if (!twos.isEmpty()) {
            for (int[] i : twos) {
                results.add(i[0]);
            }
        } else {
            results.addAll(ones);
        }
        if (results.size() > limit) return results.subList(0, limit);
        return (results.isEmpty() && chessBoardAlgo.getValInBoard(size / 2, size / 2) == 0)
                ?
                List.of(convertToIdx(size / 2, size / 2, size))
                :
                results;
    }

    @Override
    public int evaluation(boolean isAI, int humanColor) {
        int blackMaxScore = 0, whiteMaxScore = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (chessBoardAlgo.getValInBoard(j, i) == Player.BLACK.getId()) {
                    blackMaxScore += this.blackScore[i][j];
                } else if (chessBoardAlgo.getValInBoard(j, i) == Player.WHITE.getId()) {
                    whiteMaxScore += this.whiteScore[i][j];
                }
            }
        }
        if (isAI) {
            if (humanColor != Player.BLACK.getId())
                return blackMaxScore - whiteMaxScore;
            else return whiteMaxScore - blackMaxScore;
        } else {
            if (humanColor == Player.BLACK.getId())
                return blackMaxScore - whiteMaxScore;
            else return whiteMaxScore - blackMaxScore;
        }
    }
}
