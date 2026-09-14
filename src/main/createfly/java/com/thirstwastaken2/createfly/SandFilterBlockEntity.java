package com.thirstwastaken2.createfly;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Two one-bucket tanks: pipes fill the input from above and drain the output from below, and every
 * tick up to {@link #FILTERED_PER_TICK} moves from one to the other, a grade cleaner.
 *
 * <p>Pipes are held to that direction by the tanks' own insertion and extraction flags. The transfer
 * itself goes through the {@link TankSegment}s underneath, which those flags do not apply to - going
 * through {@link SmartFluidTankBehaviour#getCapability()} instead is what kept the first version of
 * this block from ever moving any water.
 */
public final class SandFilterBlockEntity extends SmartBlockEntity {
    /** Create Fly counts fluid in droplets, 81 to the millibucket, so this is one bucket. */
    public static final int CAPACITY = BucketFluidInventory.CAPACITY;
    /** 10 mB a tick, the original mod's default: a bucket every five seconds. */
    private static final int FILTERED_PER_TICK = 10 * 81;

    // Assigned from addBehaviours, which runs inside the super constructor. An initializer here would
    // run after it and wipe them.
    private SmartFluidTankBehaviour input;
    private SmartFluidTankBehaviour output;
    /** The input stack {@link #filtered} was built from. Neither is saved; both rebuild on the next tick. */
    private @Nullable FluidStack filteredFrom;
    private @Nullable FluidStack filtered;
    /** The output stack already known to hold the same water as {@link #filtered}. */
    private @Nullable FluidStack matchedOutput;

    public SandFilterBlockEntity(BlockPos pos, BlockState state) {
        super(SandFilter.blockEntity(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        input = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, CAPACITY, false,
                WaterOnlyHandler::new).forbidExtraction();
        output = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, CAPACITY, false)
                .forbidInsertion();
        behaviours.add(input);
        behaviours.add(output);
    }

    public SmartFluidTankBehaviour input() {
        return input;
    }

    public SmartFluidTankBehaviour output() {
        return output;
    }

    /**
     * Moves water across without building a stack per tick. The filtered form of the input is built once
     * per input stack, and the output is compared against it once per stack that lands there: a tank
     * merging more of the same water keeps its stack object and only changes the amount, so neither
     * cached answer can go stale without the identity changing too.
     */
    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;

        TankSegment from = input.getPrimaryHandler();
        FluidStack dirty = from.getFluid();
        if (dirty.isEmpty()) return;

        TankSegment to = output.getPrimaryHandler();
        FluidStack waiting = to.getFluid();
        int space = to.getMaxAmountPerStack() - (waiting.isEmpty() ? 0 : waiting.getAmount());
        if (space <= 0) return;

        FluidStack filtered = filtered(dirty);
        // Water of a different grade than what is already waiting cannot share the output tank, so the
        // filter holds until a pipe drains it, the same as any other full Create tank.
        if (!waiting.isEmpty() && waiting != matchedOutput) {
            if (!FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(waiting, filtered)) return;
            matchedOutput = waiting;
        }

        // A partial last batch is filtered too, so a tank never keeps a few droplets it cannot pass on.
        int moved = Math.min(Math.min(FILTERED_PER_TICK, dirty.getAmount()), space);
        if (waiting.isEmpty()) {
            matchedOutput = filtered.copyWithAmount(moved);
            to.setFluid(matchedOutput);
        } else {
            waiting.setAmount(waiting.getAmount() + moved);
        }
        if (moved == dirty.getAmount()) from.setFluid(FluidStack.EMPTY);
        else dirty.setAmount(dirty.getAmount() - moved);
        from.markDirty();
        to.markDirty();
    }

    /** The input water as it leaves the filter, rebuilt only when a different stack sits in the input. */
    private FluidStack filtered(FluidStack dirty) {
        if (dirty != filteredFrom) {
            filtered = WaterFluids.filter(dirty.copyWithAmount(1));
            filteredFrom = dirty;
            matchedOutput = null;
        }
        return filtered;
    }

    /** The input only takes water: anything else would sit in it forever, since nothing drains it. */
    private static final class WaterOnlyHandler extends SmartFluidTankBehaviour.InternalFluidHandler {
        WaterOnlyHandler(SmartFluidTankBehaviour behaviour, Boolean enforceVariety, Optional<Integer> max) {
            super(behaviour, enforceVariety, max);
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
            return WaterFluids.isWater(stack) && super.canInsert(slot, stack, dir);
        }
    }
}
