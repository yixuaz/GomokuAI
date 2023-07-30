package aialgo.vcx;

public final class NegamaxVCXEnhancedContext {

    public static NegamaxVCXEnhancedContext DISABLE = new NegamaxVCXEnhancedContext(false, 0,0,0);
    public static NegamaxVCXEnhancedContext ATTACK = new NegamaxVCXEnhancedContext(true, 2,13,7);
    public static NegamaxVCXEnhancedContext DEFENSE = new NegamaxVCXEnhancedContext(true, 1,19,7);

    public final boolean enable;
    public final int applyDeltaDepth;
    public final int vcxDepth;
    public final int startSteps;

    public NegamaxVCXEnhancedContext(boolean enable, int applyDeltaDepth, int vcxDepth, int startSteps) {
        this.enable = enable;
        this.applyDeltaDepth = applyDeltaDepth;
        this.vcxDepth = vcxDepth;
        this.startSteps = startSteps;
    }
}
