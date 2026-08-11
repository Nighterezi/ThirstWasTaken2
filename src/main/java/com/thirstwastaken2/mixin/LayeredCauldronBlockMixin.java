package com.thirstwastaken2.mixin;

import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayeredCauldronBlock.class)
abstract class LayeredCauldronBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    private void thirst$addPurity(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WaterPurity.BLOCK_PURITY);
    }
}
