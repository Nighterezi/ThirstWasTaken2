package com.thirstwastaken2.gametest;

import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/**
 * What drinking water of a given quality does to the player.
 *
 * <p>The purity tiers the default config makes deterministic are the ones tested here: tier 0 has a
 * 100 percent nausea chance and tier 3 has none, so neither depends on a dice roll. The tiers in
 * between are deliberately left alone.
 */
public final class WaterEffectsGameTest {
    @GameTest
    public void saltWaterCausesNauseaAndDoesNotHydrate(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack salty = bowl(25, true);

        boolean hydrates = WaterPurity.applyEffects(player, salty);

        TestFixtures.check(helper, !hydrates, "salt water must not grant hydration");
        TestFixtures.check(helper, player.hasEffect(MobEffects.NAUSEA), "salt water should cause nausea");
        helper.succeed();
    }

    @GameTest
    public void dirtyWaterCausesNauseaAndHunger(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        WaterPurity.applyEffects(player, bowl(WaterQuality.fromPurity(0, false)));

        TestFixtures.check(helper, player.hasEffect(MobEffects.NAUSEA),
                "purity 0 has a 100 percent nausea chance in the default config");
        TestFixtures.check(helper, player.hasEffect(MobEffects.HUNGER),
                "the nausea roll also applies hunger");
        helper.succeed();
    }

    @GameTest
    public void purifiedWaterHasNoSideEffects(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        boolean hydrates = WaterPurity.applyEffects(player, bowl(WaterQuality.fromPurity(3, false)));

        TestFixtures.check(helper, hydrates, "purified water must grant hydration");
        TestFixtures.check(helper, !player.hasEffect(MobEffects.NAUSEA) && !player.hasEffect(MobEffects.POISON),
                "purity 3 has no nausea or poison chance in the default config");
        helper.succeed();
    }

    @GameTest
    public void drinkingRaisesThirst(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ThirstManager.set(player, ThirstManager.get(player).withLevels(4, 0));

        ThirstManager.drinkItem(player, bowl(WaterQuality.fromPurity(3, false)));

        int thirst = ThirstManager.get(player).thirst();
        TestFixtures.check(helper, thirst > 4,
                "drinking a purified water bowl should raise thirst above 4, got " + thirst);
        helper.succeed();
    }

    @GameTest
    public void saltinessSurvivesBoiling(GameTestHelper helper) {
        ItemStack salty = bowl(80, true);

        WaterPurity.purify(salty, WaterPurity.MAX);

        TestFixtures.check(helper, WaterPurity.isSalty(salty),
                "purifying salt water must not desalinate it");
        TestFixtures.check(helper, WaterPurity.get(salty) == WaterPurity.MAX,
                "purifying should still raise the purity tier, got " + WaterPurity.get(salty));
        helper.succeed();
    }

    private static ItemStack bowl(int contamination, boolean salty) {
        return bowl(new WaterQuality(contamination, salty));
    }

    private static ItemStack bowl(WaterQuality quality) {
        return WaterPurity.setQuality(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
    }
}
