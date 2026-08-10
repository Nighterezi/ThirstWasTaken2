package com.thirstwastaken.data;

import com.thirstwastaken.item.ThirstItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;

public final class ThirstManager {
    private ThirstManager() { }

    public static ThirstData get(Player player) {
        return player.getAttachedOrCreate(ThirstData.TYPE);
    }

    public static void set(Player player, ThirstData data) {
        player.setAttached(ThirstData.TYPE, data);
    }

    public static void addExhaustion(Player player, float amount) {
        if (!player.level().isClientSide() && !player.getAbilities().invulnerable) {
            float modifier = player.level().dimension() == Level.NETHER ? 3.0F : 1.2F;
            set(player, get(player).addExhaustion(amount * modifier));
        }
    }

    public static void drink(Player player, int hydration, int quenched) {
        if (!player.level().isClientSide()) set(player, get(player).drink(hydration, quenched));
    }

    public static void drinkItem(Player player, ItemStack stack) {
        if (stack.is(ThirstItems.TERRACOTTA_WATER_BOWL)) {
            drink(player, 4, 5);
            return;
        }

        String id = stack.getItem().builtInRegistryHolder().key().identifier().toString().toLowerCase(Locale.ROOT);
        if (stack.getUseAnimation() == ItemUseAnimation.DRINK) {
            drink(player, id.equals("minecraft:potion") ? 6 : 10, id.equals("minecraft:potion") ? 8 : 14);
        } else if (id.matches(".*(melon|apple|berries|berry|carrot|beetroot|soup|stew).*")) {
            int hydration = id.contains("melon") || id.contains("soup") ? 4 : 2;
            drink(player, hydration, hydration + 1);
        }
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) tickPlayer(player);
    }

    private static void tickPlayer(ServerPlayer player) {
        ThirstData data = get(player);
        if (!data.enabled() || player.getAbilities().invulnerable) return;

        boolean peaceful = player.level().getDifficulty() == Difficulty.PEACEFUL;
        ThirstData updated = data.consumeExhaustion(peaceful);

        if (player.tickCount % 11 == 0 && player.getXRot() <= -80.0F
                && player.level().isRainingAt(player.blockPosition().above())) {
            updated = updated.drink(1, 1);
        }

        if (!updated.equals(data)) set(player, updated);

        if (updated.thirst() <= 0 && player.tickCount % 40 == 0) {
            if (player.getHealth() > 10.0F || player.level().getDifficulty() == Difficulty.HARD
                    || (player.getHealth() > 1.0F && player.level().getDifficulty() == Difficulty.NORMAL)) {
                player.hurtServer(player.level(), player.damageSources().starve(), 1.0F);
            }
        }
    }

    public static InteractionResult drinkByHand(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || !player.isCrouching() || !player.getItemInHand(hand).isEmpty()
                || player.getAbilities().invulnerable || get(player).thirst() >= ThirstData.MAX) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResult.PASS;

        drink(player, 3, 2);
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value(), 1.0F, 1.0F);
        return InteractionResult.SUCCESS_SERVER;
    }
}
