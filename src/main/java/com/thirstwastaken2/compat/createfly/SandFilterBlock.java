package com.thirstwastaken2.compat.createfly;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class SandFilterBlock extends Block implements EntityBlock, FluidInventoryProvider<SandFilterBlockEntity> {
    public SandFilterBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SandFilterBlockEntity(pos, state);
    }

    @Override
    public Class<SandFilterBlockEntity> getBlockEntityClass() {
        return SandFilterBlockEntity.class;
    }

    @Override
    public FluidInventory getFluidInventory(LevelAccessor level, BlockPos pos, BlockState state,
                                            SandFilterBlockEntity blockEntity, Direction side) {
        if (side == Direction.UP) return blockEntity.inputInventory();
        if (side == Direction.DOWN) return blockEntity.outputInventory();
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Create's SmartBlockEntity ticks its behaviours on both sides, so never skip the client.
        if (type != CreateFlyIntegration.sandFilterEntity()) return null;
        return (world, pos, currentState, blockEntity) -> ((SandFilterBlockEntity) blockEntity).tickFilter();
    }
}
