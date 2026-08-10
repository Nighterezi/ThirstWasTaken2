package com.thirstwastaken.mixin;

import com.thirstwastaken.data.ThirstManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"))
    private void thirst$mirrorExhaustion(float amount, CallbackInfo ci) {
        ThirstManager.addExhaustion((Player) (Object) this, amount);
    }

    @Inject(method = "canSprint", at = @At("RETURN"), cancellable = true)
    private void thirst$preventSprintingWhenDehydrated(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (cir.getReturnValue() && ThirstManager.get(player).enabled() && ThirstManager.get(player).thirst() <= 6) {
            cir.setReturnValue(false);
        }
    }
}
