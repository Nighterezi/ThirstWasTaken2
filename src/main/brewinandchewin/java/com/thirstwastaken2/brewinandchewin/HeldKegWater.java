package com.thirstwastaken2.brewinandchewin;

import com.thirstwastaken2.purity.WaterQuality;

/**
 * The keg, through {@code KegHeldWaterMixin}: how the Jade reader reaches the grade of the water in it.
 * It names no class of the mod's, so the Jade reader can ask it whether or not the mod is installed.
 */
public interface HeldKegWater {
    /** The grade of the water the keg holds now, or {@code null} when it holds none or holds another fluid. */
    WaterQuality thirst$heldWater();
}
