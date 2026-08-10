package com.thirstwastaken.mixin;

import com.thirstwastaken.config.ThirstConfig;
import com.thirstwastaken.data.ThirstData;
import com.thirstwastaken.data.ThirstManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    /** Vanilla's sprint gate is foodLevel > 6; the original mod applied the same cut-off to thirst. */
    private static final int SPRINT_THIRST_THRESHOLD = 6;

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"))
    private void thirst$mirrorExhaustion(float amount, CallbackInfo ci) {
        ThirstManager.addExhaustion((Player) (Object) this, amount);
    }

    @Inject(method = "canSprint", at = @At("RETURN"), cancellable = true)
    private void thirst$preventSprintingWhenDehydrated(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !ThirstConfig.get().preventSprintingWhenThirsty) return;
        ThirstData data = ThirstManager.get((Player) (Object) this);
        if (data.enabled() && data.thirst() <= SPRINT_THIRST_THRESHOLD) cir.setReturnValue(false);
    }
}
