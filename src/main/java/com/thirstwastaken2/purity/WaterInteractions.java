package com.thirstwastaken2.purity;

import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Queue;

/** Purity-aware handling of the interactions vanilla performs on water blocks and cauldrons. */
public final class WaterInteractions {
    /**
     * Cauldron fills and drains are resolved by vanilla after our callback returns, so the purity
     * transfer has to be applied once the block and the resulting item have settled. Only ever
     * touched from the server thread.
     */
    private static final Queue<Runnable> END_OF_TICK = new ArrayDeque<>();
    /** Block update + client notify, matching what vanilla cauldron interactions use. */
    private static final int BLOCK_UPDATE_FLAGS = 3;

    private WaterInteractions() { }

    /** Lets the terracotta bowl and waterskin scoop from any water, including flowing water. */
    public static InteractionResult fillFromWater(Player player, Level level, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean bowl = held.is(ThirstItems.TERRACOTTA_BOWL);
        boolean waterskin = held.is(ThirstItems.WATERSKIN) && WaterskinItem.servings(held) < WaterskinItem.CAPACITY;
        if (!bowl && !waterskin) return InteractionResult.PASS;

        BlockHitResult hit = pick(player, level, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = hit.getBlockPos();
        int purity = WaterPurity.at(level, pos);
        if (bowl) {
            ItemStack filled = WaterPurity.set(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), purity);
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
        } else {
            WaterskinItem.addWater(held, purity, 1);
        }
        level.playSound(null, player.blockPosition(), bowl ? SoundEvents.BUCKET_FILL : SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Draws one serving from a water cauldron and lowers it by one vanilla layer. */
    public static InteractionResult fillWaterskinFromCauldron(Player player, Level level, InteractionHand hand,
                                                               BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ThirstItems.WATERSKIN) || WaterskinItem.servings(held) >= WaterskinItem.CAPACITY) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        WaterskinItem.addWater(held, WaterPurity.at(level, pos), 1);
        LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Mirrors the original {@code fillablesHandler}: pouring a container into a cauldron stores the
     * worse of the two purities, and drawing from a cauldron stamps the resulting container.
     */
    public static InteractionResult transferCauldronPurity(Player player, Level level, InteractionHand hand,
                                                           BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState before = level.getBlockState(pos);
        boolean cauldron = before.is(Blocks.CAULDRON) || before.is(Blocks.WATER_CAULDRON);
        if (!cauldron) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        // Waterskins draw from cauldrons in fillWaterskinFromCauldron; vanilla has no matching
        // interaction that could actually pour them back, so do not schedule a phantom transfer.
        if (held.is(ThirstItems.WATERSKIN)) return InteractionResult.PASS;
        boolean filling = WaterPurity.isWaterContainer(held);
        boolean draining = before.is(Blocks.WATER_CAULDRON)
                && (held.is(Items.GLASS_BOTTLE) || held.is(Items.BUCKET));
        if (!filling && !draining) return InteractionResult.PASS;

        int purity = filling ? WaterPurity.get(held) : WaterPurity.at(level, pos);
        if (filling && before.hasProperty(WaterPurity.BLOCK_PURITY)) {
            int stored = before.getValue(WaterPurity.BLOCK_PURITY);
            if (stored > 0) purity = Math.min(purity, stored - 1);
        }

        int transferred = Math.max(WaterPurity.MIN, Math.min(WaterPurity.MAX, purity));
        END_OF_TICK.add(filling
                ? () -> storeInCauldron(level, pos, transferred)
                : () -> stampDrawnContainer(player, hand, transferred));
        return InteractionResult.PASS;
    }

    public static void tick(MinecraftServer server) {
        Runnable action;
        while ((action = END_OF_TICK.poll()) != null) action.run();
    }

    private static void storeInCauldron(Level level, BlockPos pos, int purity) {
        BlockState after = level.getBlockState(pos);
        if (!after.hasProperty(WaterPurity.BLOCK_PURITY)) return;
        // Stored purity is offset by one so that zero can act as "unset".
        level.setBlock(pos, after.setValue(WaterPurity.BLOCK_PURITY, purity + 1), BLOCK_UPDATE_FLAGS);
    }

    /**
     * The filled container does not necessarily end up back in the interaction hand - a stacked
     * bottle sends the water bottle to the first free inventory slot - so the first freshly created,
     * still unstamped container wins.
     */
    private static void stampDrawnContainer(Player player, InteractionHand hand, int purity) {
        if (stamp(player.getItemInHand(hand), purity)) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (stamp(inventory.getItem(slot), purity)) return;
        }
    }

    private static boolean stamp(ItemStack stack, int purity) {
        if (stack.isEmpty() || stack.has(ThirstComponents.WATER_PURITY) || !WaterPurity.isWaterContainer(stack)) {
            return false;
        }
        WaterPurity.set(stack, purity);
        return true;
    }

    private static BlockHitResult pick(Player player, Level level, ClipContext.Fluid fluidMode) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, fluidMode, player));
    }
}
