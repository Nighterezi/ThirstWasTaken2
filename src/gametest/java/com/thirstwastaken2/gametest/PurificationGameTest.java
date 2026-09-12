package com.thirstwastaken2.gametest;

import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * Which water a furnace will accept.
 *
 * <p>The recipes live entirely in datapack files and match on components, so nothing on the Java
 * side fails to compile when a container stops carrying what they look for. These tests are that
 * missing compiler: they ask the real recipe manager about real stacks.
 */
public final class PurificationGameTest {
    /** Enough rolls that the water pool, at half weight, is certain to have produced bottles. */
    private static final int ROLLS = 40;

    /**
     * Looted water used to carry a grade and nothing else, which no recipe could match: every
     * cooking recipe also requires the container to say it is fresh.
     */
    @GameTest
    public void lootedWaterBottlesCanBeBoiled(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);

        int bottles = 0;
        int cookable = 0;
        for (int roll = 0; roll < ROLLS; roll++) {
            for (ItemStack stack : table.getRandomItems(params, roll)) {
                if (!WaterPurity.isWaterContainer(stack)) continue;
                bottles++;
                // Purified water is already clean and deliberately has no recipe of its own.
                boolean needsBoiling = WaterPurity.get(stack) < WaterPurity.MAX;
                if (!needsBoiling || hasSmeltingRecipe(helper, stack)) cookable++;
            }
        }

        TestFixtures.check(helper, bottles > 0,
                "the loot pool should have produced water bottles in " + ROLLS + " rolls");
        TestFixtures.check(helper, cookable == bottles,
                "every looted water bottle should be boilable, got " + cookable + " of " + bottles);
        helper.succeed();
    }

    /** Salt water carries no grade, so no cooking recipe can match it. That is the whole guard. */
    @GameTest
    public void saltWaterIsRejectedByTheFurnace(GameTestHelper helper) {
        ItemStack salty = WaterPurity.setQuality(
                new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), WaterQuality.SALT);
        ItemStack dirty = WaterPurity.setQuality(
                new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), WaterQuality.fresh(0));

        TestFixtures.check(helper, !hasSmeltingRecipe(helper, salty),
                "salt water must not be smeltable, or boiling it would quietly desalinate it");
        TestFixtures.check(helper, hasSmeltingRecipe(helper, dirty),
                "dirty fresh water must still be smeltable, otherwise the test above proves nothing");
        helper.succeed();
    }

    private static boolean hasSmeltingRecipe(GameTestHelper helper, ItemStack stack) {
        return helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), helper.getLevel())
                .isPresent();
    }
}
