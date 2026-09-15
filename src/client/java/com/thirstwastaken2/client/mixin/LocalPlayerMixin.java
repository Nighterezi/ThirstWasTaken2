package com.thirstwastaken2.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The thirst sprint gate on 1.21.1, where the food check a sprint needs is private to LocalPlayer.
 * Empty on later versions, where PlayerMixin hooks the same check on Player. Loaded by both loaders.
 */
@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    //? if <=1.21.1 {
    /*@com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "hasEnoughFoodToStartSprinting",
            at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private boolean thirst$preventSprintingWhenThirsty(boolean enoughFood) {
        return enoughFood && com.thirstwastaken2.data.ThirstManager.allowsSprinting((LocalPlayer) (Object) this);
    }
    *///?}
}
