package com.thirstwastaken2.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.thirstwastaken2.coldsweat.WaterskinWater;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The empty skin a filled one leaves, after a crafting recipe, a pour, a last sip or a dispenser, comes
 * back without a grade. Every one of those goes through {@code getCraftingRemainingItem}.
 */
@Mixin(FilledWaterskinItem.class)
abstract class FilledWaterskinItemMixin {
    @Inject(method = "getCraftingRemainingItem", at = @At("RETURN"))
    private void thirst$stripGrade(ItemStack filled, CallbackInfoReturnable<ItemStack> cir) {
        // It hands back the stack it was given when that is not a filled skin; that one keeps its grade.
        if (cir.getReturnValue() != filled) WaterskinWater.strip(cir.getReturnValue());
    }
}
