package com.thirstwastaken2.tooltip;

import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.platform.Vanilla;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

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
    /** One droplet holds two units, matching {@code ThirstHud}'s fill thresholds. */
    private static final int UNITS_PER_DROPLET = 2;
    /** Ten droplets is a full bar; anything beyond that is capped rather than wrapped. */
    private static final int MAX_DROPLETS = 10;
    /** Units past a full row of droplets draw exactly like a full row. */
    private static final int MAX_UNITS = MAX_DROPLETS * UNITS_PER_DROPLET;

    private static final char THIRST_FULL = '\uE000';
    private static final char THIRST_HALF = '\uE001';
    private static final char QUENCHED_FULL = '\uE004';
    private static final char QUENCHED_HALF = '\uE007';

    /**
     * Every row that can be drawn, indexed by units. Tooltips are rebuilt every frame a stack is
     * hovered, so rows are built once and handed out as copies: restyling a line in place, which other
     * mods are free to do, must not leak into the next frame.
     */
    private static final Component[] THIRST_ROWS = rows(THIRST_FULL, THIRST_HALF);
    private static final Component[] QUENCHED_ROWS = rows(QUENCHED_FULL, QUENCHED_HALF);
    /**
     * The clay bowl is the one item whose purpose is not obvious from holding it: it has to be fired
     * before it can hold water, and using it on water does nothing until then.
     */
    private static final Component CLAY_BOWL_HINT =
            Component.translatable("tooltip.thirstwastaken2.clay_bowl").withStyle(ChatFormatting.GRAY);

    private ThirstTooltip() { }

    /**
     * Appends every line the mod contributes to an item tooltip: the clay bowl hint, waterskin fill,
     * the water's grade or salinity, then the two droplet rows.
     *
     * <p>Called once per frame per hovered stack, so both lookups it makes are memoised.
     */
    public static void appendTo(ItemStack stack, Consumer<Component> tooltip) {
        if (stack.is(ThirstItems.CLAY_BOWL)) tooltip.accept(CLAY_BOWL_HINT.copy());
        if (stack.is(ThirstItems.WATERSKIN)) {
            int servings = WaterskinItem.servings(stack);
            // Grey, like the clay bowl hint: how full the skin is describes the item, while the grade
            // and the droplet rows below say what drinking it does.
            tooltip.accept((servings == 0
                    ? Component.translatable("tooltip.thirstwastaken2.waterskin.empty")
                    : Component.translatable("tooltip.thirstwastaken2.waterskin.servings",
                            servings, WaterskinItem.CAPACITY))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (WaterPurity.isWaterContainer(stack)) {
            switch (WaterPurity.quality(stack)) {
                case WaterQuality.Salt ignored -> {
                    // Salt water has no grade to report and restores nothing, so it gets one line of
                    // its own and no droplet rows to contradict it.
                    tooltip.accept(WaterPurity.saltTooltip());
                    return;
                }
                case WaterQuality.Fresh fresh -> tooltip.accept(WaterPurity.tooltip(fresh.purity()));
            }
        }
        int[] hydration = ThirstApi.hydration(stack);
        if (hydration == null) return;
        Component thirst = thirst(hydration[0]);
        Component quenched = quenched(hydration[1]);
        if (thirst != null) tooltip.accept(thirst);
        if (quenched != null) tooltip.accept(quenched);
    }

    /** @return the filled thirst row, or {@code null} when the item restores no thirst. */
    public static Component thirst(int hydration) {
        return row(THIRST_ROWS, hydration);
    }

    /** @return the outline quenched row, or {@code null} when the item restores no quenched. */
    public static Component quenched(int quenched) {
        return row(QUENCHED_ROWS, quenched);
    }

    private static Component row(Component[] rows, int units) {
        return units <= 0 ? null : rows[Math.min(units, MAX_UNITS)].copy();
    }

    private static Component[] rows(char full, char half) {
        Component[] rows = new Component[MAX_UNITS + 1];
        for (int units = 1; units <= MAX_UNITS; units++) rows[units] = build(units, full, half);
        return rows;
    }

    private static Component build(int units, char full, char half) {
        int droplets = droplets(units);
        StringBuilder icons = new StringBuilder(droplets);
        for (int i = 0; i < droplets; i++) {
            icons.append(fillOf(units, i) == UNITS_PER_DROPLET ? full : half);
        }
        return Component.literal(icons.toString()).withStyle(style -> Vanilla.dropletFont(style)
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
