package com.thirstwastaken2.fabric.mixin;

import com.thirstwastaken2.client.HandDrinking;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hand drinking from water the crosshair misses. Vanilla then goes on with the click as usual, so an
 * item in the other hand is still used.
 */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void thirst$drinkFromWaterOutsideTheCrosshair(CallbackInfo ci) {
        HandDrinking.useWaterOutsideTheCrosshair((Minecraft) (Object) this);
    }
}
