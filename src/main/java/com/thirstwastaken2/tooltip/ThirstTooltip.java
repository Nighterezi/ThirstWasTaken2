package com.thirstwastaken2.tooltip;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/**
 * Renders item hydration as two droplet rows instead of numbers. Thirst uses filled droplets on the
 * first row and quenched uses outline droplets on the second, matching the layout used by other
 * thirst integrations.
 *
 * <p>The droplets are glyphs of the {@code thirstwastaken2:droplets} bitmap font rather than a
 * {@code ClientTooltipComponent}. That keeps each line an ordinary {@link Component}, so it survives
 * the whole tooltip pipeline unchanged in vanilla screens as well as REI, EMI and JEI.
 */
public final class ThirstTooltip {
    private static final FontDescription FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(ThirstWasTaken2.MOD_ID, "droplets"));

    /** One droplet holds two units, matching {@code ThirstHud}'s fill thresholds. */
    private static final int UNITS_PER_DROPLET = 2;
    /** Ten droplets is a full bar; anything beyond that is capped rather than wrapped. */
    private static final int MAX_DROPLETS = 10;

    private static final char THIRST_FULL = '\uE000';
    private static final char THIRST_HALF = '\uE001';
    private static final char QUENCHED_FULL = '\uE004';
    private static final char QUENCHED_HALF = '\uE007';

    private ThirstTooltip() { }

    /** @return the filled thirst row, or {@code null} when the item restores no thirst. */
    public static Component thirst(int hydration) {
        return row(hydration, THIRST_FULL, THIRST_HALF);
    }

    /** @return the outline quenched row, or {@code null} when the item restores no quenched. */
    public static Component quenched(int quenched) {
        return row(quenched, QUENCHED_FULL, QUENCHED_HALF);
    }

    private static Component row(int units, char full, char half) {
        int droplets = Math.min(droplets(units), MAX_DROPLETS);
        if (droplets == 0) return null;

        StringBuilder icons = new StringBuilder(droplets);
        for (int i = 0; i < droplets; i++) {
            icons.append(fillOf(units, i) == UNITS_PER_DROPLET ? full : half);
        }
        return Component.literal(icons.toString()).withStyle(style -> style
                .withFont(FONT)
                // Bitmap glyphs keep their own palette; shadows would smear their 1px outlines.
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
