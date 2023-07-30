package UI;

import javax.swing.*;
import java.awt.*;

public class MessagingPanel extends JPanel {
    private static final String WELCOME_MESSAGE = "Welcome to Gomoku Game";
    private static String text = WELCOME_MESSAGE;
    private static Color textColor = Color.DARK_GRAY;
    private MessagingPanel() {
    }
    public static MessagingPanel INSTANCE = new MessagingPanel();


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK); // Set the outline color to red
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        FontMetrics fontMetrics = g.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int x = (panelWidth - textWidth) / 2;
        int y = (panelHeight - textHeight) / 2 + fontMetrics.getAscent();

        g.setColor(textColor);
        g.drawString(text, x, y);
    }

    public static void AIStart() {
        textColor = Color.RED;
        text = "AI is thinking. Please wait.";
        INSTANCE.repaint();
    }

    public static void AIDone() {
        textColor = Color.DARK_GRAY;
        text = WELCOME_MESSAGE;
        INSTANCE.repaint();
    }


}
