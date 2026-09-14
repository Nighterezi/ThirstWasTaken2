package com.thirstwastaken2.client.compat;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Adds the grade of the water under the crosshair to Jade's overlay, for water in the world, a
 * waterlogged block and a water cauldron alike.
 *
 * <p>Jade is a compile-only dependency and resolves this class through the {@code jade} entrypoint,
 * which it reads on the dedicated server too. Only {@link #registerClient} touches client classes, so
 * the class itself loads safely there. The annotation is how Jade finds plugins on other loaders.
 */
@WailaPlugin
public final class JadeIntegration implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Registered on Block rather than LiquidBlock, because a waterlogged block holds water that a
        // bottle can be filled from just the same.
        registration.registerBlockComponent(WaterPurityProvider.INSTANCE, Block.class);
    }

    /**
     * Grades the water on the client. Everything {@link WaterPurity#sampleAt} reads - blockstates, the
     * biome and its tags - is synced, so the grade shown is the one the server stamps on a container
     * filled there.
     */
    private enum WaterPurityProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final Identifier UID = ThirstWasTaken2.id("water_purity");
        /**
         * How long a sample stays valid while the crosshair rests on one block. Jade rebuilds its
         * overlay every client tick; a block placed nearby shows up within half a second.
         */
        private static final int RESAMPLE_TICKS = 10;

        // Client thread only, which is the only thread Jade collects tooltips on.
        private ResourceKey<Level> lastDimension;
        private long lastPos;
        private BlockState lastState;
        private long lastSampleTime;
        private WaterQuality lastQuality;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            // Only the water cauldron carries the stored-quality property.
            if (!state.getFluidState().is(FluidTags.WATER) && !state.hasProperty(WaterPurity.BLOCK_PURITY)) return;

            WaterQuality quality = sample(accessor.getLevel(), accessor.getPosition(), state);
            tooltip.add(switch (quality) {
                case WaterQuality.Salt ignored -> WaterPurity.saltTooltip();
                case WaterQuality.Fresh fresh -> WaterPurity.tooltip(fresh.purity());
            });
        }

        private WaterQuality sample(Level level, BlockPos pos, BlockState state) {
            long time = level.getGameTime();
            if (lastQuality != null && level.dimension() == lastDimension && pos.asLong() == lastPos
                    && state == lastState && time >= lastSampleTime && time - lastSampleTime < RESAMPLE_TICKS) {
                return lastQuality;
            }
            lastDimension = level.dimension();
            lastPos = pos.asLong();
            lastState = state;
            lastSampleTime = time;
            lastQuality = WaterPurity.sampleAt(level, pos);
            return lastQuality;
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
