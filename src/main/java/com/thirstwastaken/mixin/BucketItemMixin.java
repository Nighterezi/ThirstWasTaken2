package com.thirstwastaken.mixin;

import com.thirstwastaken.purity.WaterPurity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
abstract class BucketItemMixin {
    @Unique private int thirst$capturedPurity = -1;

    @Inject(method = "use", at = @At("HEAD"))
    private void thirst$capturePurity(Level level, Player player, InteractionHand hand,
                                      CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        var start = player.getEyePosition();
        var end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        thirst$capturedPurity = hit.getType() == HitResult.Type.BLOCK
                && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)
                ? WaterPurity.at(level, hit.getBlockPos()) : -1;
    }

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack thirst$addPurity(ItemStack result) {
        return thirst$capturedPurity < 0 ? result : WaterPurity.set(result, thirst$capturedPurity);
    }
}
