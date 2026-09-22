package com.thirstwastaken2.kaleidoscope;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

/**
 * The grade of water on its way back out of a stockpot or a teapot. Both hand the filled bucket over
 * through {@code ItemUtils.getItemToLivingEntity}, the stockpot directly and the teapot through
 * {@code FluidUtils.fillItem}, and both build that bucket from nothing, so it is stamped there, while a
 * remove call is running and only then.
 *
 * <p>Per thread: the blocks run their remove calls on the client too, and in a single player game the
 * client and the server thread run them at the same time. Set only for the length of {@link #during},
 * and cleared however the call ends, so a stamp never reaches an unrelated item.
 */
public final class ReturnedWater {
    private static final ThreadLocal<WaterQuality> CURRENT = new ThreadLocal<>();

    private ReturnedWater() { }

    /** Runs {@code removal} with water of {@code quality} on its way out; {@code null} stamps nothing. */
    public static boolean during(WaterQuality quality, BooleanSupplier removal) {
        WaterQuality outer = CURRENT.get();
        CURRENT.set(quality);
        try {
            return removal.getAsBoolean();
        } finally {
            if (outer == null) CURRENT.remove();
            else CURRENT.set(outer);
        }
    }

    /** Stamps {@code stack} if it is water leaving a block right now, and hands it back. */
    public static ItemStack stamp(ItemStack stack) {
        WaterQuality quality = CURRENT.get();
        if (quality != null && WaterPurity.isWaterContainer(stack)) WaterPurity.setQuality(stack, quality);
        return stack;
    }
}
