package com.thirstwastaken2.mixin;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
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

@Mixin(BottleItem.class)
abstract class BottleItemMixin {
    @Unique private final ThreadLocal<WaterQuality> thirst$capturedQuality = new ThreadLocal<>();

    @Inject(method = "use", at = @At("HEAD"))
    private void thirst$capturePurity(Level level, Player player, InteractionHand hand,
                                      CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        thirst$capturedQuality.remove();
        // Sampling inspects a small fixed neighborhood. The server result is authoritative and
        // synchronizes the filled stack, so repeating that work for client prediction is wasteful.
        if (level.isClientSide()) return;
        var start = player.getEyePosition();
        var end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() == HitResult.Type.BLOCK
                && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            thirst$capturedQuality.set(WaterPurity.sampleAt(level, hit.getBlockPos()));
        }
    }

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BottleItem;turnBottleIntoItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack thirst$addPurity(ItemStack result) {
        WaterQuality quality = thirst$capturedQuality.get();
        thirst$capturedQuality.remove();
        return quality == null ? result : WaterPurity.setQuality(result, quality);
    }
}
