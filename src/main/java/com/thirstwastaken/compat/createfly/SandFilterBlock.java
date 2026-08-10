package com.thirstwastaken.compat.createfly;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class SandFilterBlock extends Block implements EntityBlock {
    public SandFilterBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SandFilterBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Create's SmartBlockEntity ticks its behaviours on both sides, so never skip the client.
        if (type != CreateFlyIntegration.sandFilterEntity()) return null;
        return (world, pos, currentState, blockEntity) -> ((SandFilterBlockEntity) blockEntity).tickFilter();
    }
}
