package com.thirstwastaken2.mixin;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ExhaustionTracker;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin implements ExhaustionTracker.Holder {
    /** Vanilla's sprint gate is foodLevel > 6; the original mod applied the same cut-off to thirst. */
    private static final int SPRINT_THIRST_THRESHOLD = 6;

    /** Created on first use, so client-side players, which never drain, do not carry one. */
    @Unique private ExhaustionTracker thirst$tracker;

    @Override
    public ExhaustionTracker thirst$exhaustionTracker() {
        if (thirst$tracker == null) thirst$tracker = new ExhaustionTracker();
        return thirst$tracker;
    }

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"))
    private void thirst$mirrorExhaustion(float amount, CallbackInfo ci) {
        ThirstManager.mirrorExhaustion((Player) (Object) this, amount);
    }

    @Inject(method = "canSprint", at = @At("RETURN"), cancellable = true)
    private void thirst$preventSprintingWhenDehydrated(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !ThirstConfig.get().preventSprintingWhenThirsty) return;
        ThirstData data = ThirstManager.get((Player) (Object) this);
        if (data.enabled() && data.thirst() <= SPRINT_THIRST_THRESHOLD) cir.setReturnValue(false);
    }
}
