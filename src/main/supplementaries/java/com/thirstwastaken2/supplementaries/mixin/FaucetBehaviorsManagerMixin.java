package com.thirstwastaken2.supplementaries.mixin;

import com.google.gson.JsonElement;
import com.thirstwastaken2.supplementaries.HangingPotFaucet;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Registers the hanging pots with the faucet, where Supplementaries builds its own list of behaviours.
 *
 * <p>Supplementaries has a listener for this, but a listener is run after every built-in behaviour and
 * would need an entry point of its own to be added from, which this integration otherwise has none of:
 * everything else it does is a mixin. This is the same place at the same moment, so it inherits the
 * same order, and order does not matter for a block no built-in behaviour claims.
 */
@Mixin(value = FaucetBehaviorsManager.class, remap = false)
abstract class FaucetBehaviorsManagerMixin {
    @Shadow
    protected abstract void registerInteraction(Object interaction);

    @Inject(method = "apply", at = @At("RETURN"))
    private void thirst$addHangingPot(Map<Identifier, JsonElement> map, ResourceManager resources,
                                      ProfilerFiller profiler, CallbackInfo ci) {
        registerInteraction(new HangingPotFaucet());
    }
}
