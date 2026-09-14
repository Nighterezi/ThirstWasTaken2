package com.thirstwastaken2.createfly;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Water goes in at the top and comes out of the bottom one grade cleaner, as in the original mod.
 * Pipes find the two tanks through {@link FluidInventoryProvider}; the sides connect to nothing.
 */
public final class SandFilterBlock extends Block
        implements IWrenchable, IBE<SandFilterBlockEntity>, FluidInventoryProvider<SandFilterBlockEntity> {

    public SandFilterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable FluidInventory getFluidInventory(LevelAccessor level, BlockPos pos, BlockState state,
                                                      SandFilterBlockEntity blockEntity, @Nullable Direction side) {
        if (side == Direction.UP) return blockEntity.input().getCapability();
        if (side == Direction.DOWN) return blockEntity.output().getCapability();
        // No side is Create asking on behalf of a player's hand, which is only ever allowed to take
        // filtered water out.
        if (side == null) return blockEntity.output().getCapability();
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // The same outline as the model, which is the Spout's silhouette.
        return AllShapes.SPOUT;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        // How much filtered water is waiting. Create's own helper only reads a single-tank block.
        SandFilterBlockEntity filter = getBlockEntity(level, pos);
        if (filter == null) return 0;
        TankSegment output = filter.output().getPrimaryHandler();
        return ComparatorUtil.fractionToRedstoneLevel(
                (double) output.getFluid().getAmount() / output.getMaxAmountPerStack());
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public Class<SandFilterBlockEntity> getBlockEntityClass() {
        return SandFilterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SandFilterBlockEntity> getBlockEntityType() {
        return SandFilter.blockEntity();
    }
}
