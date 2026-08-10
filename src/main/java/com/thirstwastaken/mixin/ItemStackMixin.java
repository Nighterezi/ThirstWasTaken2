package com.thirstwastaken.mixin;

import com.thirstwastaken.data.ThirstManager;
import com.thirstwastaken.purity.ThirstComponents;
import com.thirstwastaken.purity.WaterPurity;
import com.thirstwastaken.api.ThirstApi;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void thirst$onFinishUsing(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof Player player && !level.isClientSide()) {
            ThirstManager.drinkItem(player, (ItemStack) (Object) this);
        }
    }

    @Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
    private void thirst$addPurityTooltip(Item.TooltipContext context, TooltipDisplay display, Player player,
                                         TooltipFlag flag, Consumer<Component> tooltip, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        Integer purity = stack.get(ThirstComponents.WATER_PURITY);
        if (purity != null) tooltip.accept(WaterPurity.tooltip(purity));
        int[] hydration = ThirstApi.hydration(stack);
        if (hydration != null) tooltip.accept(Component.translatable("thirstwastaken.tooltip.hydration",
                hydration[0], hydration[1]).withColor(0x55AAFF));
    }
}
