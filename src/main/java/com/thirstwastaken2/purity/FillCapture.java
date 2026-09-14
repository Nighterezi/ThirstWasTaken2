package com.thirstwastaken2.purity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The two-step purity stamp shared by the bottle and bucket mixins: sample the water vanilla's own
 * raycast found, then stamp the container vanilla hands back. Reusing that hit rather than casting a
 * second ray is most of what filling a bottle costs the mod.
 *
 * <p>Item instances are singletons, so the sample cannot be parked on the item — integrated-client
 * prediction and the server call the same singleton from different threads. A capture is opened and
 * consumed inside one {@code use} call, never across calls; both entry points clear first, so a
 * branch that never reaches its stamp cannot leak a stale sample into the next interaction.
 */
public final class FillCapture {
    private static final ThreadLocal<WaterQuality> CAPTURED = new ThreadLocal<>();

    private FillCapture() { }

    /** Forgets any earlier capture. Called as {@code use} starts, before vanilla casts its ray. */
    public static void clear() {
        CAPTURED.remove();
    }

    /**
     * Samples the water source vanilla's ray hit, if any, and hands the hit back unchanged. The server
     * result is authoritative and synchronizes the filled stack, so client prediction is skipped rather
     * than repeating the neighbourhood scan.
     */
    public static BlockHitResult capture(Level level, BlockHitResult hit) {
        if (level.isClientSide() || hit.getType() != HitResult.Type.BLOCK) return hit;
        BlockPos pos = hit.getBlockPos();
        if (level.getFluidState(pos).is(FluidTags.WATER)) CAPTURED.set(WaterPurity.sampleAt(level, pos));
        return hit;
    }

    /** Stamps the captured quality onto the filled container and closes the capture. */
    public static ItemStack stamp(ItemStack filled) {
        WaterQuality quality = CAPTURED.get();
        CAPTURED.remove();
        return quality == null ? filled : WaterPurity.setQuality(filled, quality);
    }
}
