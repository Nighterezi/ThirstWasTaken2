package com.thirstwastaken2.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.IntConsumer;

/**
 * A block that has to react when the block it stands on changes. Vanilla calls that {@code updateShape},
 * and 1.21.2 reordered its parameters and moved tick scheduling onto an argument of its own, so the
 * override lives here once and hands the mod's blocks one signature for every version.
 *
 * <p>A class rather than a method for the same reason as {@link DrinkItem}: what differs is an override.
 */
public abstract class SupportedBlock extends Block {
    protected SupportedBlock(Properties properties) {
        super(properties);
    }

    /**
     * The block below {@code pos} is now {@code below}. Returns the state this block should take, which
     * may be the same one.
     *
     * @param scheduleTick schedules this block's {@code tick} that many ticks from now
     */
    protected abstract BlockState supportChanged(BlockState state, LevelReader level, BlockPos pos, BlockState below,
                                                 IntConsumer scheduleTick);

    private BlockState neighborChanged(BlockState state, LevelReader level, BlockPos pos, Direction direction,
                                       BlockState neighbor, IntConsumer scheduleTick) {
        if (direction != Direction.DOWN) return state;
        // updateOrDestroy breaks a block that turns into air here, drops included.
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return supportChanged(state, level, pos, neighbor, scheduleTick);
    }

    //? if >=1.21.2 {
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level,
                                     net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor,
                                     net.minecraft.util.RandomSource random) {
        return neighborChanged(state, level, pos, direction, neighbor,
                delay -> ticks.scheduleTick(pos, this, delay));
    }
    //?} else {
    /*@Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                     net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        return neighborChanged(state, level, pos, direction, neighbor,
                delay -> level.scheduleTick(pos, this, delay));
    }
    *///?}
}
