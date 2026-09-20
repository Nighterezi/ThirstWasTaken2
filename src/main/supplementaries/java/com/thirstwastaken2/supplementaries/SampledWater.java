package com.thirstwastaken2.supplementaries;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The quality of the water a faucet draws out of the world, remembered for a few seconds.
 *
 * <p>Collecting water is where the mod samples it, and a faucet collects on a tick: one with nowhere
 * to pour asks again every tick, since its cooldown only starts once something moves. The fixed 5x3x5
 * scan behind {@link WaterPurity#sampleAt} must never sit on a path like that, so its answer is reused
 * for {@link #RESAMPLE_TICKS}, the same span Create's pumps and Sophisticated's use.
 *
 * <p>Unlike theirs this is not one sample but a few, because one instance of the faucet behaviour
 * serves every faucet in the world and a single remembered position would be thrown away and taken
 * again on every tick the moment a second faucet stood over different water. It holds positions and
 * grades, nothing that keeps a level or a chunk alive, and it belongs to a behaviour that is built
 * again on every data pack reload.
 *
 * <p>Server thread only, which is the only thread a faucet ticks on.
 */
public final class SampledWater {
    private static final int RESAMPLE_TICKS = 100;
    /** Enough for every faucet a build is likely to stand over at once; the least used one goes first. */
    private static final int MAX_POSITIONS = 32;

    private record Where(ResourceKey<Level> dimension, BlockPos pos) { }

    private record Sample(WaterQuality quality, long at) { }

    private final Map<Where, Sample> samples = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Where, Sample> eldest) {
            return size() > MAX_POSITIONS;
        }
    };

    /**
     * Gives an offer of world water the grade that water actually has. Anything else, an offer of lava
     * or none at all, is left alone.
     */
    public void stamp(FluidOffer offer, Level level, BlockPos pos) {
        if (offer == null || level.isClientSide() || !SoftFluidQuality.isWater(offer.fluid())) return;
        SoftFluidQuality.stamp(offer.fluid(), at(level, pos));
    }

    private WaterQuality at(Level level, BlockPos pos) {
        Where where = new Where(level.dimension(), pos.immutable());
        long now = level.getGameTime();
        Sample sample = samples.get(where);
        // A game time that has gone backwards is a different world on the same behaviour: take it again.
        if (sample == null || now - sample.at() >= RESAMPLE_TICKS || now < sample.at()) {
            sample = new Sample(WaterPurity.sampleAt(level, pos), now);
            samples.put(where, sample);
        }
        return sample.quality();
    }
}
