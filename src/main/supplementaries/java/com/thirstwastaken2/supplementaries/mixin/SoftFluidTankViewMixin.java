package com.thirstwastaken2.supplementaries.mixin;

import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.items.components.SoftFluidTankView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * The tooltip of a jar that holds water, which keeps what it held when it was broken. Supplementaries
 * writes the fluid and how much of it there is; the grade goes on the line after, in the same words the
 * mod uses for a bottle or a bowl of the same water.
 *
 * <p>The tank itself is read rather than copied out of it: a tooltip is rebuilt every frame the stack
 * is hovered, and the copy this view hands out is not free.
 */
@Mixin(value = SoftFluidTankView.class, remap = false)
abstract class SoftFluidTankViewMixin {
    @Shadow
    @Final
    private SoftFluidTank inner;

    @Inject(method = "addToTooltip", at = @At("TAIL"))
    private void thirst$addGrade(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag,
                                 CallbackInfo ci) {
        SoftFluidQuality.tooltip(inner.getFluid(), tooltip);
    }
}
