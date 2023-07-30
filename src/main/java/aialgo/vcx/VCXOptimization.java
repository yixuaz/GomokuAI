package aialgo.vcx;

import scorecalculator.Score;

public enum VCXOptimization {
    FAST(2){
        @Override
        public boolean matchKillSteps(int aiScore) {
            return false;
        }
    }, MEDIUM(4) {
        @Override
        public boolean matchKillSteps(int aiScore) {
            return aiScore >= Score.BLOCKED_FOUR.value;
        }
    }, SLOW(8) {
        @Override
        public boolean matchKillSteps(int aiScore) {
            return aiScore >= Score.THREE.value;
        }
    };
    int factor;

    VCXOptimization(int factor) {
        this.factor = factor;
    }

    public abstract boolean matchKillSteps(int aiScore);
}
