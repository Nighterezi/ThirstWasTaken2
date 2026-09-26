package com.thirstwastaken2.gametest;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.HealthRegen;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

/**
 * Dehydration stops natural regeneration, and the food cost vanilla would have charged for the
 * skipped heal is refunded.
 *
 * <p>The refund is the half that is easy to lose: vanilla spends exhaustion for every point it
 * heals, so withholding a heal without giving that back drains hunger for a heal the player never
 * received. {@code hydratedPlayerStillRegenerates} is the positive control, without which the other
 * integration test would pass even if regeneration never triggered at all.
 */
public final class HealthRegenGameTest {
    /** Enough ticks for vanilla's ten-tick regeneration timer to fire several times. */
    private static final int TICKS = 40;
    private static final float START_HEALTH = 10.0F;
    /** Exactly one of quenched's heals, which keep vanilla's ten-tick beat. */
    private static final int QUENCHED_HEAL_TICKS = 10;

    @GameTest
    public void dehydrationBlocksTheSaturationHeal(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        setThirst(player, 10);
        TestFixtures.check(helper, HealthRegen.blocksSaturationHeal(player),
                "thirst 10 should block the saturation heal");

        setThirst(player, 20);
        TestFixtures.check(helper, !HealthRegen.blocksSaturationHeal(player),
                "full thirst should not block anything");
        helper.succeed();
    }

    @GameTest
    public void nearlyHydratedPlayerHealsSlowly(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        setThirst(player, 19);
        TestFixtures.check(helper, HealthRegen.blocksSaturationHeal(player),
                "thirst 19 is still short of full, so the immediate heal is withheld");
        TestFixtures.check(helper, !HealthRegen.allowsSlowHeal(player, HealthRegen.SLOW_FACTOR - 1),
                "the heal should not be let through before " + HealthRegen.SLOW_FACTOR + " were skipped");
        TestFixtures.check(helper, HealthRegen.allowsSlowHeal(player, HealthRegen.SLOW_FACTOR),
                "the heal should be let through once " + HealthRegen.SLOW_FACTOR + " were skipped");

        setThirst(player, 10);
        TestFixtures.check(helper, !HealthRegen.allowsSlowHeal(player, HealthRegen.SLOW_FACTOR),
                "a properly dehydrated player should never be let through");
        helper.succeed();
    }

    @GameTest
    public void hungerHealIsBlockedOnlyWhileDehydrated(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        setThirst(player, 18);
        TestFixtures.check(helper, HealthRegen.blocksHungerHeal(player),
                "thirst 18 should block the hunger-driven heal");

        setThirst(player, 19);
        TestFixtures.check(helper, !HealthRegen.blocksHungerHeal(player),
                "thirst 19 is above the threshold and should not block it");
        helper.succeed();
    }

    @GameTest
    public void dehydratedPlayerDoesNotRegenerateAndKeepsItsFood(GameTestHelper helper) {
        ServerPlayer player = readyToRegenerate(helper, 10);
        FoodData food = player.getFoodData();
        float saturationBefore = food.getSaturationLevel();
        int foodBefore = food.getFoodLevel();

        for (int i = 0; i < TICKS; i++) food.tick(player);

        TestFixtures.check(helper, player.getHealth() == START_HEALTH,
                "a dehydrated player must not regenerate, health went to " + player.getHealth());
        TestFixtures.check(helper, food.getFoodLevel() == foodBefore,
                "the withheld heal must be refunded, food went to " + food.getFoodLevel());
        TestFixtures.check(helper, food.getSaturationLevel() == saturationBefore,
                "the withheld heal must be refunded, saturation went to " + food.getSaturationLevel());
        helper.succeed();
    }

    @GameTest
    public void hydratedPlayerStillRegenerates(GameTestHelper helper) {
        ServerPlayer player = readyToRegenerate(helper, 20);
        FoodData food = player.getFoodData();

        for (int i = 0; i < TICKS; i++) food.tick(player);

        TestFixtures.check(helper, player.getHealth() > START_HEALTH,
                "a hydrated player with full food must still regenerate, health stayed at "
                        + player.getHealth());
        helper.succeed();
    }

    @GameTest
    public void fullThirstHealsFromQuenchedAtTheConfiguredShare(GameTestHelper helper) {
        ServerPlayer player = hurtWithQuenched(helper, ThirstData.MAX, ThirstConfig.get().quenchedHealMinFood);

        for (int i = 0; i < QUENCHED_HEAL_TICKS; i++) ThirstManager.tickPlayer(player);

        // One heal of the full six, at the configured share of vanilla's saturation heal.
        float share = (float) ThirstConfig.get().quenchedHealthRegen;
        TestFixtures.check(helper, share > 0.0F, "quenched healing should be on by default");
        TestFixtures.check(helper, Math.abs(player.getHealth() - (START_HEALTH + share)) < 1.0E-4F,
                "one quenched heal should restore " + share + ", health went to " + player.getHealth());
        TestFixtures.check(helper, Math.abs(ThirstManager.get(player).exhaustion() - 6.0F * share) < 1.0E-4F,
                "the heal should cost " + 6.0F * share + " exhaustion, got " + ThirstManager.get(player));
        helper.succeed();
    }

    @GameTest
    public void quenchedDoesNotHealWithoutFullThirstEnoughFoodOrTheSetting(GameTestHelper helper) {
        int minFood = ThirstConfig.get().quenchedHealMinFood;
        TestFixtures.check(helper, minFood > 0, "quenched healing should need some food by default");
        ServerPlayer thirsty = hurtWithQuenched(helper, ThirstData.MAX - 1, minFood);
        ServerPlayer hungry = hurtWithQuenched(helper, ThirstData.MAX, minFood - 1);
        for (int i = 0; i < QUENCHED_HEAL_TICKS; i++) {
            ThirstManager.tickPlayer(thirsty);
            ThirstManager.tickPlayer(hungry);
        }
        TestFixtures.check(helper, thirsty.getHealth() == START_HEALTH,
                "thirst short of full should not heal from quenched, health went to " + thirsty.getHealth());
        TestFixtures.check(helper, hungry.getHealth() == START_HEALTH,
                "food under " + minFood + " should not heal from quenched, health went to " + hungry.getHealth());

        TestFixtures.withConfig(config -> config.quenchedHealthRegen = 0.0, () -> {
            ServerPlayer off = hurtWithQuenched(helper, ThirstData.MAX, 20);
            for (int i = 0; i < QUENCHED_HEAL_TICKS; i++) ThirstManager.tickPlayer(off);
            TestFixtures.check(helper, off.getHealth() == START_HEALTH,
                    "0% should turn quenched healing off, health went to " + off.getHealth());
        });
        helper.succeed();
    }

    /** A hurt player with a full reserve of quenched, nothing vanilla would heal from, and no exhaustion. */
    private static ServerPlayer hurtWithQuenched(GameTestHelper helper, int thirst, int food) {
        // The thirst tick skips an invulnerable player, and a plain mock player is creative.
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        player.setHealth(START_HEALTH);
        player.getFoodData().setFoodLevel(food);
        player.getFoodData().setSaturation(0.0F);
        ThirstManager.set(player, new ThirstData(thirst, thirst, 0.0F, true));
        return player;
    }

    /** A hurt player with enough food and saturation for vanilla's saturated regeneration branch. */
    private static ServerPlayer readyToRegenerate(GameTestHelper helper, int thirst) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);
        player.setHealth(START_HEALTH);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        setThirst(player, thirst);
        return player;
    }

    private static void setThirst(ServerPlayer player, int thirst) {
        ThirstManager.set(player, ThirstManager.get(player).withLevels(thirst, 0));
    }
}
