package com.thirstwastaken2.data;

import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.world.entity.player.Player;

/**
 * Whether a dehydrated player may still regenerate health, reproducing the original mod's
 * MixinFoodData rules.
 *
 * <p>Vanilla charges food for every point it heals. Blocking a heal therefore has to refund that
 * cost, or hunger drains for a heal the player never received — see {@code FoodDataMixin}.
 */
public final class HealthRegen {
    /** A nearly-hydrated player still regenerates, just this many times slower. */
    public static final int SLOW_FACTOR = 8;
    /** Ceiling on the refund, matching the exhaustion vanilla spends per heal. */
    public static final float MAX_REFUND = 6.0F;
    /** Above this the player counts as nearly hydrated and regeneration is only slowed, not stopped. */
    private static final int NEARLY_HYDRATED = 18;

    private HealthRegen() { }

    /** Whether the saturation-driven heal has to be withheld from this player. */
    public static boolean blocksSaturationHeal(Player player) {
        return ThirstConfig.get().dehydrationHaltsHealthRegen
                && ThirstManager.get(player).thirst() < ThirstData.MAX;
    }

    /** Whether a withheld saturation heal is let through anyway, once every {@link #SLOW_FACTOR}. */
    public static boolean allowsSlowHeal(Player player, int skipped) {
        return skipped >= SLOW_FACTOR && ThirstManager.get(player).thirst() > NEARLY_HYDRATED;
    }

    /** Whether the hunger-driven heal has to be withheld from this player. */
    public static boolean blocksHungerHeal(Player player) {
        return ThirstConfig.get().dehydrationHaltsHealthRegen
                && ThirstManager.get(player).thirst() <= NEARLY_HYDRATED;
    }
}
