package com.thirstwastaken.purity;

import com.thirstwastaken.item.ThirstItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Blocks;

public final class WaterInteractions {
    private static final java.util.Queue<Runnable> END_OF_TICK = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private WaterInteractions() { }

    public static InteractionResult fillBowl(net.minecraft.world.entity.player.Player player,
                                             net.minecraft.world.level.Level level, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ThirstItems.TERRACOTTA_BOWL)) return InteractionResult.PASS;
        var start = player.getEyePosition();
        var end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER))
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack filled = WaterPurity.set(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL),
                    WaterPurity.at(level, hit.getBlockPos()));
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public static InteractionResult transferCauldronPurity(net.minecraft.world.entity.player.Player player,
                                                            net.minecraft.world.level.Level level, InteractionHand hand,
                                                            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.PASS;
        var before = level.getBlockState(hit.getBlockPos());
        if (!before.is(Blocks.CAULDRON) && !before.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        boolean filling = WaterPurity.isWaterContainer(held);
        boolean extracting = before.is(Blocks.WATER_CAULDRON)
                && (held.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || held.is(net.minecraft.world.item.Items.BUCKET));
        if (!filling && !extracting) return InteractionResult.PASS;
        int purity = filling ? WaterPurity.get(held) : WaterPurity.at(level, hit.getBlockPos());
        if (filling && before.hasProperty(WaterPurity.BLOCK_PURITY)) {
            int old = before.getValue(WaterPurity.BLOCK_PURITY);
            if (old > 0) purity = Math.min(purity, old - 1);
        }
        final int transferredPurity = purity;
        END_OF_TICK.add(() -> {
            var after = level.getBlockState(hit.getBlockPos());
            if (filling && after.hasProperty(WaterPurity.BLOCK_PURITY))
                level.setBlock(hit.getBlockPos(), after.setValue(WaterPurity.BLOCK_PURITY, transferredPurity + 1), 3);
            if (extracting) {
                ItemStack result = player.getItemInHand(hand);
                if (WaterPurity.isWaterContainer(result)) WaterPurity.set(result, transferredPurity);
            }
        });
        return InteractionResult.PASS;
    }

    public static void tick(net.minecraft.server.MinecraftServer server) {
        Runnable action;
        while ((action = END_OF_TICK.poll()) != null) action.run();
    }
}
