package com.thirstwastaken2.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentType;

/** Fabric Transfer API calls whose names changed between the Fabric API versions the nodes use. */
public final class FabricTransfer {
    private FabricTransfer() { }

    /**
     * One component a fluid variant carries, or {@code null}. Fabric API 8 (Minecraft 26.1 and later)
     * renamed the full component map from {@code getComponentMap} to {@code getComponents}, a name that
     * returned the patch before.
     */
    public static <T> T component(FluidVariant variant, DataComponentType<T> type) {
        //? if >=26.1 {
        return variant.getComponents().get(type);
        //?} else {
        /*return variant.getComponentMap().get(type);
        *///?}
    }
}
