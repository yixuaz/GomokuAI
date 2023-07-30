package UI;

import chessboardalgo.IChessboardAIAlgo;
import chessboardalgo.IChessboardUIAlgo;
import lombok.Getter;
import lombok.Setter;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Chessboard extends JPanel {
    public static final int CHESSBOARD_SIZE = 15;
    public final int margin = 20;
    public final int bottomButtonMargin;
    private List<Piece> pieces = new ArrayList<>();
    private final int[][] starPoints = {
            {3, 3}, {3, 11}, {11, 3}, {11, 11}, {7, 7}
    };

    private int gameId = 0;

    public synchronized int getGameId() {
        return gameId;
    }

    @Getter @Setter
    private IChessboardUIAlgo chessBoardAlgo;

    private boolean isEnd = false;

    public Chessboard(int bottomHeight) {
        bottomButtonMargin = bottomHeight;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawChessBoard(g);
        drawPieces(g);
        if (!pieces.isEmpty()) {
            Piece last = pieces.get(pieces.size() - 1);
            last.highlight(g, getCellSize(), margin);
        }
    }

    private int getMinSize() {
        return Math.min(getWidth(), getHeight() - bottomButtonMargin + 10);
    }
    private int getCellSize() {
        return (getMinSize() - 2 * margin) / (CHESSBOARD_SIZE - 1);
    }

    public Piece getPieceByMouseEvent(MouseEvent e, Player curPlayer) {
        int cellSize = getCellSize();
        int x = (e.getX() - 5) / cellSize;
        int y = (e.getY() - 5) / cellSize;
        return new Piece(x, y, curPlayer);
    }

    private void drawPieces(Graphics g) {
        List<Piece> snapshot = new ArrayList<>(pieces);
        for (Piece piece : snapshot) {
            piece.draw(g, getCellSize(), margin);
        }
    }

    public synchronized boolean addPiece(Piece piece, int previousGameId) {
        if (previousGameId != gameId) return false;
        return addPiece(piece);
    }

    private boolean addPiece(Piece piece) {
        checkChessboardAlgoExist();

        if (chessBoardAlgo.isLegalMove(piece)) {
            chessBoardAlgo.setPiece(piece);
            pieces.add(piece);
            repaint();
            piece.incrementId();
            return true;
        }
        System.out.println("illegal move: " + ((IChessboardAIAlgo) chessBoardAlgo).getValInBoard(piece.getX(), piece.getY()));
        return false;
    }

    private void checkChessboardAlgoExist() {
        if (chessBoardAlgo == null)
            throw new IllegalStateException("invalid area, miss chessBoardAlgo");
    }

    private void drawChessBoard(Graphics g) {
        int cellSize = getCellSize();
        for (int i = 0; i < CHESSBOARD_SIZE; i++) {
            g.drawLine(margin, margin + cellSize * i, margin + cellSize * 14, margin + cellSize * i);
            g.drawLine(margin + cellSize * i, margin, margin + cellSize * i, margin + cellSize * 14 );

            // Add number labels to the x and y axes
            addNumberInLine(g, i, cellSize);
        }
        drawStarPoints(g, cellSize);
    }

    private void drawStarPoints(Graphics g, int cellSize) {
        // Draw the star points
        int starPointRadius = cellSize / 4;
        g.setColor(Color.BLACK);
        for (int[] starPoint : starPoints) {
            int x = starPoint[0] * cellSize + margin - starPointRadius / 2;
            int y = starPoint[1] * cellSize + margin - starPointRadius / 2;
            g.fillOval(x, y, starPointRadius, starPointRadius);
        }
    }

    private void addNumberInLine(Graphics g, int i, int cellSize) {
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        int height = fontMetrics.getHeight() / 2;
        String s = Integer.toString(i);
        int width = fontMetrics.stringWidth(s) / 2;
        g.drawString(s, cellSize * i + margin - width,  margin / 4 + height);
        g.drawString(s, margin / 2 - width, cellSize * i + margin + height / 2);

    }

    public synchronized boolean isWin(Piece piece) {
        checkChessboardAlgoExist();

        return isEnd = chessBoardAlgo.isTerminal(piece);
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized void reset() {
        gameId++;
        checkChessboardAlgoExist();
        Piece.reset();
        pieces.clear();
        isEnd = false;
        repaint();
    }
}
