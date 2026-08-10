package com.thirstwastaken.mixin;

import com.thirstwastaken.config.ThirstConfig;
import com.thirstwastaken.data.ThirstManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reproduces the original MixinFoodData: dehydration stops natural regeneration, and the food cost
 * vanilla would have charged for the skipped heal is refunded so hunger is not silently drained.
 */
@Mixin(FoodData.class)
abstract class FoodDataMixin {
    @Shadow public abstract void addExhaustion(float amount);

    @Shadow public abstract float getSaturationLevel();

    /** Lets a nearly-hydrated player still regenerate, just eight times slower. */
    @Unique private int thirst$dehydratedHealTimer;

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void thirst$healWithSaturation(ServerPlayer player, float amount) {
        int thirst = ThirstManager.get(player).thirst();
        if (!ThirstConfig.get().dehydrationHaltsHealthRegen || thirst >= 20) {
            player.heal(amount);
            return;
        }

        if (++thirst$dehydratedHealTimer >= 8 && thirst > 18) {
            thirst$dehydratedHealTimer = 0;
            player.heal(amount);
            return;
        }

        addExhaustion(-Math.min(getSaturationLevel(), 6.0F));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void thirst$healWithHunger(ServerPlayer player, float amount) {
        if (!ThirstConfig.get().dehydrationHaltsHealthRegen || ThirstManager.get(player).thirst() > 18) {
            player.heal(amount);
        } else {
            addExhaustion(-6.0F);
        }
    }
}
