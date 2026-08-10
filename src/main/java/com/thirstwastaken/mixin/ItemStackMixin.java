package com.thirstwastaken.mixin;

import com.thirstwastaken.api.ThirstApi;
import com.thirstwastaken.data.ThirstManager;
import com.thirstwastaken.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    private static final int HYDRATION_TOOLTIP_COLOR = 0x55AAFF;

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void thirst$onFinishUsing(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        ThirstManager.drinkItem(player, (ItemStack) (Object) this);
    }

    @Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
    private void thirst$addPurityTooltip(Item.TooltipContext context, TooltipDisplay display, Player player,
                                         TooltipFlag flag, Consumer<Component> tooltip, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (WaterPurity.isWaterContainer(stack)) {
            tooltip.accept(WaterPurity.tooltip(WaterPurity.get(stack)));
        }
        int[] hydration = ThirstApi.hydration(stack);
        if (hydration != null) {
            tooltip.accept(Component.translatable("thirstwastaken.tooltip.hydration",
                    hydration[0], hydration[1]).withColor(HYDRATION_TOOLTIP_COLOR));
        }
    }
}
