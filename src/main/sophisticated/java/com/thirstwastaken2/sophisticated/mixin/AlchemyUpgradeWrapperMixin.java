package com.thirstwastaken2.sophisticated.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.p3pp3rf1y.sophisticatedcore.upgrades.alchemy.AlchemyCondition;
import net.p3pp3rf1y.sophisticatedcore.upgrades.alchemy.AlchemyFilterAttribute;
import net.p3pp3rf1y.sophisticatedcore.upgrades.alchemy.AlchemyUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The Alchemy upgrade drinks and eats on a condition, and like the Feeding upgrade it finishes through
 * {@code Item.finishUsingItem}, past the mod's hook. Its item definitions do that in lambdas, so both
 * hooks sit in the two named methods around them instead: the one call in {@code tick} that finishes
 * whatever is being applied, and the condition check in {@code applyTo} that starts it.
 */
@Mixin(value = AlchemyUpgradeWrapper.class, remap = false)
abstract class AlchemyUpgradeWrapperMixin {
    /**
     * Plain water is left to the player, so it is never drunk at a full bar or without a choice about
     * its grade. Sophisticated's own potion definition already skips a potion without effects; this
     * also covers a definition another mod adds. Refusing at the condition means nothing is taken out
     * of the backpack, where refusing at the end would leave the upgrade starting to drink again every
     * check.
     */
    @WrapOperation(method = "applyTo", at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/upgrades/alchemy/AlchemyCondition;test(Lnet/minecraft/world/entity/LivingEntity;F)Z"))
    private boolean thirst$refusePlainWater(AlchemyCondition condition, LivingEntity entity, float value,
                                            Operation<Boolean> original, @Local AlchemyFilterAttribute attribute) {
        return !WaterPurity.isPlainWaterDrink(attribute.filter()) && original.call(condition, entity, value);
    }

    /** Thirst for what a player drinks or eats, before the item is used up. A thrown potion is neither. */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/upgrades/alchemy/AlchemyUpgradeWrapper$FinishUsing;apply(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack thirst$quenchAppliedItem(AlchemyUpgradeWrapper.FinishUsing finishUsing, ItemStack stack,
                                               LivingEntity entity, Operation<ItemStack> original) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            UseAnim animation = stack.getUseAnimation();
            if (animation == UseAnim.DRINK || animation == UseAnim.EAT) ThirstManager.drinkItem(player, stack);
        }
        return original.call(finishUsing, stack, entity);
    }
}
