package com.thirstwastaken2.sophisticated.drinking;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageFluidHandler;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemResourceHandler;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

/**
 * The storage the Drinking upgrade drinks from, its slots and its Tank upgrades, through the transfer
 * API generation of Sophisticated Core.
 */
final class DrinkingStorage {
    private final ITrackedContentsItemResourceHandler inventory;
    private final IStorageFluidHandler tanks;

    DrinkingStorage(IStorageWrapper storageWrapper) {
        inventory = storageWrapper.getInventoryForUpgradeProcessing();
        tanks = storageWrapper.getFluidHandler().orElse(null);
    }

    int slots() {
        return inventory.size();
    }

    ItemStack get(int slot) {
        return inventory.getStackInSlot(slot);
    }

    void set(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    /** What did not fit. */
    ItemStack insert(ItemStack stack) {
        int inserted = InventoryHelper.insert(inventory, ItemResource.of(stack), stack.getCount());
        return stack.copyWithCount(stack.getCount() - inserted);
    }

    int tanks() {
        return tanks == null ? 0 : tanks.size();
    }

    FluidStack tank(int tank) {
        return tanks.getResource(tank).toStack(tanks.getAmountAsInt(tank));
    }

    /**
     * Takes all of {@code serving} out of the tanks, or nothing. Past the tanks' input and output limit,
     * like 1.21.1's drain, since a player drinking is not a pipe.
     */
    boolean drain(FluidStack serving) {
        if (tanks == null) return false;
        FluidResource resource = FluidResource.of(serving);
        try (Transaction transaction = Transaction.openRoot()) {
            int drained = 0;
            for (int tank = 0; tank < tanks.size() && drained < serving.getAmount(); tank++) {
                drained += tanks.extract(tank, resource, serving.getAmount() - drained, transaction, true);
            }
            if (drained < serving.getAmount()) return false;
            transaction.commit();
            return true;
        }
    }
}
