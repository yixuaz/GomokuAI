## 前言
这套文集会通过一个五子棋的AI, 来带大家走进对抗搜索和博弈的算法。
选择五子棋的原因是，他是一个完美信息的零和博弈。完美信息就是完全可观测的意思。大家都能感知到对局全部的信息。零和博弈是指对一方有利的东西对另一方同等程度有害。
整个文集会依次从下面几个章节展开。
1. 极小化极大搜索
2. a-b 剪枝优化
3. 移动顺序优化
4. 评价函数设计
5. 截断搜索和前向剪枝
6. 搜索和查表
7. 蒙特卡罗树搜索
8. ProofNumber离线搜索

最后结合上述技术，我们可以看到AI的棋力是如何一步一步增强的。最后基本做到黑棋先手必胜的棋力。

全文也会带着大家写出尽可能利于扩展和可读性和可维护性比较高的代码。

配套代码为JAVA，UI通过`javax.swing` 去写。
![1690707942687.png](https://upload-images.jianshu.io/upload_images/10803273-424258468865bfa4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

整个项目的代码放在了
https://github.com/yixuaz/GomokuAI

## 第一步我们需要实现一下五子棋的UI
这部分是一些面向对象的设计，想看算法的可以跳过。

UI的话，大概分为几个模块。第一个肯定是棋盘，所以我们可以先构建一个棋盘类。棋盘类的职责，就是负责接收用户在棋盘上的下棋操作，以及记录棋子位置和顺序，然后渲染棋盘。
```
public class Chessboard extends JPanel {
    public static final int CHESSBOARD_SIZE = 15;
    private List<Piece> pieces = new ArrayList<>();
    @Override
    public void paint(Graphics g) {}
    public boolean addPiece(Piece piece) { ...}
  // 判断落子后是否构成胜利
    public boolean isWin(Piece piece) {...}
   // 判断用户点击是否还有效
    public boolean isEnd() { ... }
   // 重置
   public void reset() {}
```
然后有了棋盘类，我们肯定还需要棋子类。棋子类这里只有白棋和黑棋2种。代表了先手玩家和后手玩家。所以我们这里可以使用一个枚举类，来表示这2类玩家。然后棋子类可以有一个属性代表这是哪个玩家的棋子。

```
@Getter
public class Piece {
   // ID 自增器
    private static int ID = 1;
    private int x;
    private int y;
    private Player player;
    private int idx;
}
```

然后是游戏设置局面，大概就是给玩家选择是人机对战还是人人对战，还是电脑打电脑。这个大家就自由发挥。我是配置了先手和后手的分别3种模式（人，简单的电脑，聪明的电脑）；然后按钮有2个，开始游戏 和 悔棋。

这里我用了`GridLayout(3 + buttonNumber, 1)`；行数是3 + buttonNumber，列数是1。感觉竖着排列会容易一些。如果想要一行放多个的，可以单独再建一个component，放进去
![1690722192661.png](https://upload-images.jianshu.io/upload_images/10803273-bc566578638798c8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

具体效果如图。
3分别是代表一个游戏信息栏，2个配置栏。可能有些人会想添加一些其他按钮所以，我这里给构造函数，动态配置按钮数量，这样未来加减按钮时，可以更加不用改代码，只要加代码即可。

这里我设计了一个接口，叫`IUICallback`
这个是为了，方便AI在进行运算时，修改一些UI显示效果。比如让悔棋按钮暂时不可用，在消息栏里显示AI正在思考，让玩家知道不是程序出故障了。
```
public class GameSettingPanel extends JPanel implements IUICallback {
    public static final String UNDO = "Undo";
    public static final String START_GAME = "Start Game";
    @Getter
    private ButtonGroup firstPlay;
    @Getter
    private ButtonGroup secondPlay;
    
    private MessagingPanel messagingPanel;

    private int restButtonNumber;

    private Map<String, JButton> buttonMap;

    public GameSettingPanel(int buttonNumber) {
        super(new GridLayout(3 + buttonNumber, 1));
        if (buttonNumber < 1) throw new IllegalArgumentException("buttonNumber should be postive");
        messagingPanel = new MessagingPanel();
        this.add(messagingPanel);
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
        messagingPanel.AIStart();
        if (buttonMap.containsKey(UNDO))
            buttonMap.get(UNDO).setEnabled(false);
    }

    @Override
    public void onAIThinkingDone() {
        messagingPanel.AIDone();
        if (buttonMap.containsKey(UNDO))
            buttonMap.get(UNDO).setEnabled(true);
    }
}
```

## 第二步我们完成一下五子棋的规则，实现人人对战

我们有了UI类之后，下一步我需要一些算法类。

比如说棋盘，我们需要二维数组去存当前摆放的棋子。然后暴露一些接口，比如下了这步之后，是否结束，是否是个合法可下的位置。

为了表示位置，我们写一个POSITION的类。
```
public final class Position {
    public static Position EMPTY = null;
    public final int y;
    public final int x;
```
那么对棋盘UI的接口就如下：
```
public interface IChessboardUIAlgo {
    boolean isLegalMove(Piece piece);

    void setPiece(Piece piece);

    boolean isTerminal(Piece piece);

    // serialize Deque<Position> to a string
    String generateStepsCode();

    Deque<Position> getAllSteps();

}
```

这里有些小伙伴会问为啥接口里要用个UI字眼。

因为我们用到一个接口隔离的思想。比如有些类就是为UI服务的，不应该让具体的AI算法去感知，比如我们设计的Piece, 他是一个可绘制的类。

而AI算法本身不该感知他们，所以我拆了2个接口，一个是 `IChessboardUIAlgo`, 而将来我们要实现的AI相关的棋盘接口叫`IChessboardAIAlgo`

那么基于上述思想，我们UI的组件只要和`IChessboardUIAlgo`打交道即可。AI的算法模块只要和`IChessboardAIAlgo`，会使得代码更加清晰。


然后我们就可以根据这个接口自己定制不同的实现。比如很直观的，我们可以用一个二维数组去存棋盘。我这里为了节约内存空间和更好的利用缓存局部性 去 优化后序的算法速度。用了一个byte[]，原因是我们每个格子的值只有0,1,2 三种情况。所以用1个字节即可。
```
public class ChessboardByteArrayAlgo implements IChessboardAlgo {
    @Getter
    protected final byte[] location;
    @Getter
    protected final int size;

    protected Deque<Position> allSteps;

    public ChessboardByteArrayAlgo(int size) {
        this.size = size;
        location = new byte[size * size];
        allSteps = new ArrayDeque<>();
    }

    @Override
    public boolean isLegalMove(Piece p) {
        ...
    }
```
我们看一下`chessboad`这个UI类里是怎么使用这个接口的

```
public class Chessboard extends JPanel {
    @Getter @Setter
    private IChessboardUIAlgo chessBoardAlgo;
    
    public boolean addPiece(Piece piece) {
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

    public boolean isWin(Piece piece) {
        checkChessboardAlgoExist();

        return isEnd = chessBoardAlgo.isTerminal(piece);
    }

    public boolean isEnd() {
        return isEnd;
    }
```

下面小伙伴可以自己根据这个UI接口，去写一个棋盘UI算法的实现类。

最后为了完成人人对战。我们还需要棋手 和 主程序 2个模块没有写。

棋手其实当时在设计棋子时，我们已经考虑到了。是个枚举类，他的职责1个是思考下在哪（对AI来说，就是跑AI的算法。对HUMAN来说，就是等待鼠标点击事件）

另一个就是真的去和棋盘交互，去下这步棋。

那么我们需要给棋手注册上他的下棋策略，策略比如可以是人类操控，又或者是不同的AI算法。

下面是我的代码
```
@Getter
public enum Player {
    WHITE(Color.white, 1), BLACK(Color.black, 2);

    private Color color;
    private int id;

    @Setter @Getter
    private IPlayStrategy playStrategy;

    Player(Color color, int id) {
        this.color = color;
        this.id = id;
    }

    public Player doSwitch() {
        return this == WHITE ? BLACK : WHITE;
    }

    public static int enemyColor(int role) {
        assert role == 1 || role == 2;
        return 3 - role;
    }

    public boolean play(Piece piece, Chessboard chessboard, int previousGameId) {
        if (playStrategy == null) throw new IllegalStateException("invalid area, miss playStrategy");
        if (piece == null) return false;
        if (chessboard.addPiece(piece, previousGameId)) {
            for (Player p : Player.values()) {
                p.getPlayStrategy().setPieceCallback(piece, getPlayStrategy().isAI());
            }
            return true;
        }
        return false;
    }

    public Piece decidePieceIfFailReturnNull() {
        if (playStrategy == null) throw new IllegalStateException("invalid area, miss playStrategy");
        Position pos = playStrategy.decidePiecePos();
        if (pos == Position.EMPTY) return null;
        return new Piece(pos.x, pos.y, this);
    }
}
```

最后是主程序, 主程序主要负责运行游戏规则，初始化和调度各个UI组件。

```
public class GomokuGame {
    private JFrame frame;
    private Chessboard chessboard;
    // 默认黑棋先手
    private Player curPlayer = Player.BLACK;
    private volatile Thread t;

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
```
这里我们额外开了一个线程，把鼠标点击棋盘后发生的事情与主线程异步化。这样的好处是AI在思考时，我们主线程不用BLOCK在那。比如用户依然可以中断AI思考，重新再开一把游戏。同时也可以让主线程这边直接绘制用户的落子，不然的话，用户点击之后，棋盘没有反应。要等AI思考完成，才会同时绘制2个棋子，使得交互不友好。

## 总结
综上，我们完成了五子棋，人人对战的大致框架，下一章我们会开始讲AI算法的环节。

这一章我希望，你能够从代码中学习到几个基本思想。
#### 面试对象设计：
1. 单一职责（一个类设计，有他自己的职责，不用把很多职责都堆进一个类里）
2. 面向接口 （设计的时候，优先考虑接口而非实现，面向接口编程）
3. 接口隔离 （让固定类的和固定的接口交互，以减少维护成本和出错可能）
4. 对修改关闭，对增加开放 （写的代码尽可能考虑，未来可以通过增加类或接口的形式 去 迭代功能，而非去修改已有的类或接口或函数）
#### JAVA UI 类的使用
熟悉button, panel, JFrame, layout, text 等
#### 完成五子棋的基本判断逻辑
- 如何判断是否练成五子，游戏结束。
- 如何判断一个落子是否合法
- 棋盘应该使用什么数据结构
- 主程序如何保证玩家交互走棋

下一章我们从设计一个最简单的会下五子棋的AI算法开始。
