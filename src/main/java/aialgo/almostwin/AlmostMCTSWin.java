package aialgo.almostwin;

import UI.Chessboard;
import UI.Piece;
import aialgo.IWinningAlgo;
import chessboardalgo.ChessboardByteArrayAlgo;
import chessboardalgo.IChessboardAIAlgo;
import chessboardalgo.IChessboardAlgo;
import common.Position;
import aialgo.mcts.MCTSNode;
import aialgo.mcts.MCTsBasedAIAlgo;
import player.Player;
import aialgo.vcx.VCX;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

public class AlmostMCTSWin extends MCTsBasedAIAlgo implements IWinningAlgo {
    private int eachChdLimitMs;
    private int simulateVCXDepth;
    private boolean findAlmostWinNode;
    public AlmostMCTSWin(IChessboardAIAlgo chessboardAlgo, int humanColor,
                         int eachChdLimitMs, int simulateVCXDepth) {
        super(chessboardAlgo, humanColor);
        this.eachChdLimitMs = eachChdLimitMs;
        this.simulateVCXDepth = simulateVCXDepth;
        findAlmostWinNode = false;
    }

    @Override
    protected MCTSNode getBestChild(MCTSNode node) {
        return node.getChildren().stream()
                .filter(y -> y.getWinner() != node.getPlayer())
                .min(Comparator.comparingInt(chd -> chd.getVisits()))
                .orElse(null);
    }

    @Override
    public Position aiFindPos() {
        // if already find ans, we utilize result
        if (findAlmostWinNode) {
            return utilizeFindResult();
        }
        // if not, find if it will almost win
        Position res = super.aiFindPos();
        return findAlmostWinNode ? new Position(res.y, res.x, true) : res;
    }

    private Position utilizeFindResult() {
        // player play an un-calculated position
        if (previousStep == null) {
            System.out.println("MCTS逼近算杀bug");
            // let other algo make decision
            return Position.EMPTY;
        }
        // enemy play then ai find vcx must win
        if (previousStep.getChildren().isEmpty()) {
            Position result = new VCX(chessboardAlgo.clone(), humanColor, simulateVCXDepth).aiFindPos();
            simulateVCXDepth -= 2;
            return result;
        }
        previousStep = previousStep.getWinChildMove();
        return new Position(previousStep.y, previousStep.x, true);
    }

    @Override
    protected MCTSNode aiFindPos(MCTSNode root) {
        expand(root, true);
        if (root.getChildren().size() > 6) {
            System.out.println("MCTS early stop");
            return null;
        }
        for (MCTSNode chd : root.getChildren()) {
            doMCTS(chd);
            if (root.getWinner() == chd.getPlayer()) {
                findAlmostWinNode = true;
                return chd;
            }
        }
        return null;
    }

    @Override
    protected boolean getBestMoveEnd(int i, MCTSNode root, long startTime) {
        return System.currentTimeMillis() - startTime >= eachChdLimitMs || root.getWinner() != 0 || Thread.currentThread().isInterrupted();
    }

    @Override
    protected void printDebugInfo(MCTSNode result) {

    }

    @Override
    protected int simulate(MCTSNode node, int rootStep) {
        IChessboardAIAlgo b = node.getBoard();
        int lastStepDonePlayer = node.getPlayer();
        IChessboardAIAlgo board = b.clone();
        if (board.isTerminal(node.x, node.y)) {
            node.setWinner(lastStepDonePlayer);
            return lastStepDonePlayer;
        }

        if (node.step > 10 && node.step > rootStep + 1) {
            Position vcx = new VCX(board.clone(), lastStepDonePlayer, simulateVCXDepth).aiFindPos();
            if (vcx != Position.EMPTY) {
                node.setWinner(3 - lastStepDonePlayer);
                return 3 - lastStepDonePlayer;
            }
        }
        return 0;
    }

    private static void debugInit(Chessboard chessboard) {
        String s = "H8 I8 I9 G7 J9 K9 Ha H9 Ga J8 Ia ";
        String[] steps = s.split(" ");
        char[] m = new char[256];
        for (int i = 1; i <= 6; i++) m['a' + i - 1] = (char) ('9' + i);
        for (char i = '1'; i <= '9'; i++) m[i] = i;
        Player curP = Player.BLACK;
        int gameId = chessboard.getGameId();
        for (String step : steps) {
            char x = step.charAt(0), y = m[step.charAt(1)];
            chessboard.addPiece(new Piece(x - 'A', '9' + 6 - y, curP), gameId);
            curP = curP.doSwitch();
        }

    }

    public static void main(String[] args) {
        Chessboard chessboard = new Chessboard(0);
        IChessboardAlgo chessboardAIAlgo = new ChessboardByteArrayAlgo(15);
        chessboard.setChessBoardAlgo(chessboardAIAlgo);
        debugInit(chessboard);

        // debugSteps.addAll(List.of(getIdx2(9, 9)));
        JFrame frame = new JFrame("Gomoku");
        frame.setSize(518, 540);
        frame.setLocationRelativeTo(null); // center
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chessboard);
        frame.setVisible(true);
        Player human = Player.WHITE;
        AlmostMCTSWin vcx = new AlmostMCTSWin(chessboardAIAlgo, human.getId(), 2500, 9);
        int gameId = chessboard.getGameId();
        chessboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (human != Player.WHITE || chessboardAIAlgo.steps() != 0) {
                    super.mouseClicked(e);
                    Piece pp = chessboard.getPieceByMouseEvent(e, human);
                    chessboard.addPiece(pp, gameId);
                    vcx.setPieceCallBack(pp.getY(), pp.getX(), pp.getPlayer().getId(), false);
                }
                long start = System.currentTimeMillis();
                Position pos = vcx.aiFindPos();
                System.out.println("time cost:" + (System.currentTimeMillis() - start));


                if (pos != Position.EMPTY) {
                    Piece p = new Piece(pos.x, pos.y, human.doSwitch());
                    chessboard.addPiece(p, gameId);
                    vcx.setPieceCallBack(p.getY(), p.getX(), p.getPlayer().getId(), true);
                    if (chessboard.isWin(p)) {
                        JOptionPane.showMessageDialog(frame, "电脑获胜", "本局结束", JOptionPane.PLAIN_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "算杀失败", "本局结束", JOptionPane.PLAIN_MESSAGE);
                }

            }
        });
    }
}
