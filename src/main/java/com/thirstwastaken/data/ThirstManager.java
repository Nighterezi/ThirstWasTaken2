package com.thirstwastaken.data;

import com.thirstwastaken.item.ThirstItems;
import com.thirstwastaken.api.ThirstApi;
import com.thirstwastaken.config.ThirstConfig;
import com.thirstwastaken.purity.WaterPurity;
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
            ThirstConfig config = ThirstConfig.get();
            float modifier = (float) (player.level().dimension() == Level.NETHER
                    ? config.netherThirstDepletionModifier : biomeModifier(player) * config.thirstDepletionModifier);
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE))
                modifier *= config.fireResistanceDehydrationPercent / 100.0F;
            if (player instanceof ServerPlayer serverPlayer) {
                float protection = net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageProtection(
                        serverPlayer.level(), serverPlayer, serverPlayer.damageSources().onFire());
                modifier *= Math.max(0.25F, 1.0F - protection * 0.0234375F);
            }
            if (!config.depletesWhenNauseous && player.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA)) return;
            set(player, get(player).addExhaustion(amount * modifier));
        }
    }

    public static void drink(Player player, int hydration, int quenched) {
        if (!player.level().isClientSide()) set(player, get(player).drink(hydration, quenched));
    }

    public static void drinkItem(Player player, ItemStack stack) {
        int[] value = ThirstApi.hydration(stack);
        if (value != null && WaterPurity.applyEffects(player, stack)) drink(player, value[0], value[1]);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) tickPlayer(player);
    }

    private static void tickPlayer(ServerPlayer player) {
        ThirstData data = get(player);
        if (!data.enabled() || player.getAbilities().invulnerable) return;

        boolean peaceful = player.level().getDifficulty() == Difficulty.PEACEFUL
                && !ThirstConfig.get().thirstDepletionInPeaceful;
        ThirstData updated = data.consumeExhaustion(peaceful);

        if (ThirstConfig.get().canDrinkRain && player.tickCount % 11 == 0 && player.getXRot() <= -80.0F
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
        if (!ThirstConfig.get().canDrinkByHand || level.isClientSide() || !player.isCrouching() || !player.getItemInHand(hand).isEmpty()
                || player.getAbilities().invulnerable || get(player).thirst() >= ThirstData.MAX) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResult.PASS;

        if (WaterPurity.applyEffects(player, WaterPurity.set(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), WaterPurity.at(level, pos))))
            drink(player, ThirstConfig.get().handDrinkingHydration, ThirstConfig.get().handDrinkingQuenched);
        player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value(), 1.0F, 1.0F);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static float biomeModifier(Player player) {
        var biome = player.level().getBiome(player.blockPosition()).value();
        float humidity = biome.getPrecipitationAt(player.blockPosition(), 63)
                == net.minecraft.world.level.biome.Biome.Precipitation.NONE ? 0.65F : 1.25F;
        float temperature = biome.getBaseTemperature() + 0.2F;
        temperature = temperature <= 0 ? (float) Math.exp(temperature) : temperature > 1 ? temperature / 2 : temperature;
        float result = temperature / humidity;
        return result < 1 ? 1 - ((1 - result) * 0.5F) : result;
    }
}
