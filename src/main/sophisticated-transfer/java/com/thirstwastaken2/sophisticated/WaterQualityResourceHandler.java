package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.neoforge.WaterContainerFluids;
import com.thirstwastaken2.neoforge.WaterFluidResources;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A container's fluid handler with the water's quality carried across it, the transfer API's
 * counterpart of 1.21.1's {@code WaterQualityFluidHandler}.
 *
 * <p>The handlers underneath know nothing of quality: NeoForge's bucket handler hands out and takes
 * plain water, and Core's own bottle handler only recognises an unstamped water bottle. So the handler
 * is found on the container's {@link WaterPurity#unstamped unstamped} copy and works through an
 * {@link UnstampedItemAccess}; water leaving it is stamped with the grade the container held, water
 * entering it is passed down plain, and the access stamps the container swapped in.
 *
 * <p>The quality is read from the real container every time rather than kept here, so a transaction
 * rolled back leaves nothing stale behind.
 *
 * <p>The fluid side keeps the one-component rule of {@code WaterFluids}, so water of two grades never
 * merges into one tank.
 */
public final class WaterQualityResourceHandler implements ResourceHandler<FluidResource> {
    private final ResourceHandler<FluidResource> delegate;
    private final UnstampedItemAccess access;

    private WaterQualityResourceHandler(ResourceHandler<FluidResource> delegate, UnstampedItemAccess access) {
        this.delegate = delegate;
        this.access = access;
    }

    /**
     * {@code lookup}'s handler for {@code stack} held in {@code access}, wrapped. The mod's own waterskin
     * and bowls are the exception: their handler already carries the grade.
     */
    public static Optional<ResourceHandler<FluidResource>> wrap(ItemStack stack, ItemAccess access,
            BiFunction<ItemStack, ItemAccess, Optional<ResourceHandler<FluidResource>>> lookup) {
        if (WaterContainerFluids.handles(stack)) return lookup.apply(stack, access);
        UnstampedItemAccess view = new UnstampedItemAccess(access);
        ItemStack shown = WaterPurity.isWaterContainer(stack) ? WaterPurity.unstamped(stack) : stack;
        return lookup.apply(shown, view).map(handler -> new WaterQualityResourceHandler(handler, view));
    }

    /**
     * For a caller that looks the handler up on the access itself, from its item: the access to hand that
     * lookup instead of {@code access}, whose answer then goes through {@link #of}.
     */
    public static ItemAccess viewFor(ItemAccess access) {
        return WaterContainerFluids.handles(access.getResource().toStack()) ? access : new UnstampedItemAccess(access);
    }

    /** A handler found through {@link #viewFor}, wrapped; the mod's own containers pass as they are. */
    public static ResourceHandler<FluidResource> of(ResourceHandler<FluidResource> handler, ItemAccess view) {
        return view instanceof UnstampedItemAccess unstamped ? new WaterQualityResourceHandler(handler, unstamped) : handler;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return stamped(delegate.getResource(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return delegate.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return delegate.getCapacityAsLong(index, WaterFluidResources.unstamped(resource));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return delegate.isValid(index, WaterFluidResources.unstamped(resource));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (!WaterFluidResources.isWater(resource)) {
            access.stampWith(null);
            return delegate.insert(index, resource, amount, transaction);
        }
        WaterQuality incoming = WaterFluidResources.quality(resource);
        WaterQuality held = access.quality();
        // A container cannot hold two grades at once, and one with room left could otherwise be topped
        // up with better water and take its grade.
        if (held != null && !held.equals(incoming) && holdsWater()) return 0;
        access.stampWith(incoming);
        return delegate.insert(index, WaterFluidResources.unstamped(resource), amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        WaterQuality held = access.quality();
        if (held == null || !WaterFluidResources.isWater(resource)) {
            access.stampWith(null);
            return delegate.extract(index, resource, amount, transaction);
        }
        // Only water of exactly this grade matches, the same comparison the asking tank makes.
        FluidResource plain = WaterFluidResources.unstamped(resource);
        if (!WaterFluidResources.stamp(plain, held).equals(resource)) return 0;
        // Whatever is left in the container keeps its grade.
        access.stampWith(held);
        return delegate.extract(index, plain, amount, transaction);
    }

    private FluidResource stamped(FluidResource resource) {
        WaterQuality held = WaterFluidResources.isWater(resource) ? access.quality() : null;
        return held == null ? resource : WaterFluidResources.stamp(resource, held);
    }

    private boolean holdsWater() {
        for (int index = 0; index < delegate.size(); index++) {
            if (WaterFluidResources.isWater(delegate.getResource(index))) return true;
        }
        return false;
    }
}
