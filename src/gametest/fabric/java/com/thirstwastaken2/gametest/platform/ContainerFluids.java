package com.thirstwastaken2.gametest.platform;

import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;

/**
 * The waterskin and the terracotta bowls through a loader's item fluid capability. The mod only gives
 * them one on NeoForge, so on Fabric there is nothing to call.
 */
public final class ContainerFluids {
    private ContainerFluids() { }

    public static boolean available() {
        return false;
    }

    public static String unavailable() {
        return "the waterskin and the bowls have no Fabric Transfer API storage yet; the capability is "
                + "NeoForge only and is checked on the NeoForge nodes";
    }

    public static FluidMove fillWater(ItemStack container, WaterQuality quality, int amount) {
        throw new UnsupportedOperationException(unavailable());
    }

    public static FluidMove fillLava(ItemStack container, int amount) {
        throw new UnsupportedOperationException(unavailable());
    }

    public static FluidMove drain(ItemStack container, int amount) {
        throw new UnsupportedOperationException(unavailable());
    }
}
