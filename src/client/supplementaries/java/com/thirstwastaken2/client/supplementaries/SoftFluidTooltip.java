package com.thirstwastaken2.client.supplementaries;

import com.thirstwastaken2.client.compat.JadeIntegration;
import com.thirstwastaken2.purity.WaterQuality;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The grade of the water a Supplementaries jar or goblet holds, for Jade's overlay.
 *
 * <p>No sampling and no cache: the tank is on the block entity the client already has, so the answer
 * is a component read, and what it says is what a bottle drawn from that block would carry.
 *
 * <p>Written as a block the mod's own Jade plugin asks rather than as a plugin of its own, so that
 * Jade shows one entry with one name in its settings instead of two that say the same thing in nine
 * languages.
 */
final class SoftFluidTooltip {
    private SoftFluidTooltip() { }

    static void register() {
        JadeIntegration.addContainer(SoftFluidTooltip::quality);
    }

    /** @return what the block holds, or null for a block that is not one of these or is empty. */
    private static WaterQuality quality(BlockEntity blockEntity) {
        if (!(blockEntity instanceof ISoftFluidTankProvider provider)) return null;
        SoftFluidTank tank = provider.getSoftFluidTank();
        if (tank == null || !SoftFluidQuality.isWater(tank.getFluid())) return null;
        return SoftFluidQuality.quality(tank.getFluid());
    }
}
