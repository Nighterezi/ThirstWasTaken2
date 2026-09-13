package com.thirstwastaken2.mixin;

import com.thirstwastaken2.data.HealthRegen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
    // FoodData#tick took any Player until 1.21.2 narrowed it to ServerPlayer, and heals through it.
    //? if >=1.21.2 {
    private static final String HEAL = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V";
    //?} else
    /*private static final String HEAL = "Lnet/minecraft/world/entity/player/Player;heal(F)V";*/

    @Shadow public abstract void addExhaustion(float amount);

    @Shadow public abstract float getSaturationLevel();

    /** Heals skipped since the last one that was let through. */
    @Unique private int thirst$dehydratedHealTimer;

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = HEAL))
    //? if >=1.21.2 {
    private void thirst$healWithSaturation(ServerPlayer player, float amount) {
    //?} else
    /*private void thirst$healWithSaturation(Player player, float amount) {*/
        if (!HealthRegen.blocksSaturationHeal(player)) {
            player.heal(amount);
            return;
        }
        if (HealthRegen.allowsSlowHeal(player, ++thirst$dehydratedHealTimer)) {
            thirst$dehydratedHealTimer = 0;
            player.heal(amount);
            return;
        }
        addExhaustion(-Math.min(getSaturationLevel(), HealthRegen.MAX_REFUND));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 1, target = HEAL))
    //? if >=1.21.2 {
    private void thirst$healWithHunger(ServerPlayer player, float amount) {
    //?} else
    /*private void thirst$healWithHunger(Player player, float amount) {*/
        if (HealthRegen.blocksHungerHeal(player)) {
            addExhaustion(-HealthRegen.MAX_REFUND);
        } else {
            player.heal(amount);
        }
    }
}
