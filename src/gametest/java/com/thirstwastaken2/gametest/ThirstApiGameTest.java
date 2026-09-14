package com.thirstwastaken2.gametest;

import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;

/**
 * What an item restores, and the config that decides it: the drink and food tables, the blacklist,
 * the {@code c:drinks} tag, keyword matching, and the clamping that keeps a hand-edited config file
 * from breaking the game.
 *
 * <p>Expected values are read from the live config rather than written out, because the test server
 * keeps its config file between runs and a value is only wrong if the API disagrees with the config.
 */
public final class ThirstApiGameTest {
    private static final TagKey<Item> DRINKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "drinks"));

    @GameTest
    public void configuredDrinksAndFoodsRestoreWhatTheConfigSays(GameTestHelper helper) {
        ThirstConfig config = ThirstConfig.get();
        restores(helper, Items.POTION, config.drinks.get("minecraft:potion"));
        restores(helper, Items.MILK_BUCKET, config.drinks.get("minecraft:milk_bucket"));
        restores(helper, Items.APPLE, config.foods.get("minecraft:apple"));
        restores(helper, ThirstItems.TERRACOTTA_WATER_BOWL, config.drinks.get("thirstwastaken2:terracotta_water_bowl"));
        TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.STONE)) == null,
                "stone should restore nothing");
        helper.succeed();
    }

    @GameTest
    public void aWaterskinOnlyRestoresWhileItHoldsWater(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);
        TestFixtures.check(helper, ThirstApi.thirstValues(skin) == null, "an empty waterskin should restore nothing");

        WaterskinItem.addWater(skin, WaterQuality.fresh(2), 1);
        TestFixtures.check(helper, Arrays.equals(ThirstApi.thirstValues(skin),
                        ThirstConfig.get().drinks.get("thirstwastaken2:waterskin")),
                "a filled waterskin should restore its configured value, got "
                        + Arrays.toString(ThirstApi.thirstValues(skin)));
        helper.succeed();
    }

    /** Also proves the per-item cache is dropped on commit, in both directions. */
    @GameTest
    public void blacklistedItemsRestoreNothing(GameTestHelper helper) {
        TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.APPLE)) != null,
                "an apple should restore something before it is blacklisted");

        TestFixtures.withConfig(config -> config.itemBlacklist.add("minecraft:apple"), () ->
                TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.APPLE)) == null,
                        "a blacklisted apple should restore nothing, got "
                                + Arrays.toString(ThirstApi.thirstValues(new ItemStack(Items.APPLE)))));

        TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.APPLE)) != null,
                "the apple should restore something again once the blacklist is restored");
        helper.succeed();
    }

    /**
     * The gametest mod tags a nautilus shell {@code c:drinks} in its own data, standing in for a drink
     * from a mod the config has never heard of. Vanilla has no such item: everything it tags is listed.
     */
    @GameTest
    public void itemsTaggedAsDrinksRestoreTheTagValue(GameTestHelper helper) {
        TestFixtures.check(helper, new ItemStack(Items.NAUTILUS_SHELL).is(DRINKS),
                "the gametest data pack should tag the nautilus shell c:drinks, or this test proves nothing");
        restores(helper, Items.NAUTILUS_SHELL, ThirstConfig.get().drinkTagValue);
        // Fabric API tags the ominous bottle c:drinks too, as a magic drink.
        TestFixtures.check(helper, new ItemStack(Items.OMINOUS_BOTTLE).is(DRINKS),
                "the ominous bottle should be tagged c:drinks, or its exclusion below proves nothing");
        TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.OMINOUS_BOTTLE)) == null,
                "a magic drink should restore nothing, got "
                        + Arrays.toString(ThirstApi.thirstValues(new ItemStack(Items.OMINOUS_BOTTLE))));
        helper.succeed();
    }

    @GameTest
    public void theConfigAndBlacklistWinOverTheDrinkTag(GameTestHelper helper) {
        // Honey is tagged c:drinks/honey and listed at a different value, so the listed one must win.
        ThirstConfig config = ThirstConfig.get();
        TestFixtures.check(helper, !Arrays.equals(config.drinks.get("minecraft:honey_bottle"), config.drinkTagValue),
                "honey should be listed at a value other than the tag value, or this test proves nothing");
        restores(helper, Items.HONEY_BOTTLE, config.drinks.get("minecraft:honey_bottle"));

        TestFixtures.withConfig(edited -> edited.itemBlacklist.add("minecraft:nautilus_shell"), () ->
                TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.NAUTILUS_SHELL)) == null,
                        "a blacklisted item should restore nothing whatever its tags say"));
        helper.succeed();
    }

    @GameTest
    public void drinkTagMatchingCanBeTurnedOff(GameTestHelper helper) {
        TestFixtures.withConfig(config -> config.enableDrinkTagMatching = false, () ->
                TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.NAUTILUS_SHELL)) == null,
                        "with drink tag matching off, a tagged item nobody listed should restore nothing, got "
                                + Arrays.toString(ThirstApi.thirstValues(new ItemStack(Items.NAUTILUS_SHELL)))));
        TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.NAUTILUS_SHELL)) != null,
                "the tagged item should restore something again once matching is back on");
        helper.succeed();
    }

    @GameTest
    public void keywordMatchingOnlyAppliesWhenTurnedOn(GameTestHelper helper) {
        TestFixtures.withConfig(config -> config.enableKeywordMatching = false, () -> {
            TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.SUSPICIOUS_STEW)) == null,
                    "with keyword matching off, a stew nobody listed should restore nothing");
        });

        TestFixtures.withConfig(config -> config.enableKeywordMatching = true, () -> {
            ThirstConfig config = ThirstConfig.get();
            restores(helper, Items.SUSPICIOUS_STEW, config.keywordSoupValue);
            restores(helper, Items.CHORUS_FRUIT, config.keywordFruitValue);
            // "melon" matches the fruit keywords, and "seed" is on the keyword blacklist, which wins.
            TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.MELON_SEEDS)) == null,
                    "the keyword blacklist should win over a fruit keyword, got "
                            + Arrays.toString(ThirstApi.thirstValues(new ItemStack(Items.MELON_SEEDS))));
        });
        helper.succeed();
    }

    @GameTest
    public void aHandEditedConfigIsClampedBackIntoRange(GameTestHelper helper) {
        TestFixtures.withConfig(config -> {
            config.defaultPurity = 99;
            config.handDrinkingThirst = -3;
            config.thirstDepletionModifier = 50.0;
            config.fireResistanceDehydrationPercent = 400;
            config.thirstBarYOffset = -9000;
            config.nauseaChance = new int[] {1};
            config.poisonChance = new int[] {150, -5, 0, 0};
            config.drinks.remove("minecraft:milk_bucket");
            config.drinks.remove("farmersdelight:milk_bottle");
            config.foods.remove("farmersdelight:bone_broth");
            config.drinkTagValue = new int[] {3};
        }, () -> {
            ThirstConfig config = ThirstConfig.get();
            TestFixtures.check(helper, config.defaultPurity == 3, "default_purity should clamp to 3, got " + config.defaultPurity);
            TestFixtures.check(helper, config.handDrinkingThirst == 0, "hand_drinking_thirst should clamp to 0");
            TestFixtures.check(helper, config.thirstDepletionModifier == 10.0, "thirst_depletion_modifier should clamp to 10");
            TestFixtures.check(helper, config.fireResistanceDehydrationPercent == 100, "the percentage should clamp to 100");
            TestFixtures.check(helper, config.thirstBarYOffset == -200, "the HUD offset should clamp to -200");
            TestFixtures.check(helper, config.nauseaChance.length == 4,
                    "a nausea table of the wrong length should be reset, got " + Arrays.toString(config.nauseaChance));
            TestFixtures.check(helper, config.poisonChance[0] == 100 && config.poisonChance[1] == 0,
                    "poison chances should clamp to 0-100, got " + Arrays.toString(config.poisonChance));
            TestFixtures.check(helper, config.drinks.containsKey("minecraft:milk_bucket"),
                    "a config file written before milk counted should have it merged back in");
            TestFixtures.check(helper, config.drinks.containsKey("farmersdelight:milk_bottle")
                            && config.foods.containsKey("farmersdelight:bone_broth"),
                    "a config file written before the added Farmer's Delight entries should have them merged back in");
            TestFixtures.check(helper, config.drinkTagValue.length == 2,
                    "a drink tag value of the wrong length should be reset, got " + Arrays.toString(config.drinkTagValue));
        });
        helper.succeed();
    }

    @GameTest
    public void aBrokenKeywordPatternIsIgnoredRatherThanFatal(GameTestHelper helper) {
        TestFixtures.withConfig(config -> {
            config.enableKeywordMatching = true;
            config.fruitKeywords = "(";
        }, () -> {
            TestFixtures.check(helper, ThirstConfig.get().fruitKeywordPattern() == null,
                    "an invalid pattern should be dropped, not compiled");
            TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.CHORUS_FRUIT)) == null,
                    "with the fruit pattern dropped, chorus fruit should match nothing");
            TestFixtures.check(helper, ThirstApi.thirstValues(new ItemStack(Items.SUSPICIOUS_STEW)) != null,
                    "the other keyword patterns should keep working");
        });
        helper.succeed();
    }

    private static void restores(GameTestHelper helper, Item item, int[] expected) {
        int[] actual = ThirstApi.thirstValues(new ItemStack(item));
        TestFixtures.check(helper, expected != null && Arrays.equals(actual, expected),
                item + " should restore " + Arrays.toString(expected) + ", got " + Arrays.toString(actual));
    }
}
