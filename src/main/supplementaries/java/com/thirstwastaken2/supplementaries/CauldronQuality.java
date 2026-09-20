package com.thirstwastaken2.supplementaries;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a faucet and a vanilla water cauldron say to each other about the water between them.
 *
 * <p>A cauldron is the one carrier whose quality is a blockstate value rather than a component, so
 * none of the soft fluid hooks reach it. Supplementaries' own behaviour offers plain water when it
 * drains one and places a default water cauldron when it fills one, which turns a faucet into a way to
 * launder dirty water clean in both directions.
 */
public final class CauldronQuality {
    /** The flags {@code WaterInteractions} writes a cauldron's quality with. */
    private static final int BLOCK_UPDATE_FLAGS = 3;

    private CauldronQuality() { }

    /** Hands the faucet what the cauldron actually holds. An unstamped cauldron is left to read as the default. */
    public static void offer(FluidOffer offer, BlockState cauldron) {
        if (offer == null) return;
        WaterQuality stored = WaterPurity.storedQuality(cauldron);
        if (stored != null) SoftFluidQuality.stamp(offer.fluid(), stored);
    }

    /**
     * Stores what was poured in. The cauldron keeps the worse of what it held and what arrived, exactly
     * as pouring a container in by hand does, because its blockstate has room for one value.
     *
     * @param before the state the cauldron was in, which is where what it held is read from
     * @param moved what the behaviour reported: null for a block it does not handle, zero for nothing
     */
    public static void filled(Level level, BlockPos pos, BlockState before, FluidOffer offer, Integer moved) {
        if (moved == null || moved == 0 || !SoftFluidQuality.isWater(offer.fluid())) return;

        WaterQuality held = WaterPurity.storedQuality(before);
        WaterQuality poured = SoftFluidQuality.quality(offer.fluid());
        WaterQuality quality = held == null ? poured : WaterQuality.worse(held, poured);

        BlockState after = level.getBlockState(pos);
        if (!after.hasProperty(WaterPurity.BLOCK_PURITY)) return;
        int value = WaterPurity.storedValue(quality);
        if (after.getValue(WaterPurity.BLOCK_PURITY) == value) return;
        level.setBlock(pos, after.setValue(WaterPurity.BLOCK_PURITY, value), BLOCK_UPDATE_FLAGS);
    }
}
