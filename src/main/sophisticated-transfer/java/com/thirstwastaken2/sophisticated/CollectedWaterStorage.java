package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.neoforge.WaterFluidResources;
import com.thirstwastaken2.purity.WaterQuality;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The backpack's tanks while the Pump upgrade collects one block of world water: the water goes in
 * stamped with the quality sampled where it lay. The block, whether a {@code BucketPickup} or a fluid
 * capability, only ever hands out plain water, so stamping the side that receives it covers both.
 */
public final class CollectedWaterStorage implements ResourceHandler<FluidResource> {
    private final ResourceHandler<FluidResource> delegate;
    private final WaterQuality quality;

    public CollectedWaterStorage(ResourceHandler<FluidResource> delegate, WaterQuality quality) {
        this.delegate = delegate;
        this.quality = quality;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return delegate.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return delegate.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return delegate.getCapacityAsLong(index, WaterFluidResources.stamp(resource, quality));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return delegate.isValid(index, WaterFluidResources.stamp(resource, quality));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return delegate.insert(index, WaterFluidResources.stamp(resource, quality), amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(index, resource, amount, transaction);
    }

    /** Whether any tank could take more water: an empty one, or one of water with room left. */
    public static boolean hasRoomForWater(ResourceHandler<FluidResource> storage) {
        for (int index = 0; index < storage.size(); index++) {
            FluidResource held = storage.getResource(index);
            if (held.isEmpty() || WaterFluidResources.isWater(held)
                    && storage.getAmountAsLong(index) < storage.getCapacityAsLong(index, held)) {
                return true;
            }
        }
        return false;
    }
}
