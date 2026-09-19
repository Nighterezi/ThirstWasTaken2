package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.neoforge.WaterFluids;
import com.thirstwastaken2.purity.WaterQuality;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Water in the world as a fluid handler that knows its grade: whatever is drained comes out stamped
 * with the quality sampled where it lies. The handler underneath, a {@code BucketPickup} or a block's
 * own capability, hands out plain water and is asked for plain water.
 */
public final class StampedWaterSource implements IFluidHandler {
    private final IFluidHandler delegate;
    private final WaterQuality quality;

    public StampedWaterSource(IFluidHandler delegate, WaterQuality quality) {
        this.delegate = delegate;
        this.quality = quality;
    }

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return stamped(delegate.getFluidInTank(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return delegate.isFluidValid(tank, WaterFluids.unstamped(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return delegate.fill(WaterFluids.unstamped(resource), action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!WaterFluids.isWater(resource)) return delegate.drain(resource, action);
        if (!FluidStack.isSameFluidSameComponents(WaterFluids.stamp(resource.copy(), quality), resource)) {
            return FluidStack.EMPTY;
        }
        return stamped(delegate.drain(WaterFluids.unstamped(resource), action));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return stamped(delegate.drain(maxDrain, action));
    }

    private FluidStack stamped(FluidStack fluid) {
        return WaterFluids.isWater(fluid) ? WaterFluids.stamp(fluid.copy(), quality) : fluid;
    }
}
