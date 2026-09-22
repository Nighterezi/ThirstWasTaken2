package com.thirstwastaken2.config;

import net.minecraft.world.Difficulty;

/**
 * One difficulty's chances of falling ill from a drink, in percent, indexed by grade: Dirty, Murky,
 * Clean. Pure water is always safe, so it has no column. A drink rolls once from 0 to 100 and walks the
 * ranges from worst to mildest, so the chances of one grade add up and at most one illness is caught.
 * The numbers are docs/dev/mechanics/WATER-SICKNESS.md's, fixed here rather than in the config.
 *
 * @param poisoningChance    percent per grade
 * @param upsetStomachChance percent per grade, for the drinks that did not poison
 * @param upsetStomachLevel  1 or 2 per grade; Poisoning brings the same level with it
 */
public record SicknessTable(int[] poisoningChance, int[] upsetStomachChance, int[] upsetStomachLevel) {
    /** Dirty, Murky and Clean. */
    public static final int GRADES = 3;
    public static final int MAX_UPSET_STOMACH_LEVEL = 2;

    private static final SicknessTable EASY =
            new SicknessTable(new int[]{15, 5, 0}, new int[]{50, 30, 5}, new int[]{1, 1, 1});
    private static final SicknessTable NORMAL =
            new SicknessTable(new int[]{25, 10, 2}, new int[]{50, 40, 10}, new int[]{2, 1, 1});
    private static final SicknessTable HARD =
            new SicknessTable(new int[]{33, 20, 5}, new int[]{45, 46, 15}, new int[]{2, 2, 1});

    /** The difficulty's table, or {@code null} on Peaceful, which only ever gives the taste. */
    public static SicknessTable of(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> null;
            case EASY -> EASY;
            case NORMAL -> NORMAL;
            case HARD -> HARD;
        };
    }
}
