package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A container's {@link ItemAccess} as a handler that knows nothing of quality needs to see it: the item
 * shows unstamped, and whatever the handler swaps in is stamped with the grade of the water being moved.
 *
 * <p>NeoForge's bucket handler and Core's bottle handler both build the container they swap in from
 * scratch, so without this a graded bucket poured out and refilled would come back plain. Every write
 * goes straight through to the access underneath, inside the caller's transaction.
 */
final class UnstampedItemAccess implements ItemAccess {
    private final ItemAccess delegate;
    /** What a water container swapped in next is stamped with, or {@code null} to leave it plain. */
    private WaterQuality stampWith;

    UnstampedItemAccess(ItemAccess delegate) {
        this.delegate = delegate;
    }

    /** The water the real container holds, or {@code null} while it holds none of the mod's water. */
    WaterQuality quality() {
        ItemStack stack = delegate.getResource().toStack();
        return WaterPurity.isWaterContainer(stack) ? WaterPurity.quality(stack) : null;
    }

    /** Set by the handler before each transfer, to the grade that ends up in the container. */
    void stampWith(WaterQuality quality) {
        stampWith = quality;
    }

    @Override
    public ItemResource getResource() {
        ItemResource real = delegate.getResource();
        ItemStack stack = real.toStack();
        return WaterPurity.isWaterContainer(stack) ? ItemResource.of(WaterPurity.unstamped(stack)) : real;
    }

    @Override
    public int getAmount() {
        return delegate.getAmount();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack stack = resource.toStack();
        if (stampWith != null && WaterPurity.isWaterContainer(stack)) {
            resource = ItemResource.of(WaterPurity.setQuality(stack, stampWith));
        }
        return delegate.insert(resource, amount, transaction);
    }

    /** The handler asks for the item it was shown, which is the real, stamped one underneath. */
    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(resource.equals(getResource()) ? delegate.getResource() : resource, amount, transaction);
    }
}
