package com.thirstwastaken.tooltip;

import com.thirstwastaken.ThirstWasTaken;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/**
 * Renders the hydration line as droplets instead of numbers, the way the HUD already shows them.
 *
 * <p>Hydration and quenched share one row of droplets rather than sitting in two groups: quenched is
 * layered over the water inside the same droplet, exactly as {@code ThirstHud} draws its overlay.
 * The two never hide each other, because they occupy different parts of the sprite — water fills the
 * body bottom-up, quenched traces the outline — so a droplet still reports both values at a glance,
 * and the row is only {@code max(hydration, quenched)} droplets wide.
 *
 * <p>The droplets are glyphs of the {@code thirstwastaken:droplets} bitmap font rather than a
 * {@code ClientTooltipComponent}. That keeps the line an ordinary {@link Component}, so it survives
 * the whole tooltip pipeline unchanged — vanilla screens, but equally REI, EMI and JEI, none of
 * which route third-party tooltip components through their own renderers.
 */
public final class ThirstTooltip {
    private static final FontDescription FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(ThirstWasTaken.MOD_ID, "droplets"));

    /** One droplet holds two units, matching {@code ThirstHud}'s fill thresholds. */
    private static final int UNITS_PER_DROPLET = 2;
    /** Ten droplets is a full bar; anything beyond that is capped rather than wrapped. */
    private static final int MAX_DROPLETS = 10;

    /**
     * Private-use code points of the glyph sheet, indexed {@code [water units][quenched units]}.
     * A droplet with neither is never emitted, so that slot stays unused.
     */
    private static final char UNUSED = '\0';
    private static final char[][] GLYPHS = {
            //  quenched: none      half        full
            /* no water   */ {UNUSED, '\uE007', '\uE004'},
            /* half water */ {'\uE001', '\uE006', '\uE003'},
            /* full water */ {'\uE000', '\uE005', '\uE002'},
    };

    private ThirstTooltip() { }

    /** @return the droplet row for what this item restores, or {@code null} when it restores nothing. */
    public static Component hydration(int hydration, int quenched) {
        int droplets = Math.min(Math.max(droplets(hydration), droplets(quenched)), MAX_DROPLETS);
        if (droplets == 0) return null;

        StringBuilder icons = new StringBuilder(droplets);
        for (int i = 0; i < droplets; i++) {
            icons.append(GLYPHS[fillOf(hydration, i)][fillOf(quenched, i)]);
        }
        return Component.literal(icons.toString()).withStyle(style -> style
                .withFont(FONT)
                // Bitmap glyphs are tinted by the text colour, so they have to stay white to keep
                // their own palette, and the tooltip drop shadow would smear the 1px outlines.
                .withColor(0xFFFFFF)
                .withoutShadow());
    }

    /** Droplets needed to show this many units, rounding a leftover half up to its own droplet. */
    private static int droplets(int units) {
        return (Math.max(units, 0) + UNITS_PER_DROPLET - 1) / UNITS_PER_DROPLET;
    }

    /** How full droplet {@code index} is: 2 whole, 1 half, 0 empty. */
    private static int fillOf(int units, int index) {
        return Math.clamp(units - index * UNITS_PER_DROPLET, 0, UNITS_PER_DROPLET);
    }
}
