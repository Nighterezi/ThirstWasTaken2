package com.thirstwastaken2.createfly;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The quality of the water one pump or hose pulley draws, remembered for a few seconds.
 *
 * <p>Collecting water is where the mod samples it, and a pipe collects every tick it pulls. The
 * neighbourhood scan behind {@link WaterPurity#sampleAt} is kept off that tick path by reusing its
 * answer for {@link #RESAMPLE_TICKS}; a cauldron's stored quality is only a blockstate read, so it is
 * never reused, and a block with no water in it is not sampled at all.
 */
public final class SampledWater {
    private static final int RESAMPLE_TICKS = 100;

    private @Nullable WaterQuality quality;
    private @Nullable BlockPos pos;
    private long sampledAt;

    /** What water drawn from {@code pos} is, or {@code null} when there is no water there. */
    public @Nullable WaterQuality at(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        WaterQuality stored = WaterPurity.storedQuality(state);
        if (stored != null) return stored;
        if (!state.getFluidState().is(FluidTags.WATER)) return null;

        long now = level.getGameTime();
        if (quality == null || !pos.equals(this.pos) || now - sampledAt >= RESAMPLE_TICKS) {
            quality = WaterPurity.sampleAt(level, pos);
            this.pos = pos.immutable();
            sampledAt = now;
        }
        return quality;
    }
}
