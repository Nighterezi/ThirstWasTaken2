package com.thirstwastaken.mixin;

import com.thirstwastaken.config.ThirstConfig;
import com.thirstwastaken.data.ThirstManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
abstract class FoodDataMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void thirst$limitNaturalRegeneration(ServerPlayer player, float amount) {
        if (!ThirstConfig.get().dehydrationHaltsHealthRegen || ThirstManager.get(player).thirst() > 18)
            player.heal(amount);
    }
}
