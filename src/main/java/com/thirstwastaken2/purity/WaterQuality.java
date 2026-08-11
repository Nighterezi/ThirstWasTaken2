package com.thirstwastaken2.purity;

/** A sampled body of water. Contamination and salinity deliberately remain independent. */
public record WaterQuality(int contamination, boolean salty) {
    public static final int MIN_CONTAMINATION = 0;
    public static final int MAX_CONTAMINATION = 100;

    public WaterQuality {
        contamination = Math.max(MIN_CONTAMINATION, Math.min(MAX_CONTAMINATION, contamination));
    }

    public int purity() {
        if (contamination <= 15) return 3;
        if (contamination <= 35) return 2;
        if (contamination <= 65) return 1;
        return 0;
    }

    public static WaterQuality fromPurity(int purity, boolean salty) {
        int representative = switch (Math.max(WaterPurity.MIN, Math.min(WaterPurity.MAX, purity))) {
            case 0 -> 80;
            case 1 -> 50;
            case 2 -> 25;
            default -> 5;
        };
        return new WaterQuality(representative, salty);
    }
}
