package com.thirstwastaken2.kaleidoscope.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.item.TeapotItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.thirstwastaken2.kaleidoscope.BrewedWaterQuality;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * An empty teapot held in the hand scoops water straight out of the world, the way a bucket does, and
 * writes only the fluid id into the item. The water is sampled where it lay, as a bucket's is, and sea
 * water is refused, as the placed teapot refuses a bucket of it. Without this a teapot dipped in the
 * sea and emptied into a bucket turned sea water fresh.
 */
@Mixin(value = TeapotItem.class, remap = false)
abstract class TeapotItemMixin {
    // Sampled before the call: it takes the source block away. Both sides sample, so the client agrees
    // with the server about a refusal rather than showing a teapot that fills and then empties again.
    @WrapOperation(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack thirst$sampleScooped(BucketPickup pickup, Player player, LevelAccessor level, BlockPos pos,
                                          BlockState state, Operation<ItemStack> original,
                                          @Share("scooped") LocalRef<WaterQuality> scooped) {
        WaterQuality quality = BrewedWaterQuality.sample(level, pos, state);
        if (quality != null && quality.salty()) {
            BrewedWaterQuality.refuseSalt(player);
            return ItemStack.EMPTY;
        }
        scooped.set(quality);
        return original.call(pickup, player, level, pos, state);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopecookery/item/TeapotItem;fillFluid(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean thirst$stampScooped(ItemStack teapot, Fluid fluid, LivingEntity user, Operation<Boolean> original,
                                        @Share("scooped") LocalRef<WaterQuality> scooped) {
        boolean filled = original.call(teapot, fluid, user);
        if (filled) BrewedWaterQuality.stampItem(teapot, scooped.get());
        return filled;
    }
}
