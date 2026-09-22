package com.thirstwastaken2.kaleidoscope.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.thirstwastaken2.kaleidoscope.ReturnedWater;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Where the stockpot and the teapot hand a filled bucket back, both having built it from nothing. Stamped
 * only while one of their remove calls is running, see {@link ReturnedWater}; every other item passing
 * through is left alone. Stamped through {@code WaterPurity.setQuality}, so fresh water gets
 * {@code water_salty: false} and sea water its sprite, the two rules a bare component copy misses.
 */
@Mixin(value = ItemUtils.class, remap = false)
abstract class ItemUtilsMixin {
    // By name alone: both overloads take the stack, and the short one calls the long one, where a second
    // stamp changes nothing.
    @ModifyVariable(method = "getItemToLivingEntity", at = @At("HEAD"), argsOnly = true)
    private static ItemStack thirst$stampReturnedWater(ItemStack stack) {
        return ReturnedWater.stamp(stack);
    }
}
