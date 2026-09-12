package com.thirstwastaken2.gametest;

import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

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
        ServerPlayer player = TestFixtures.mockPlayer(helper);
        ItemStack salty = bowl(WaterQuality.SALT);

        boolean hydrates = WaterPurity.applyEffects(player, salty);

        TestFixtures.check(helper, !hydrates, "salt water must not grant hydration");
        TestFixtures.check(helper, player.hasEffect(MobEffects.NAUSEA), "salt water should cause nausea");
        helper.succeed();
    }

    @GameTest
    public void dirtyWaterCausesNauseaAndHunger(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        WaterPurity.applyEffects(player, bowl(WaterQuality.fresh(0)));

        TestFixtures.check(helper, player.hasEffect(MobEffects.NAUSEA),
                "purity 0 has a 100 percent nausea chance in the default config");
        TestFixtures.check(helper, player.hasEffect(MobEffects.HUNGER),
                "the nausea roll also applies hunger");
        helper.succeed();
    }

    @GameTest
    public void purifiedWaterHasNoSideEffects(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        boolean hydrates = WaterPurity.applyEffects(player, bowl(WaterQuality.fresh(3)));

        TestFixtures.check(helper, hydrates, "purified water must grant hydration");
        TestFixtures.check(helper, !player.hasEffect(MobEffects.NAUSEA) && !player.hasEffect(MobEffects.POISON),
                "purity 3 has no nausea or poison chance in the default config");
        helper.succeed();
    }

    @GameTest
    public void drinkingRaisesThirst(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);
        ThirstManager.set(player, ThirstManager.get(player).withLevels(4, 0));

        ThirstManager.drinkItem(player, bowl(WaterQuality.fresh(3)));

        int thirst = ThirstManager.get(player).thirst();
        TestFixtures.check(helper, thirst > 4,
                "drinking a purified water bowl should raise thirst above 4, got " + thirst);
        helper.succeed();
    }

    /**
     * Milk and honey are the two drinks vanilla has that are not water, and both used to be worth
     * nothing. Their values live in the config, which merges them into files written before they
     * existed, so this asks the API rather than the defaults.
     */
    @GameTest
    public void milkAndHoneyQuenchThirst(GameTestHelper helper) {
        for (ItemStack drink : List.of(new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.HONEY_BOTTLE))) {
            ServerPlayer player = TestFixtures.mockPlayer(helper);
            ThirstManager.set(player, ThirstManager.get(player).withLevels(4, 0));

            ThirstManager.drinkItem(player, drink);

            int thirst = ThirstManager.get(player).thirst();
            TestFixtures.check(helper, thirst > 4,
                    drink.getItem() + " should raise thirst above 4, got " + thirst);
        }
        helper.succeed();
    }

    /**
     * Salt water has no grade, so there is nothing for boiling to raise. It must come back out of
     * the fire as salt water, without having picked up a grade on the way.
     */
    @GameTest
    public void boilingCannotDesalinate(GameTestHelper helper) {
        ItemStack salty = bowl(WaterQuality.SALT);

        WaterPurity.purify(salty, WaterPurity.MAX);

        TestFixtures.check(helper, WaterPurity.quality(salty) instanceof WaterQuality.Salt,
                "purifying salt water must leave it salt water, got " + WaterPurity.quality(salty));
        TestFixtures.check(helper, !salty.has(ThirstComponents.WATER_PURITY),
                "salt water must not end up carrying a grade");
        helper.succeed();
    }

    private static ItemStack bowl(WaterQuality quality) {
        return WaterPurity.setQuality(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
    }
}
