package com.thirstwastaken2.kaleidoscope.mixin;

import com.thirstwastaken2.kaleidoscope.ReturnedWater;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Where Refabricated's teapot hands a player the bucket it fills: {@code FluidUtils.fillItem} goes
 * through the Fabric Transfer API, which swaps the empty bucket for a full one straight in the player's
 * inventory slot rather than through {@code ItemUtils}, so {@link ItemUtilsMixin} never sees it. The
 * slot is written through {@code setItem}. Stamped only while a remove call is running, as
 * {@link ReturnedWater} says; any other stack set in the meantime is not water, and a stamp on water
 * already stamped changes nothing.
 */
@Mixin(Inventory.class)
abstract class InventoryMixin {
    @ModifyVariable(method = "setItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack thirst$stampReturnedWater(ItemStack stack) {
        return ReturnedWater.stamp(stack);
    }
}
