package UI;

import lombok.Getter;
import player.PlayStrategyDecider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class GameSettingPanel extends JPanel implements IUICallback {
    public static final String UNDO = "Undo";
    public static final String START_GAME = "Start Game";
    @Getter
    private ButtonGroup firstPlay;
    @Getter
    private ButtonGroup secondPlay;

    private int restButtonNumber;

    private Map<String, JButton> buttonMap;

    public GameSettingPanel(int buttonNumber) {
        super(new GridLayout(3 + buttonNumber, 1));
        if (buttonNumber < 1) throw new IllegalArgumentException("buttonNumber should be postive");
        this.add(MessagingPanel.INSTANCE);
        firstPlay = buildButtonGroup("go first");
        secondPlay = buildButtonGroup("backhand");
        restButtonNumber = buttonNumber;
        buttonMap = new HashMap<>(buttonNumber);
    }

    public boolean ready() {
        return restButtonNumber == 0;
    }

    public void buildButton( String text, ActionListener action ) {
        if (restButtonNumber <= 0)
            throw new IllegalCallerException("restButtonNumber is 0, call too much buildButton");
        restButtonNumber--;
        JButton button = new JButton(text);
        button.addActionListener(action);
        buttonMap.put(text, button);
        this.add(button, BorderLayout.PAGE_END);
    }

    private ButtonGroup buildButtonGroup(String order) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel group1Title = new JLabel(order + ":");
        buttonPanel.add(group1Title);

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton radioButton1A = new JRadioButton(PlayStrategyDecider.HUMAN);
        buttonGroup.add(radioButton1A);
        buttonPanel.add(radioButton1A);

        JRadioButton radioButton1B = new JRadioButton(PlayStrategyDecider.FAST_AI);
        buttonGroup.add(radioButton1B);
        buttonPanel.add(radioButton1B);

        JRadioButton radioButton1C = new JRadioButton(PlayStrategyDecider.SMART_AI);
        buttonGroup.add(radioButton1C);
        buttonPanel.add(radioButton1C);

        radioButton1A.setSelected(true);
        this.add(buttonPanel);
        return buttonGroup;
    }

    @Override
    public void onAIThinkingStart() {
        MessagingPanel.AIStart();
        if (buttonMap.containsKey(UNDO))
            buttonMap.get(UNDO).setEnabled(false);
    }

    @Override
    public void onAIThinkingDone() {
        MessagingPanel.AIDone();
        if (buttonMap.containsKey(UNDO))
            buttonMap.get(UNDO).setEnabled(true);
    }
}
