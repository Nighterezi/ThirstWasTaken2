package com.thirstwastaken2.mixin;

import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.tooltip.ThirstTooltip;
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
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void thirst$onFinishUsing(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        ThirstManager.drinkItem(player, (ItemStack) (Object) this);
    }

    @Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
    private void thirst$addPurityTooltip(Item.TooltipContext context, TooltipDisplay display, Player player,
                                         TooltipFlag flag, Consumer<Component> tooltip, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.is(ThirstItems.WATERSKIN)) {
            int servings = WaterskinItem.servings(stack);
            tooltip.accept(servings == 0
                    ? Component.translatable("tooltip.thirstwastaken2.waterskin.empty")
                    : Component.translatable("tooltip.thirstwastaken2.waterskin.servings",
                            servings, WaterskinItem.CAPACITY));
        }
        if (WaterPurity.isWaterContainer(stack)) {
            tooltip.accept(WaterPurity.tooltip(WaterPurity.get(stack)));
            if (WaterPurity.isSalty(stack)) tooltip.accept(WaterPurity.salinityTooltip());
        }
        int[] hydration = ThirstApi.hydration(stack);
        if (hydration != null) {
            Component droplets = ThirstTooltip.hydration(hydration[0], hydration[1]);
            if (droplets != null) tooltip.accept(droplets);
        }
    }
}
