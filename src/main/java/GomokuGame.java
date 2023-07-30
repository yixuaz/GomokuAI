import UI.Chessboard;
import UI.GameSettingPanel;
import UI.Piece;
import chessboardalgo.IChessboardAlgo;
import common.BoardToString;
import common.Position;
import player.PlayStrategyDecider;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Deque;
import java.util.Enumeration;

public class GomokuGame {
    private JFrame frame;
    private Chessboard chessboard;
    private Player curPlayer = Player.BLACK;
    private volatile Thread t;

    private void startGame(GameSettingPanel bottomPanel, String state) {
        String blackStrategy = getText(bottomPanel.getFirstPlay());
        String whiteStrategy = getText(bottomPanel.getSecondPlay());
        IChessboardAlgo chessboardAlgo = PlayStrategyDecider.decideChessboardAlgo(blackStrategy, whiteStrategy);
        chessboard.setChessBoardAlgo(chessboardAlgo);
        Player.BLACK.setPlayStrategy(PlayStrategyDecider.buildPlayStrategy(blackStrategy, chessboardAlgo, Player.WHITE.getId(), bottomPanel));
        Player.WHITE.setPlayStrategy(PlayStrategyDecider.buildPlayStrategy(whiteStrategy, chessboardAlgo, Player.BLACK.getId(), bottomPanel));
        chessboard.reset();
        int gameId = chessboard.getGameId();
        Player curP = Player.BLACK;
        for (Position p : BoardToString.strDecode(state)) {
            if (!chessboard.addPiece(new Piece(p.x, p.y, curP), gameId)) break;
            curP = curP.doSwitch();
        }
        curPlayer = curP;
        if (t != null && t.isAlive())
            t.interrupt();
    }

    private void undo(GameSettingPanel bottomPanel) {
        Deque<Position> allSteps = chessboard.getChessBoardAlgo().getAllSteps();
        allSteps.pollLast();
        allSteps.pollLast();
        startGame(bottomPanel,BoardToString.serialize(allSteps));
    }

    public void init() {
        GameSettingPanel bottomPanel = new GameSettingPanel(2);
        bottomPanel.buildButton(GameSettingPanel.START_GAME, e -> startGame(bottomPanel, ""));
        bottomPanel.buildButton(GameSettingPanel.UNDO, e -> undo(bottomPanel));


        buildChessBoardPanel(bottomPanel.getPreferredSize().height);
        frame = buildJFrame(bottomPanel.getPreferredSize().height);
        frame.add(chessboard);

        if (!bottomPanel.ready())
            throw new IllegalStateException("bottom panel should be ready then add to chessboard");
        chessboard.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.repaint();
        frame.setVisible(true);
    }

    private String getText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            JRadioButton button = (JRadioButton) buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }
        throw new IllegalStateException("invalid area: button is unselected");
    }

    private JPanel buildChessBoardPanel(int bottomHeight) {
        chessboard = new Chessboard(bottomHeight);
        chessboard.setLayout(new BorderLayout());
        chessboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (t != null && t.isAlive()) return;
                super.mouseClicked(e);
                actionPiece(e);
            }
        });

        return chessboard;
    }

    private JFrame buildJFrame(int bottomHeight) {
        JFrame frame = new JFrame("Gomoku");
        frame.setSize(518, 540 + bottomHeight);
        frame.setLocationRelativeTo(null); // center
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return frame;
    }


    private void actionPiece(MouseEvent e) {
        int gameId = chessboard.getGameId();
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                if(chessboard.isEnd()) return;
                Piece piece = curPlayer.decidePieceIfFailReturnNull();
                if (piece == null) {
                    // human turn
                    piece = chessboard.getPieceByMouseEvent(e, curPlayer);
                }
                while (curPlayer.play(piece, chessboard, gameId)) {
                    boolean win = chessboard.isWin(piece);
                    if (win) {
                        JOptionPane.showMessageDialog(frame, winMessage(curPlayer), "本局结束", JOptionPane.PLAIN_MESSAGE);
                        chessboard.getChessBoardAlgo().generateStepsCode();
                        return;
                    }
                    curPlayer = curPlayer.doSwitch();
                    System.out.println("------------");
                    System.out.println(curPlayer.name());
                    System.out.println("------------");
                    piece = curPlayer.decidePieceIfFailReturnNull();
                    System.out.println(piece);
                }
            }
        });
        t.start();
    }

    private String winMessage(Player winner) {
        boolean isAI = winner.getPlayStrategy().isAI();
        boolean isWhite = winner == Player.WHITE;
        return (isWhite ? "白棋" : "黑棋") + (isAI ? "电脑" : "玩家") + "获胜！";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GomokuGame().init());
    }
}
