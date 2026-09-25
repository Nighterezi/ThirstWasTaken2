package com.thirstwastaken2.brewinandchewin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.thirstwastaken2.brewinandchewin.KegWater;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;

/**
 * Where the keg builds the container it hands back through a pouring recipe: a bucket or a bottle made
 * from the recipe's output, with no grade. Stamped with the grade of the water in the tank, read before
 * the drain that follows.
 *
 * <p>{@code fluidExtract} builds the same result when a full container is poured in, only to compare it
 * with the one in hand, so a full water container in hand is left alone: stamping it there would make a
 * plain bottle stop matching the plain one the bottle recipe expects.
 */
@Mixin(value = KegBlockEntity.class, remap = false)
abstract class KegBlockEntityMixin {
    @Shadow
    @Final
    private AbstractedFluidTank fluidTank;

    // By name alone: the Fabric jar names Minecraft types in intermediary, so a descriptor would not
    // match there. fluidExtract calls only the one overload.
    @ModifyExpressionValue(method = "fluidExtract",
            at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/common/crafting/KegPouringRecipe;assemble"))
    private ItemStack thirst$gradeOfDrawnWater(ItemStack result, @Local(argsOnly = true) ItemStack slotIn) {
        return WaterPurity.isWaterContainer(slotIn) ? result : KegWater.drawn(result, fluidTank.getAbstractedFluid());
    }
}
