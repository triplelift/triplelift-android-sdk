package com.triplelift.sdk;

public class NativeFeedPositions {

    private final int repeatInterval;
    private final int[] fixedPositions;

    public NativeFeedPositions(int[] fixedPositions, int repeatInterval) {
        this.repeatInterval = repeatInterval;
        this.fixedPositions = fixedPositions;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public int[] getFixedPositions() {
        return fixedPositions;
    }
}
