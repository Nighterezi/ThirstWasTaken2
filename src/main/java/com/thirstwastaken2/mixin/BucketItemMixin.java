package com.thirstwastaken2.mixin;

import com.thirstwastaken2.purity.FillCapture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
abstract class BucketItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void thirst$capturePurity(Level level, Player player, InteractionHand hand,
                                      CallbackInfoReturnable<InteractionResult> cir) {
        FillCapture.capture(level, player);
    }

    /**
     * {@code use} calls {@code createFilledResult} twice: once for the branch that empties the
     * bucket and once for the branch that fills it. Only the filling one may be stamped.
     *
     * <p>The two branches are not in the same order on every version, so they cannot be told apart
     * by a plain {@code ordinal}: 26.2 emits the emptying branch first, 26.1 and 1.21.11 emit the
     * filling branch first. Slicing from {@code pickupBlock}, which only the filling branch calls,
     * identifies it on both orderings without a version fork.
     */
    @ModifyArg(method = "use", index = 2, slice = @Slice(from = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;")),
            at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack thirst$addPurity(ItemStack filled) {
        return FillCapture.stamp(filled);
    }
}
