package com.thirstwastaken2.kaleidoscope.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thirstwastaken2.kaleidoscope.BrewedWater;
import com.thirstwastaken2.kaleidoscope.BrewedWaterQuality;
import com.thirstwastaken2.kaleidoscope.ReturnedWater;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The teapot keeps the grade of the water poured into it: back out in a bucket, and in the item when the
 * teapot is picked up and placed again. It refuses sea water, as the Cooking Pot does: tea brewed from
 * the sea would come out as safe as any other, a free way to make it drinkable.
 *
 * <p>The tea itself is not affected. It is brewed from boiled water, so a cup restores its fixed value
 * whatever went into the pot.
 */
@Mixin(value = TeapotBlockEntity.class, remap = false)
abstract class TeapotBlockEntityMixin implements BrewedWater {
    @Shadow private int status;
    @Shadow private Identifier teaFluidId;

    /** What was poured in. Only meaningful while {@link #thirst$heldWater} says there is water. */
    @Unique private WaterQuality thirst$quality;

    @Override
    public WaterQuality thirst$heldWater() {
        return status == ITeapot.PUT_INGREDIENT && BrewedWaterQuality.isWater(teaFluidId) ? thirst$quality : null;
    }

    @Override
    public void thirst$holdWater(WaterQuality quality) {
        thirst$quality = quality;
    }

    @WrapMethod(method = "addTeaFluid")
    private boolean thirst$keepPouredGrade(Level level, LivingEntity user, ItemStack itemStack, Operation<Boolean> original) {
        // Read before the call: it empties the bucket.
        WaterQuality poured = BrewedWaterQuality.of(itemStack);
        // Refused only where the teapot would otherwise take it, so a full or busy teapot still says why
        // in its own words.
        if (poured != null && poured.salty() && status == ITeapot.PUT_INGREDIENT
                && TeapotRecipeSerializer.EMPTY_TEA_FLUID.equals(teaFluidId)) {
            BrewedWaterQuality.refuseSalt(user);
            return false;
        }
        boolean added = original.call(level, user, itemStack);
        if (added) thirst$quality = poured;
        return added;
    }

    @WrapMethod(method = "removeTeaFluid")
    private boolean thirst$returnGrade(Level level, LivingEntity user, ItemStack itemStack, Operation<Boolean> original) {
        boolean removed = ReturnedWater.during(thirst$heldWater(), () -> original.call(level, user, itemStack));
        if (removed) thirst$quality = null;
        return removed;
    }

    /**
     * The teapot picked up with water in it: {@code getDrops} builds the item's tag from nothing, holding
     * only the fluid id. A finished teapot writes a tag too, but holds no water, so nothing is added there.
     */
    @ModifyArg(method = "getDrops", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BlockItem;setBlockEntityData(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/nbt/CompoundTag;)V"),
            index = 2)
    private CompoundTag thirst$keepGradeInItem(CompoundTag tag) {
        BrewedWaterQuality.save(tag, thirst$heldWater());
        return tag;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void thirst$saveGrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        BrewedWaterQuality.save(tag, thirst$heldWater());
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void thirst$loadGrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        thirst$quality = BrewedWaterQuality.load(tag);
    }
}
