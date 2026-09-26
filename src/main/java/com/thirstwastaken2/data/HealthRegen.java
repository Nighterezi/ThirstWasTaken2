package com.thirstwastaken2.data;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.platform.Vanilla;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Whether a dehydrated player may still regenerate health, reproducing the original mod's
 * MixinFoodData rules.
 *
 * <p>Vanilla charges food for every point it heals. Blocking a heal therefore has to refund that
 * cost, or hunger drains for a heal the player never received — see {@code FoodDataMixin}.
 *
 * <p>Also the heal quenched gives a player whose thirst is full, {@link #healWithQuenched}.
 */
public final class HealthRegen {
    /** A nearly-hydrated player still regenerates, just this many times slower. */
    public static final int SLOW_FACTOR = 8;
    /** Ceiling on the refund, matching the exhaustion vanilla spends per heal. */
    public static final float MAX_REFUND = 6.0F;
    /** Above this the player counts as nearly hydrated and regeneration is only slowed, not stopped. */
    private static final int NEARLY_HYDRATED = 18;
    /** Vanilla's saturation heal comes every this many ticks; quenched's keeps the same beat. */
    private static final int QUENCHED_HEAL_INTERVAL = 10;
    /** The most one heal draws on, as vanilla caps the saturation one heal spends at 6. */
    private static final float QUENCHED_PER_HEAL = 6.0F;

    private HealthRegen() { }

    /**
     * Heals the player from quenched the way vanilla heals from saturation, scaled by
     * {@code quenchedHealthRegen}, and returns the thirst exhaustion that heal costs, zero on a tick that
     * does not heal. Called once per tick by {@code ThirstManager.tickPlayer}, which adds the cost to its
     * one write. Like vanilla, the heal stacks with the food one: both bars full heal faster.
     */
    static float healWithQuenched(ServerPlayer player, ThirstData data, ExhaustionTracker tracker) {
        ThirstConfig config = ThirstConfig.get();
        double scale = config.quenchedHealthRegen;
        if (scale <= 0.0 || data.thirst() < ThirstData.MAX || data.quenched() <= 0 || !player.isHurt()
                || player.getFoodData().getFoodLevel() < config.quenchedHealMinFood
                || !Vanilla.naturalRegeneration(player)) {
            tracker.quenchedHealTimer = 0;
            return 0.0F;
        }
        if (++tracker.quenchedHealTimer < QUENCHED_HEAL_INTERVAL) return 0.0F;
        tracker.quenchedHealTimer = 0;
        // Vanilla heals f / 6 for f exhaustion; scaling both keeps quenched's rate of exchange with it.
        float spent = (float) scale * Math.min(data.quenched(), QUENCHED_PER_HEAL);
        player.heal(spent / QUENCHED_PER_HEAL);
        return spent;
    }

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
