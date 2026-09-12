package com.thirstwastaken2.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterInteractions;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Cauldrons remember the quality of the water poured into them.
 *
 * <p>The two extra blockstate properties come from {@code LayeredCauldronBlockMixin}, and the
 * transfer itself is deferred to the end of the tick because vanilla resolves the interaction after
 * the callback returns.
 */
public final class CauldronGameTest {
    private static final BlockPos CAULDRON = new BlockPos(2, 2, 2);
    /** Stored grades are offset by one so that zero can mean "nothing stored yet". */
    private static final int UNSET = WaterPurity.BLOCK_UNSET;

    /**
     * A cauldron nobody has poured into holds plain water, and the quality has to be stored in one
     * property for it to say so. Vanilla gives a freshly placed block the first value of every
     * property it carries, and for a boolean that is {@code true}, so a separate salinity flag would
     * make every new cauldron read as sea water.
     */
    @GameTest
    public void aFreshWaterCauldronHoldsNothingInParticular(GameTestHelper helper) {
        BlockState state = Blocks.WATER_CAULDRON.defaultBlockState();

        TestFixtures.check(helper, state.hasProperty(WaterPurity.BLOCK_PURITY),
                "the water cauldron should have a quality property");
        TestFixtures.check(helper, state.getValue(WaterPurity.BLOCK_PURITY) == UNSET,
                "a fresh water cauldron should store no quality yet, got "
                        + state.getValue(WaterPurity.BLOCK_PURITY));
        TestFixtures.check(helper, WaterPurity.storedQuality(state) == null,
                "a fresh water cauldron must not read as salt water, got "
                        + WaterPurity.storedQuality(state));
        helper.succeed();
    }

    @GameTest
    public void powderSnowCauldronsStayPlain(GameTestHelper helper) {
        BlockState state = Blocks.POWDER_SNOW_CAULDRON.defaultBlockState();

        TestFixtures.check(helper, !state.hasProperty(WaterPurity.BLOCK_PURITY),
                "powder snow never holds water, so its cauldron should not multiply its states with "
                        + "the quality property");
        helper.succeed();
    }

    @GameTest
    public void powderSnowCauldronsSavedWithQualityStillLoad(GameTestHelper helper) {
        // Worlds saved before the properties were limited to water cauldrons still list them on
        // powder snow. Decoding has to ignore them instead of falling back to the default state.
        JsonObject properties = new JsonObject();
        properties.addProperty("level", "2");
        properties.addProperty("purity", "4");
        properties.addProperty("salty", "true");
        JsonObject saved = new JsonObject();
        saved.addProperty("Name", "minecraft:powder_snow_cauldron");
        saved.add("Properties", properties);

        BlockState loaded = BlockState.CODEC.parse(JsonOps.INSTANCE, saved).result().orElse(null);

        TestFixtures.check(helper, loaded != null && loaded.is(Blocks.POWDER_SNOW_CAULDRON)
                        && loaded.getValue(LayeredCauldronBlock.LEVEL) == 2,
                "an old powder snow cauldron should load with its fill level intact, got " + loaded);
        helper.succeed();
    }

    @GameTest
    public void pouringStoresTheGradeInTheCauldron(GameTestHelper helper) {
        WaterQuality.Fresh poured = new WaterQuality.Fresh(0);
        BlockPos pos = pour(helper, poured);

        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, after.getValue(WaterPurity.BLOCK_PURITY) == poured.purity() + 1,
                "the cauldron should store grade " + poured.purity() + ", got "
                        + (after.getValue(WaterPurity.BLOCK_PURITY) - 1));
        TestFixtures.check(helper, after.getValue(WaterPurity.BLOCK_PURITY) != WaterPurity.BLOCK_SALT,
                "fresh water should not make the cauldron salty");
        helper.succeed();
    }

    /** Salt water has no grade, so the cauldron stores the salinity alone and leaves grade unset. */
    @GameTest
    public void pouringSaltWaterStoresNoGrade(GameTestHelper helper) {
        BlockPos pos = pour(helper, WaterQuality.SALT);

        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, after.getValue(WaterPurity.BLOCK_PURITY) == WaterPurity.BLOCK_SALT,
                "the cauldron should remember that the water was salty, got "
                        + after.getValue(WaterPurity.BLOCK_PURITY));
        TestFixtures.check(helper, WaterPurity.sampleAt(helper.getLevel(), pos) instanceof WaterQuality.Salt,
                "drawing from a salty cauldron should give salt water back");
        helper.succeed();
    }

    @GameTest
    public void pouringKeepsTheWorseOfTheTwoQualities(GameTestHelper helper) {
        // Fill the cauldron with clean water first, then pour dirty water in on top of it.
        BlockPos pos = pour(helper, WaterQuality.fresh(WaterPurity.MAX));
        int clean = helper.getLevel().getBlockState(pos).getValue(WaterPurity.BLOCK_PURITY);

        pourInto(helper, pos, WaterQuality.fresh(0));

        int mixed = helper.getLevel().getBlockState(pos).getValue(WaterPurity.BLOCK_PURITY);
        TestFixtures.check(helper, mixed < clean,
                "pouring dirty water into clean water should not leave it clean, "
                        + (clean - 1) + " became " + (mixed - 1));
        helper.succeed();
    }

    @GameTest
    public void sampledCauldronWaterMatchesWhatWasPoured(GameTestHelper helper) {
        WaterQuality poured = WaterQuality.fresh(0);
        BlockPos pos = pour(helper, poured);

        WaterQuality sampled = WaterPurity.sampleAt(helper.getLevel(), pos);

        TestFixtures.check(helper, sampled.equals(poured),
                "drawing from the cauldron should report " + poured + ", got " + sampled);
        helper.succeed();
    }

    /** Places a water cauldron and pours one container of the given quality into it. */
    private static BlockPos pour(GameTestHelper helper, WaterQuality quality) {
        helper.setBlock(CAULDRON, Blocks.WATER_CAULDRON);
        BlockPos pos = helper.absolutePos(CAULDRON);
        pourInto(helper, pos, quality);
        return pos;
    }

    private static void pourInto(GameTestHelper helper, BlockPos pos, WaterQuality quality) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack container = WaterPurity.setQuality(
                new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
        player.setItemInHand(InteractionHand.MAIN_HAND, container);

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        WaterInteractions.transferCauldronPurity(player, helper.getLevel(), InteractionHand.MAIN_HAND, hit);
        // The transfer is queued until vanilla has settled the block, so run the queue by hand.
        WaterInteractions.tick(helper.getLevel().getServer());
    }
}
