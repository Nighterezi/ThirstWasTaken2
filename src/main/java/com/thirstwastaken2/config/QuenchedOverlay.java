package com.thirstwastaken2.config;

/**
 * The colour quenched is outlined in, on the thirst bar and in the tooltip's quenched row, while
 * AppleSkin is installed.
 *
 * <p>The order is load-bearing: the coloured values are the rows of
 * {@code textures/gui/quenched_overlay.png} and pairs of glyphs from U+E008 in
 * {@code textures/font/droplets.png}, both written by {@code tools/generate_quenched_overlay.py} in
 * this same order. The config file stores the name, so renaming a value resets it to the default.
 */
public enum QuenchedOverlay {
    DIAMOND,
    ICE,
    GOLD,
    /** The gold AppleSkin draws saturation in, filling in the same direction its drumstick does. */
    APPLESKIN,
    /**
     * The original Thirst Was Taken's blue outline. Its full frame is the original's; the partial frames
     * grow from it like the others, since the original's were AppleSkin's drumstick pieces in blue.
     */
    LEGACY,
    /** No outline on the thirst bar; the tooltip row keeps the plain blue outline it always had. */
    OFF
}
