package com.thirstwastaken2.mixin;

import com.thirstwastaken2.data.ExhaustionTracker;
import com.thirstwastaken2.data.ThirstManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin implements ExhaustionTracker.Holder {
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

    // The food check LocalPlayer asks before it starts or keeps a sprint. Not Player#canSprint, which
    // only answers whether something riding the player may sprint it. 1.21.1 keeps the check on
    // LocalPlayer itself instead, so LocalPlayerMixin in the client source set covers that version.
    // A return-value modifier rather than a cancellable @Inject: it runs every tick, and a cancellable
    // inject allocates a callback object on every call.
    //? if >1.21.1 {
    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "hasEnoughFoodToDoExhaustiveManoeuvres", at = @At("RETURN"))
    private boolean thirst$preventSprintingWhenThirsty(boolean enoughFood) {
        return enoughFood && ThirstManager.allowsSprinting((Player) (Object) this);
    }
    //?}
}
