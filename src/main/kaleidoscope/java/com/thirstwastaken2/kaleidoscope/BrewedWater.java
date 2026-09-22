package com.thirstwastaken2.kaleidoscope;

import com.thirstwastaken2.purity.WaterQuality;

/**
 * A Kaleidoscope Cookery block that holds water: the stockpot and the teapot, each through its own
 * mixin. It is how a second mixin on the same block and the Jade reader reach the grade, and it names no
 * class of the mod's, so the Jade reader can ask it whether or not the mod is installed.
 */
public interface BrewedWater {
    /** The grade of the water the block holds now, or {@code null} when it holds none or holds another fluid. */
    WaterQuality thirst$heldWater();

    /** What water just poured in by something other than a container, dripstone on 1.21.1, is graded as. */
    void thirst$holdWater(WaterQuality quality);
}
