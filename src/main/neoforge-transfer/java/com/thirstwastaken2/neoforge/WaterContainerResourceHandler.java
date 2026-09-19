package com.thirstwastaken2.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A waterskin or terracotta bowl as a fluid {@code ResourceHandler}, NeoForge's transfer API from
 * 1.21.11. The rules are in {@link WaterContainerFluids}; the base class does the transactions and
 * swaps the item through its {@link ItemAccess}, one item of a stack at a time.
 */
public final class WaterContainerResourceHandler extends ItemAccessResourceHandler<FluidResource> {
    public WaterContainerResourceHandler(ItemAccess access) {
        super(access, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource item, int index) {
        FluidStack held = WaterContainerFluids.contents(item.toStack());
        return held.isEmpty() ? FluidResource.EMPTY : FluidResource.of(held);
    }

    @Override
    protected int getAmountFrom(ItemResource item, int index) {
        return WaterContainerFluids.contents(item.toStack()).getAmount();
    }

    /** {@link ItemResource#EMPTY} refuses the change, which is how part of a serving is turned away. */
    @Override
    protected ItemResource update(ItemResource item, int index, FluidResource resource, int amount) {
        ItemStack changed = WaterContainerFluids.holding(item.toStack(),
                resource.isEmpty() ? null : WaterFluids.quality(resource.toStack(1)), amount);
        return changed == null ? ItemResource.EMPTY : ItemResource.of(changed);
    }

    /** Rounded down to whole servings first, as 1.21.1's handler does; the base class would refuse the lot. */
    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return super.insert(index, resource, WaterContainerFluids.wholeServings(amount), transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return super.extract(index, resource, WaterContainerFluids.wholeServings(amount), transaction);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return WaterContainerFluids.accepts(resource.getFluid());
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return WaterContainerFluids.capacity(itemAccess.getResource().toStack());
    }
}
