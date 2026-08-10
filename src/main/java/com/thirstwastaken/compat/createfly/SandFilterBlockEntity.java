package com.thirstwastaken.compat.createfly;

import com.thirstwastaken.purity.ThirstComponents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class SandFilterBlockEntity extends SmartBlockEntity {
    private static final int CAPACITY = 1000;
    private static final int TRANSFER_PER_TICK = 10;
    private SmartFluidTankBehaviour input;
    private SmartFluidTankBehaviour output;

    public SandFilterBlockEntity(BlockPos pos, BlockState state) {
        super(CreateFlyIntegration.SAND_FILTER_ENTITY, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        input = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, CAPACITY, false)
                .allowInsertion().forbidExtraction();
        output = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, CAPACITY, false)
                .forbidInsertion().allowExtraction();
        behaviours.add(input);
        behaviours.add(output);
    }

    public void tickFilter() {
        super.tick();
        if (level == null || level.isClientSide() || input == null || output == null) return;
        FluidStack source = input.getPrimaryHandler().getFluid();
        if (source.isEmpty() || !source.isIn(FluidTags.WATER) || source.getAmount() < TRANSFER_PER_TICK) return;
        FluidStack filtered = source.copyWithAmount(TRANSFER_PER_TICK);
        Integer purity = filtered.get(ThirstComponents.WATER_PURITY);
        filtered.set(ThirstComponents.WATER_PURITY, Math.min(3, (purity == null ? 0 : purity) + 1));
        int inserted = output.getCapability().insert(filtered);
        if (inserted > 0) input.getCapability().extract(source.copyWithAmount(inserted));
    }
}
