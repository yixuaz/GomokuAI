package aialgo.mcts;

import chessboardalgo.IChessboardAIAlgo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MCTSNode {
    private IChessboardAIAlgo board;
    private MCTSNode parent;
    private List<MCTSNode> children;
    @Getter
    private int wins;
    @Getter
    private int visits;
    @Getter
    private int player;
    @Getter
    private int winner;

    private int enemyWinCnt = 0;

    public final int y;
    public final int x;
    public final int step;

    public MCTSNode(IChessboardAIAlgo board, int player, int y, int x, int step) {
        this.board = board;
        this.parent = null;
        this.children = new ArrayList<>();
        this.wins = 0;
        this.visits = 0;
        this.player = player;
        this.y = y;
        this.x = x;
        this.step = step;
    }

    public IChessboardAIAlgo getBoard() {
        return board;
    }

    public MCTSNode getParent() {
        return parent;
    }

    public void setParent(MCTSNode parent) {
        this.parent = parent;
    }

    public List<MCTSNode> getChildren() {
        return children;
    }

    public void setWinner(int winner) {
        this.winner = winner;
        if (getParent() == null) {
            return;
        }
        MCTSNode par = getParent();
        if (winner == player) {
            // 走完这步，无论对面怎么下都还是自己赢； PAR 选步子，必然选这步确保自己赢，所以要给PAR 赋值
            par.setWinner(winner);

        } else {
            // 走完这步，对面赢了； PAR选步子，肯定不选这步； 如果PAR怎么选步子，都是对面赢， 那么PAR 赋值为这个WINNER； 以为这PAR'S PAR 走这步必胜
            par.enemyWinCnt++;
            if (par.enemyWinCnt == par.getChildren().size())
                par.setWinner(winner); // par lose, set winner as enemy
        }
    }

    public MCTSNode findChildren(int y, int x) {
        for (MCTSNode node : children) {
            if (node.y == y && node.x == x) return node;
        }
        return null;
    }

    public MCTSNode getWinChildMove() {
        if (winner == 0 || winner == player) return null;
        var winChildren = getChildren().stream().filter(x -> x.getWinner() == winner);
        return winChildren.findAny().orElse(null);
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementVisits() {
        this.visits++;
    }

    public int getPlayer() {
        assert player == 1 || player == 2;
        return player;
    }

    @Override
    public String toString() {
        return "MCTSNode{" +
                "wins=" + wins +
                ", visits=" + visits +
                ", y=" + y +
                ", x=" + x +
                ", winner=" + winner +
                ", player=" + player +
                ", step=" + step +
                '}';
    }
}
