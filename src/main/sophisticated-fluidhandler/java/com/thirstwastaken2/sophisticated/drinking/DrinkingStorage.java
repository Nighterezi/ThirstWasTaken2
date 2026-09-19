package com.thirstwastaken2.sophisticated.drinking;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageFluidHandler;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;

/**
 * The storage the Drinking upgrade drinks from, its slots and its Tank upgrades, through the
 * {@code IFluidHandler} generation of Sophisticated Core.
 */
final class DrinkingStorage {
    private final ITrackedContentsItemHandler inventory;
    private final IStorageFluidHandler tanks;

    DrinkingStorage(IStorageWrapper storageWrapper) {
        inventory = storageWrapper.getInventoryForUpgradeProcessing();
        tanks = storageWrapper.getFluidHandler().orElse(null);
    }

    int slots() {
        return inventory.getSlots();
    }

    ItemStack get(int slot) {
        return inventory.getStackInSlot(slot);
    }

    void set(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    /** What did not fit. */
    ItemStack insert(ItemStack stack) {
        return inventory.insertItem(stack, false);
    }

    int tanks() {
        return tanks == null ? 0 : tanks.getTanks();
    }

    FluidStack tank(int tank) {
        return tanks.getFluidInTank(tank);
    }

    /** Takes all of {@code serving} out of the tanks, or nothing. */
    boolean drain(FluidStack serving) {
        if (tanks == null || tanks.drain(serving, FluidAction.SIMULATE, true).getAmount() < serving.getAmount()) return false;
        tanks.drain(serving, FluidAction.EXECUTE, true);
        return true;
    }
}
