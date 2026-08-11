package com.thirstwastaken.compat.createfly;

import com.thirstwastaken.purity.ThirstComponents;
import com.thirstwastaken.purity.WaterPurity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Purifies water by one step as it is pumped from the input tank into the output tank. */
public final class SandFilterBlockEntity extends SmartBlockEntity {
    private static final int CAPACITY = 1000;
    private static final int TRANSFER_PER_TICK = 10;

    private SmartFluidTankBehaviour input;
    private SmartFluidTankBehaviour output;

    public SandFilterBlockEntity(BlockPos pos, BlockState state) {
        super(CreateFlyIntegration.sandFilterEntity(), pos, state);
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

    FluidInventory inputInventory() {
        return input == null ? null : input.getCapability();
    }

    FluidInventory outputInventory() {
        return output == null ? null : output.getCapability();
    }

    public void tickFilter() {
        super.tick();
        if (level == null || level.isClientSide() || input == null || output == null) return;

        FluidStack source = input.getPrimaryHandler().getFluid();
        if (source.isEmpty() || source.getAmount() < TRANSFER_PER_TICK || !source.isIn(FluidTags.WATER)) return;

        Integer purity = source.get(ThirstComponents.WATER_PURITY);
        // Already-clean water still flows through; it simply cannot get any cleaner.
        int purified = Math.min(WaterPurity.MAX, (purity == null ? WaterPurity.MIN : purity) + 1);

        FluidStack filtered = source.copyWithAmount(TRANSFER_PER_TICK);
        filtered.set(ThirstComponents.WATER_PURITY, purified);

        int inserted = output.getCapability().insert(filtered);
        if (inserted > 0) input.getCapability().extract(source.copyWithAmount(inserted));
    }
}
