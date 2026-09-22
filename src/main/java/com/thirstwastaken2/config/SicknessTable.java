package com.thirstwastaken2.config;

/**
 * One difficulty's chances of falling ill from a drink, in percent, indexed by grade: Dirty, Murky,
 * Clean. Pure water is always safe, so it has no column. A drink rolls once from 0 to 100 and walks the
 * ranges from worst to mildest, so the chances of one grade add up and at most one illness is caught.
 */
public final class SicknessTable {
    /** Dirty, Murky and Clean. */
    public static final int GRADES = 3;
    public static final int MAX_UPSET_STOMACH_LEVEL = 2;

    public int[] poisoningChance;
    public int[] upsetStomachChance;
    /** Level of Upset Stomach caught, 1 or 2, by grade. Poisoning brings the same level with it. */
    public int[] upsetStomachLevel;

    /** For Gson. A table read with an array missing gets the difficulty's default in {@link #sanitize}. */
    private SicknessTable() { }

    public SicknessTable(int[] poisoningChance, int[] upsetStomachChance, int[] upsetStomachLevel) {
        this.poisoningChance = poisoningChance;
        this.upsetStomachChance = upsetStomachChance;
        this.upsetStomachLevel = upsetStomachLevel;
    }

    public static SicknessTable easy() {
        return new SicknessTable(new int[]{15, 5, 0}, new int[]{50, 30, 5}, new int[]{1, 1, 1});
    }

    public static SicknessTable normal() {
        return new SicknessTable(new int[]{25, 10, 2}, new int[]{50, 40, 10}, new int[]{2, 1, 1});
    }

    public static SicknessTable hard() {
        return new SicknessTable(new int[]{33, 20, 5}, new int[]{45, 46, 15}, new int[]{2, 2, 1});
    }

    public SicknessTable copy() {
        return new SicknessTable(poisoningChance.clone(), upsetStomachChance.clone(), upsetStomachLevel.clone());
    }

    /** {@code table} with every value in range, or {@code defaults} where it is missing or misshapen. */
    static SicknessTable sanitize(SicknessTable table, SicknessTable defaults) {
        if (table == null) return defaults;
        table.poisoningChance = clamped(table.poisoningChance, defaults.poisoningChance, 0, 100);
        table.upsetStomachChance = clamped(table.upsetStomachChance, defaults.upsetStomachChance, 0, 100);
        table.upsetStomachLevel = clamped(table.upsetStomachLevel, defaults.upsetStomachLevel, 1,
                MAX_UPSET_STOMACH_LEVEL);
        return table;
    }

    private static int[] clamped(int[] values, int[] defaults, int min, int max) {
        if (values == null || values.length != GRADES) return defaults;
        for (int i = 0; i < GRADES; i++) values[i] = Math.max(min, Math.min(max, values[i]));
        return values;
    }
}
