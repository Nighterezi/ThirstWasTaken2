package com.thirstwastaken2.gametest;

import com.thirstwastaken2.block.HangingPotBlock;
import com.thirstwastaken2.block.ThirstBlocks;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The copper hanging pot: filling and drawing through the real use path, the frame following the
 * campfire, and boiling. The iron pot is the same block class, so it is only checked for taking part in
 * the use path and the boil at all.
 *
 * <p>Boiling is driven by calling the block's tick directly rather than waiting for it, because a
 * default boil outlasts a test's time limit. What the scheduler is asked to do is checked separately.
 */
public final class HangingPotGameTest {
    private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
    private static final BlockPos POT = new BlockPos(2, 2, 2);

    @GameTest
    public void aBucketFillsThreeServingsAndKeepsItsGrade(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), 0, null);
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        hold(player, WaterPurity.set(new ItemStack(Items.WATER_BUCKET), 1));

        use(helper, player, pos);

        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, after.getValue(HangingPotBlock.LEVEL) == HangingPotBlock.BUCKET,
                "a bucket should add three servings, the pot holds " + after.getValue(HangingPotBlock.LEVEL));
        TestFixtures.check(helper, WaterQuality.fresh(1).equals(HangingPotBlock.quality(after)),
                "the pot should hold the bucket's murky water, got " + HangingPotBlock.quality(after));
        TestFixtures.check(helper, player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET),
                "the player should be left holding an empty bucket, got " + player.getItemInHand(InteractionHand.MAIN_HAND));
        helper.succeed();
    }

    @GameTest
    public void aFullPotTakesNoMoreWater(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), HangingPotBlock.CAPACITY - 2, WaterQuality.fresh(3));
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        hold(player, new ItemStack(Items.WATER_BUCKET));

        use(helper, player, pos);

        TestFixtures.check(helper, helper.getLevel().getBlockState(pos).getValue(HangingPotBlock.LEVEL)
                        == HangingPotBlock.CAPACITY - 2,
                "a bucket should not fit in a pot with room for two servings");
        TestFixtures.check(helper, player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WATER_BUCKET),
                "the player should keep the full bucket");
        helper.succeed();
    }

    @GameTest
    public void pouringKeepsTheWorseGradeAndRestartsTheBoil(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), 2, WaterQuality.fresh(3));
        helper.setBlock(POT, helper.getLevel().getBlockState(pos).setValue(HangingPotBlock.BOIL, 3));
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        hold(player, WaterPurity.set(TestFixtures.waterBottle(), 0));

        use(helper, player, pos);

        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, WaterQuality.fresh(0).equals(HangingPotBlock.quality(after)),
                "pouring dirty water into pure water should leave it dirty, got " + HangingPotBlock.quality(after));
        TestFixtures.check(helper, after.getValue(HangingPotBlock.BOIL) == 0,
                "new water should start the boil over, got stage " + after.getValue(HangingPotBlock.BOIL));
        TestFixtures.check(helper, player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GLASS_BOTTLE),
                "the player should be left holding a glass bottle");
        helper.succeed();
    }

    @GameTest
    public void aBottleDrawnFromThePotCarriesItsWater(GameTestHelper helper) {
        WaterQuality stored = WaterQuality.fresh(3);
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), 1, stored);
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        hold(player, new ItemStack(Items.GLASS_BOTTLE));

        use(helper, player, pos);

        ItemStack drawn = player.getItemInHand(InteractionHand.MAIN_HAND);
        TestFixtures.check(helper, drawn.is(Items.POTION) && WaterPurity.isWaterContainer(drawn),
                "drawing should hand out a water bottle, got " + drawn);
        TestFixtures.check(helper, stored.equals(WaterPurity.quality(drawn)),
                "the bottle should carry the pot's " + stored + ", got " + WaterPurity.quality(drawn));
        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, after.getValue(HangingPotBlock.LEVEL) == 0
                        && after.getValue(WaterPurity.BLOCK_PURITY) == WaterPurity.BLOCK_UNSET,
                "an emptied pot should hold nothing and remember no grade, got " + after);
        helper.succeed();
    }

    @GameTest
    public void aWaterskinDrawsOneServingAndPoursWhenSneaking(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), 2, WaterQuality.fresh(2));
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);
        hold(player, skin);

        use(helper, player, pos);
        TestFixtures.check(helper, WaterskinItem.servings(skin) == 1
                        && helper.getLevel().getBlockState(pos).getValue(HangingPotBlock.LEVEL) == 1,
                "a waterskin should draw one serving, it holds " + WaterskinItem.servings(skin));

        player.setShiftKeyDown(true);
        player.setPose(Pose.CROUCHING);
        use(helper, player, pos);
        TestFixtures.check(helper, WaterskinItem.servings(skin) == 0
                        && helper.getLevel().getBlockState(pos).getValue(HangingPotBlock.LEVEL) == 2,
                "a sneaking player should pour the waterskin back into the pot, it holds "
                        + WaterskinItem.servings(skin));
        helper.succeed();
    }

    @GameTest
    public void theFrameFollowsTheCampfire(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.CAMPFIRE.defaultBlockState(), 0, null);
        ServerLevel level = helper.getLevel();
        BlockState placed = ThirstBlocks.COPPER_HANGING_POT.getStateForPlacement(
                new net.minecraft.world.item.context.BlockPlaceContext(TestFixtures.mockPlayer(helper),
                        InteractionHand.MAIN_HAND, new ItemStack(ThirstItems.COPPER_HANGING_POT),
                        aimAt(helper.absolutePos(FLOOR))));
        TestFixtures.check(helper, placed != null && placed.getValue(HangingPotBlock.HANGING),
                "a pot placed on a campfire should hang, got " + placed);
        TestFixtures.check(helper, level.getBlockState(pos).canSurvive(level, pos),
                "a pot should stand on a campfire");

        helper.setBlock(FLOOR, Blocks.AIR);
        TestFixtures.check(helper, level.getBlockState(pos).isAir(),
                "a pot whose campfire is gone should break, got " + level.getBlockState(pos));
        helper.succeed();
    }

    @GameTest
    public void aPotOverALitCampfireBoilsPure(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.CAMPFIRE.defaultBlockState(), 3, WaterQuality.fresh(0));
        ServerLevel level = helper.getLevel();
        TestFixtures.check(helper, level.getBlockTicks().hasScheduledTick(pos, ThirstBlocks.COPPER_HANGING_POT),
                "dirty water over a lit campfire should schedule a boil");

        for (int stage = 0; stage < HangingPotBlock.BOIL_STAGES; stage++) {
            level.getBlockState(pos).tick(level, pos, level.getRandom());
        }

        BlockState after = level.getBlockState(pos);
        TestFixtures.check(helper, WaterQuality.fresh(WaterPurity.MAX).equals(HangingPotBlock.quality(after)),
                "the water should have boiled pure, got " + HangingPotBlock.quality(after));
        TestFixtures.check(helper, after.getValue(HangingPotBlock.LEVEL) == 3,
                "boiling should not cost any water, the pot holds " + after.getValue(HangingPotBlock.LEVEL));
        helper.succeed();
    }

    @GameTest
    public void anUnlitCampfireOrSaltWaterDoesNotBoil(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockState unlit = Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false);
        BlockPos pos = pot(helper, unlit, 3, WaterQuality.fresh(0));
        TestFixtures.check(helper, !level.getBlockTicks().hasScheduledTick(pos, ThirstBlocks.COPPER_HANGING_POT),
                "nothing should be scheduled over an unlit campfire");
        for (int stage = 0; stage < HangingPotBlock.BOIL_STAGES; stage++) {
            level.getBlockState(pos).tick(level, pos, level.getRandom());
        }
        TestFixtures.check(helper, WaterQuality.fresh(0).equals(HangingPotBlock.quality(level.getBlockState(pos))),
                "an unlit campfire should not boil anything");

        helper.setBlock(FLOOR, Blocks.CAMPFIRE.defaultBlockState());
        TestFixtures.check(helper, level.getBlockTicks().hasScheduledTick(pos, ThirstBlocks.COPPER_HANGING_POT),
                "lighting the campfire should pick the boil up");

        helper.setBlock(POT, HangingPotBlock.withWater(level.getBlockState(pos), 3, WaterQuality.SALT));
        level.getBlockState(pos).tick(level, pos, level.getRandom());
        TestFixtures.check(helper, level.getBlockState(pos).getValue(HangingPotBlock.BOIL) == 0
                        && WaterQuality.SALT.equals(HangingPotBlock.quality(level.getBlockState(pos))),
                "salt water should not boil into anything");
        helper.succeed();
    }

    /** Rain tops the pot up with rainwater, and like a cauldron only on a fraction of its chances. */
    @GameTest
    public void rainFillsThePotWithRainwater(GameTestHelper helper) {
        BlockPos pos = pot(helper, Blocks.STONE.defaultBlockState(), 0, null);
        ServerLevel level = helper.getLevel();
        for (int attempt = 0; attempt < 500 && level.getBlockState(pos).getValue(HangingPotBlock.LEVEL) == 0; attempt++) {
            BlockState before = level.getBlockState(pos);
            before.getBlock().handlePrecipitation(before, level, pos, Biome.Precipitation.RAIN);
        }

        BlockState after = level.getBlockState(pos);
        WaterQuality expected = WaterQuality.fresh(ThirstConfig.get().rainwaterPurity);
        TestFixtures.check(helper, after.getValue(HangingPotBlock.LEVEL) == 1,
                "rain should have added one serving in 500 tries, the pot holds " + after.getValue(HangingPotBlock.LEVEL));
        TestFixtures.check(helper, expected.equals(HangingPotBlock.quality(after)),
                "rainwater should be graded " + expected + ", got " + HangingPotBlock.quality(after));
        helper.succeed();
    }

    @GameTest
    public void aPotInTheNetherTakesNoWater(GameTestHelper helper) {
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        TestFixtures.check(helper, nether != null, "the test server should have a Nether");
        // Well inside the Nether's height and away from anything a test places, and cleared again after.
        BlockPos pos = new BlockPos(0, 100, 0);
        nether.getChunk(pos);
        nether.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        nether.setBlockAndUpdate(pos, ThirstBlocks.COPPER_HANGING_POT.defaultBlockState());
        try {
            ServerPlayer player = TestFixtures.survivalPlayer(helper);
            hold(player, new ItemStack(Items.WATER_BUCKET));

            player.gameMode.useItemOn(player, nether, player.getItemInHand(InteractionHand.MAIN_HAND),
                    InteractionHand.MAIN_HAND, aimAt(pos));

            TestFixtures.check(helper, nether.getBlockState(pos).getValue(HangingPotBlock.LEVEL) == 0,
                    "water poured into a pot in the Nether should boil away, the pot holds "
                            + nether.getBlockState(pos).getValue(HangingPotBlock.LEVEL));
            TestFixtures.check(helper, player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WATER_BUCKET),
                    "the player should keep the water bucket, got " + player.getItemInHand(InteractionHand.MAIN_HAND));
        } finally {
            nether.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            nether.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest
    public void theIronPotFillsLikeTheCopperOne(GameTestHelper helper) {
        BlockPos pos = pot(helper, ThirstBlocks.IRON_HANGING_POT, Blocks.STONE.defaultBlockState(), 0, null);
        ServerPlayer player = TestFixtures.survivalPlayer(helper);
        hold(player, WaterPurity.set(new ItemStack(Items.WATER_BUCKET), 1));

        use(helper, player, pos);

        BlockState after = helper.getLevel().getBlockState(pos);
        TestFixtures.check(helper, after.is(ThirstBlocks.IRON_HANGING_POT)
                        && after.getValue(HangingPotBlock.LEVEL) == HangingPotBlock.BUCKET,
                "a bucket should add three servings to the iron pot, got " + after);
        TestFixtures.check(helper, WaterQuality.fresh(1).equals(HangingPotBlock.quality(after)),
                "the iron pot should hold the bucket's murky water, got " + HangingPotBlock.quality(after));
        helper.succeed();
    }

    @GameTest
    public void theIronPotBoilsOverACampfire(GameTestHelper helper) {
        BlockPos pos = pot(helper, ThirstBlocks.IRON_HANGING_POT, Blocks.CAMPFIRE.defaultBlockState(), 3,
                WaterQuality.fresh(0));
        ServerLevel level = helper.getLevel();
        TestFixtures.check(helper, level.getBlockTicks().hasScheduledTick(pos, ThirstBlocks.IRON_HANGING_POT),
                "an iron pot of dirty water over a lit campfire should schedule its boil");

        for (int stage = 0; stage < HangingPotBlock.BOIL_STAGES; stage++) {
            level.getBlockState(pos).tick(level, pos, level.getRandom());
        }

        BlockState after = level.getBlockState(pos);
        TestFixtures.check(helper, WaterQuality.fresh(WaterPurity.MAX).equals(HangingPotBlock.quality(after)),
                "the iron pot should boil its water pure, got " + HangingPotBlock.quality(after));
        helper.succeed();
    }

    /** Places {@code floor}, and a pot on it holding {@code level} servings of {@code quality}. */
    private static BlockPos pot(GameTestHelper helper, BlockState floor, int level, WaterQuality quality) {
        return pot(helper, ThirstBlocks.COPPER_HANGING_POT, floor, level, quality);
    }

    private static BlockPos pot(GameTestHelper helper, HangingPotBlock block, BlockState floor, int level,
                                WaterQuality quality) {
        helper.setBlock(FLOOR, floor);
        BlockState pot = block.defaultBlockState()
                .setValue(HangingPotBlock.HANGING, floor.is(Blocks.CAMPFIRE));
        helper.setBlock(POT, HangingPotBlock.withWater(pot, level, quality));
        return helper.absolutePos(POT);
    }

    private static void hold(ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    /** Uses the held item on the pot through the game mode, which is where both loaders fire their hooks. */
    private static void use(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, helper.getLevel(), player.getItemInHand(InteractionHand.MAIN_HAND),
                InteractionHand.MAIN_HAND, aimAt(pos));
    }

    private static BlockHitResult aimAt(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    }
}
