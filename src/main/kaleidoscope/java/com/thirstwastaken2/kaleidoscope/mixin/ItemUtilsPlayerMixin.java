package com.thirstwastaken2.kaleidoscope.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.thirstwastaken2.kaleidoscope.ReturnedWater;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Refabricated's teapot hands the bucket to a player through {@code giveItemToPlayer} rather than
 * {@code getItemToLivingEntity}, from {@code FluidUtils.fillItem}. The official NeoForge build has no
 * such method, so this is a class of its own, which the plugin applies only where the method is there.
 * Stamped as {@link ItemUtilsMixin} does.
 */
@Mixin(value = ItemUtils.class, remap = false)
abstract class ItemUtilsPlayerMixin {
    // By name alone, both overloads, as in ItemUtilsMixin.
    @ModifyVariable(method = "giveItemToPlayer", at = @At("HEAD"), argsOnly = true)
    private static ItemStack thirst$stampReturnedWater(ItemStack stack) {
        return ReturnedWater.stamp(stack);
    }
}
