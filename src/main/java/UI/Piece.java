package UI;

import lombok.Getter;
import player.Player;

import java.awt.*;

@Getter
public class Piece {
    private static int ID = 1;
    private static final Color FONT_COLOR = Color.red;
    private int x;
    private int y;
    private Player player;
    private int idx;

    public Piece(int x, int y, Player player) {
        this.x = x; //非实际坐标，而是格子数，第x格
        this.y = y; //同上，第y格
        this.player = player;
        this.idx = ID;
    }

    public void draw(Graphics g, int cellSize, int margin) {
        g.setColor(player.getColor());
        int xPos = x * cellSize + margin - cellSize / 2;
        int yPos = y * cellSize + margin - cellSize / 2;
        g.fillOval(xPos, yPos, cellSize, cellSize);
        g.setColor(FONT_COLOR);
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        String str = ""+idx;
        g.drawString( str, xPos + cellSize / 2 - (fontMetrics.stringWidth(str) >> 1) , yPos + cellSize / 2 + (fontMetrics.getHeight() / 4));
    }

    public void highlight(Graphics g, int cellSize, int margin) {
        g.setColor(FONT_COLOR);
        int xPos = x * cellSize + margin - cellSize / 2;
        int yPos = y * cellSize + margin - cellSize / 2;
        g.drawOval(xPos, yPos, cellSize, cellSize);
        g.drawOval(xPos + 1, yPos + 1, cellSize-2, cellSize-2);
    }

    public void incrementId() {
        ID++;
    }

    public static void reset() {
        ID = 1;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "x=" + x +
                ", y=" + y +
                ", player=" + player.name() +
                ", idx=" + idx +
                '}';
    }
}
