package com.thirstwastaken2.purity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The two-step purity stamp shared by the bottle and bucket mixins: sample the water the player is
 * aiming at while the vanilla method starts, then stamp the container vanilla hands back.
 *
 * <p>Item instances are singletons, so the sample cannot be parked on the item — integrated-client
 * prediction and the server call the same singleton from different threads. A capture is opened and
 * consumed inside one {@code use} call, never across calls; both entry points clear first, so a
 * branch that never reaches its stamp cannot leak a stale sample into the next interaction.
 */
public final class FillCapture {
    private static final ThreadLocal<WaterQuality> CAPTURED = new ThreadLocal<>();

    private FillCapture() { }

    /**
     * Samples the water source under the player's crosshair, if any. The server result is
     * authoritative and synchronizes the filled stack, so client prediction is skipped rather than
     * repeating the neighbourhood scan.
     */
    public static void capture(Level level, Player player) {
        CAPTURED.remove();
        if (level.isClientSide()) return;
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(
                new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        if (level.getFluidState(pos).is(FluidTags.WATER)) CAPTURED.set(WaterPurity.sampleAt(level, pos));
    }

    /** Stamps the captured quality onto the filled container and closes the capture. */
    public static ItemStack stamp(ItemStack filled) {
        WaterQuality quality = CAPTURED.get();
        CAPTURED.remove();
        return quality == null ? filled : WaterPurity.setQuality(filled, quality);
    }
}
