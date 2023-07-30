package scorecalculator;

public enum Score {
    ONE(10), TWO(100),THREE(1000),FOUR(100_000),FIVE(10_000_000),BLOCKED_ONE(1), BLOCKED_TWO(10),BLOCKED_THREE(100),BLOCKED_FOUR(10_000);

    public final int value;

    Score(int value) {
        this.value = value;
    }

    public static int calculateSingleEmpty(int emptyPos, int count, boolean emptySideBlock, boolean nonEmptySideBlock) {
        if (emptyPos == 1) {
            // 1 0 x x x
            if (count >= 6) return FIVE.value;
            if (!emptySideBlock && !nonEmptySideBlock) {
                switch (count) {
                    case 2: return TWO.value / 2;
                    case 3: return THREE.value;
                    case 4: return BLOCKED_FOUR.value;
                    case 5: return FOUR.value;
                }
            }else if (!emptySideBlock || !nonEmptySideBlock) {
                switch (count) {
                    case 2: return BLOCKED_TWO.value;
                    case 3: return BLOCKED_THREE.value;
                    case 4: return BLOCKED_FOUR.value;
                    case 5: return (emptySideBlock ? FOUR : BLOCKED_FOUR).value;
                }
            } else {
                assert emptySideBlock && nonEmptySideBlock;
                if (count >= 4) return BLOCKED_FOUR.value;
            }

        }
        else if (emptyPos == 2) {
            assert count >= 3;
            // 1 1 0 1 1 1 1 1
            if (count >= 7) return FIVE.value;
            if (!emptySideBlock && !nonEmptySideBlock) {
                switch (count) {
                    // 1 1 0 x
                    case 3: return THREE.value;
                    // 1 1 0 x x (x)
                    case 4:
                    case 5: return BLOCKED_FOUR.value;
                    // 1 1 0 1 1 1 1
                    case 6: return FOUR.value;
                }
            }else if (!emptySideBlock || !nonEmptySideBlock) {
                switch (count) {
                    // x 1 1 0 1, 1 1 0 1 X
                    case 3: return BLOCKED_THREE.value;
                    // x 1 1 0 1 1, 1 1 0 1 1 X
                    case 4:
                    case 5: return BLOCKED_FOUR.value;
                    // x 1 1 0 1 1 1 1, 1 1 0 1 1 1 1 X
                    case 6: return (emptySideBlock ? FOUR : BLOCKED_FOUR).value;
                }
            } else {
                // x 1 1 0 1 1 (1) (1) (1) x
                assert emptySideBlock && nonEmptySideBlock;
                if (count >= 4) return BLOCKED_FOUR.value;
            }
        }
        else if (emptyPos == 3) {
            assert count >= 4;
            if (count >= 8) return FIVE.value;
            if (!emptySideBlock && !nonEmptySideBlock) {
                switch (count) {
                    // 1 1 1 0 1
                    case 4:
                        // 1 1 1 0 1 1
                    case 5:
                        // 1 1 1 0 1 1 1
                    case 6: return BLOCKED_FOUR.value;
                    // 1 1 1 0 1 1 1 1
                    case 7: return FOUR.value;
                }
            }else if (!emptySideBlock || !nonEmptySideBlock) {
                switch (count) {
                    // 1 1 1 0 1 x, x 1 1 1 0 1
                    case 4:
                        // 1 1 1 0 1 1 x, x 1 1 1 0 1 1
                    case 5:
                        // 1 1 1 0 1 1 1 x, x 1 1 1 0 1 1 1
                    case 6: return BLOCKED_FOUR.value;
                    // 1 1 1 0 1 1 1 1 x, x 1 1 1 0 1 1 1 1
                    case 7: return (emptySideBlock ? FOUR : BLOCKED_FOUR).value;
                }
            } else {
                // x 1 1 1 0 1 (1) (1)  x
                assert emptySideBlock && nonEmptySideBlock;
                if (count >= 4) return BLOCKED_FOUR.value;
            }
        }
        else if (emptyPos == 4) {
            assert count >= 5;
            if (count >= 9) return FIVE.value;
            if (!emptySideBlock && !nonEmptySideBlock) {
                switch (count) {
                    // 1 1 1 1 0 1
                    case 5:
                        // 1 1 1 1 0 1 1
                    case 6:
                        // 1 1 1 1 0 1 1 1
                    case 7:
                        // 1 1 1 1 0 1 1 1 1
                    case 8: return FOUR.value;
                }
            }else if (!emptySideBlock || !nonEmptySideBlock) {
                switch (count) {
                    // 1 1 1 1 0 1 x, x 1 1 1 1 0 1
                    case 5:
                        // 1 1 1 1 0 1 1 x, x 1 1 1 1 0 1 1
                    case 6:
                        // 1 1 1 1 0 1 1 1 x, x 1 1 1 1 0 1 1 1
                    case 7: return (nonEmptySideBlock ? FOUR : BLOCKED_FOUR).value;
                    case 8: return FOUR.value;
                }
            } else {
                // x 1 1 1 1 0 1 (1) (1) (1) x
                assert emptySideBlock && nonEmptySideBlock;
                if (count >= 5) return BLOCKED_FOUR.value;
            }
        }
        else if (emptyPos >= 5) {
            return FIVE.value;
        } else {
            throw new IllegalStateException("");
        }
        return 0;
    }

    public static int calculateNonEmpty(int count, boolean blocked) {
        assert count >= 1 && count <= 4;
        switch (count) {
            case 1 : return (blocked ? BLOCKED_ONE : ONE).value;
            case 2 : return (blocked ? BLOCKED_TWO : TWO).value;
            case 3 : return (blocked ? BLOCKED_THREE : THREE).value;
            case 4 : return (blocked ? BLOCKED_FOUR : FOUR).value;
        }
        throw new IllegalStateException("impossible");
    }

    public static int calculateDoubleEmpty(int leftPartA, int leftPartB, boolean leftBlock,
                                           int rightPartA, int rightPartB, boolean rightBlock) {
        assert leftPartA <= rightPartB;
        assert leftPartA > 0 && rightPartB > 0;
        // (leftPartA) 0 (leftPartB) 1 (rightPartA) 0 (rightPartB)
        if (rightPartB >= 5 || leftPartA >= 5) return FIVE.value;
        if (leftPartB + 1 + rightPartA >= 5) return FIVE.value;
        if (leftPartB + 1 + rightPartA == 4) return FOUR.value;
        if (leftPartA == 4 && !leftBlock) return FOUR.value;
        if (rightPartB == 4 && !rightBlock) return FOUR.value;
        if (rightPartB == 4 && leftPartA == 4) return FOUR.value;
        if (leftPartB + 1 + rightPartA == 3) {
            // (leftPartA) 0 1 1 1 0 (rightPartB)
            return FOUR.value;
        }
        if (leftPartB + 1 + rightPartA == 2) {
            // (leftPartA[1-4]) 0 1 1 0 (rightPartB[1-4])
            if (leftPartA >= 2 && rightPartB >= 2) return FOUR.value;
            assert leftPartA == 1;
            if (rightPartB >= 2) return BLOCKED_FOUR.value;
            assert rightPartB == 1;
            return THREE.value;
        }
        if (leftPartB + rightPartA == 0) {
            // (leftPartA[1-4]) 0 1 0 (rightPartB[1-4])
            if (leftPartA >= 3 && rightPartB >= 3) return FOUR.value;
            if (leftPartA == 2) return BLOCKED_FOUR.value;
            assert leftPartA == 1;
            if (rightPartB >= 3) return BLOCKED_FOUR.value;
            if (rightPartB == 2) return THREE.value;
            assert rightPartB == 1;
            return TWO.value;
        }
        throw new IllegalStateException("invalid area");
    }

    //冲四活三这种杀棋，则将分数提高。
    public static int fixScore(int type) {
        if(type < FOUR.value && type >= BLOCKED_FOUR.value + THREE.value) {
            return FOUR.value;
        }
        return type;
    }
}
