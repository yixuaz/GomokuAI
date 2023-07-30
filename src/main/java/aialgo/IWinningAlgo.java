package aialgo;

// when not find forced win, it returns Position.Empty
// when find forced win, it returns position which result in win.
public interface IWinningAlgo extends IAIAlgo {
    // false is almost win
    default boolean isAbsoluteForcedWin() {
        return false;
    }
}
