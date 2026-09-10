package com.thirstwastaken2.gametest;

import com.thirstwastaken2.purity.ThirstComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/** Scaffolding shared by the gametests: readable assertions, a water source, and a player aimed at it. */
final class TestFixtures {
    /** Relative position of the water source every water test uses. */
    static final BlockPos WATER = new BlockPos(2, 2, 2);

    private TestFixtures() { }

    /**
     * Asserts with a plain message. {@link GameTestHelper#assertTrue} only takes a
     * {@link Component} on every supported version, so this keeps the call sites readable.
     */
    static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    /**
     * Places a stone floor with one water source on top of it and returns the source's absolute
     * position.
     *
     * <p>Fluid spreading is scheduled rather than immediate, and these tests run inside a single
     * tick, so the source cannot flow away before it is sampled.
     */
    static BlockPos water(GameTestHelper helper) {
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, WATER.getY() - 1, z), Blocks.STONE);
            }
        }
        helper.setBlock(WATER, Blocks.WATER);
        return helper.absolutePos(WATER);
    }

    /**
     * A survival player standing above the water and looking straight down at it.
     *
     * <p>Survival matters: {@code ItemUtils.createFilledResult} hands the filled container back
     * differently once the player has infinite materials, which would make the filling tests measure
     * the wrong thing. Gravity is off because nothing ticks during a test body.
     */
    static ServerPlayer playerAboveWater(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setNoGravity(true);
        BlockPos water = helper.absolutePos(WATER);
        player.snapTo(water.getX() + 0.5, water.getY() + 2.0, water.getZ() + 0.5, 0.0F, 90.0F);
        return player;
    }

    /**
     * A rideable entity type. The constants moved from {@code EntityType} to {@code EntityTypes} in
     * 26.2, and this is the only place the tests need one.
     */
    static EntityType<?> mountType() {
        //? if >=26.2 {
        return net.minecraft.world.entity.EntityTypes.PIG;
        //?} else {
        /*return net.minecraft.world.entity.EntityType.PIG;
        *///?}
    }

    /**
     * The first stack the player is carrying that has a sampled water quality on it. The filled
     * container does not always come back in the interaction hand, so the whole inventory is
     * searched.
     */
    static ItemStack findSampledWater(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.has(ThirstComponents.WATER_CONTAMINATION)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
