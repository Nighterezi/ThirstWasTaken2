package com.thirstwastaken2.data;

import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.damage.ThirstDamageTypes;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;

public final class ThirstManager {
    /** Matches the original mod's syncTimer cadence for rain drinking and peaceful regeneration. */
    private static final int SLOW_TICK_INTERVAL = 11;
    private static final int DAMAGE_INTERVAL = 40;
    /** Softens any modifier below 1, exactly like the original MODIFIER_HARSHNESS. */
    private static final float MODIFIER_HARSHNESS = 0.5F;
    /** Nausea's extra drain per tick, from the original DEPLETES_WHEN_NAUSEA branch. */
    private static final float NAUSEA_EXHAUSTION = 0.06F;

    private ThirstManager() { }

    public static ThirstData get(Player player) {
        return player.getAttachedOrCreate(ThirstData.TYPE);
    }

    public static void set(Player player, ThirstData data) {
        player.setAttached(ThirstData.TYPE, data);
    }

    public static void addExhaustion(Player player, float amount) {
        // Riding a mount does not dehydrate you, matching the original's isSitting guard.
        if (player.level().isClientSide() || player.getAbilities().invulnerable
                || player.isPassenger() || amount == 0.0F) {
            return;
        }
        ThirstData data = get(player);
        if (!data.enabled()) return;
        set(player, data.addExhaustion(amount * exhaustionModifier(player)));
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

        ThirstConfig config = ThirstConfig.get();
        Difficulty difficulty = player.level().getDifficulty();
        boolean peaceful = difficulty == Difficulty.PEACEFUL && !config.thirstDepletionInPeaceful;
        ThirstData updated = data;

        // The Hunger effect already routes through causeFoodExhaustion; the original cancels that
        // contribution back out so poisoned food does not double as dehydration.
        MobEffectInstance hunger = player.getEffect(MobEffects.HUNGER);
        boolean nauseous = config.depletesWhenNauseous && player.hasEffect(MobEffects.NAUSEA);
        if (hunger != null || nauseous) {
            float modifier = exhaustionModifier(player);
            float delta = 0.0F;
            if (hunger != null) delta -= 0.005F * (hunger.getAmplifier() + 1) * modifier;
            if (nauseous) delta += NAUSEA_EXHAUSTION * modifier;
            updated = updated.addExhaustion(delta);
        }

        updated = updated.consumeExhaustion(peaceful);

        if (player.tickCount % SLOW_TICK_INTERVAL == 0) {
            if (peaceful) {
                updated = updated.regenerate(1);
            }
            if (config.canDrinkRain && Mth.wrapDegrees(player.getXRot()) <= -80.0F
                    && player.level().isRainingAt(player.blockPosition().above())) {
                updated = updated.drinkRain(1, 1);
            }
        }

        if (!updated.equals(data)) set(player, updated);

        if (updated.thirst() <= 0 && player.tickCount % DAMAGE_INTERVAL == 0) {
            float health = player.getHealth();
            if (health > 10.0F || difficulty == Difficulty.HARD
                    || (health > 0.0F && difficulty == Difficulty.NORMAL)) {
                ServerLevel level = player.level();
                player.hurtServer(level, ThirstDamageTypes.dehydration(level), 1.0F);
            }
        }
    }

    public static InteractionResult drinkByHand(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ThirstConfig config = ThirstConfig.get();
        if (!config.canDrinkByHand || level.isClientSide() || !player.isCrouching()
                || player.getAbilities().invulnerable || !get(player).enabled()
                || get(player).thirst() >= ThirstData.MAX) {
            return InteractionResult.PASS;
        }
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (config.drinkByHandNeedsBothHandsEmpty
                && !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
                && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            // The crosshair usually lands on the block beneath the surface when looking at water.
            pos = pos.relative(hit.getDirection());
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResult.PASS;
        }

        ItemStack sample = WaterPurity.set(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), WaterPurity.at(level, pos));
        if (WaterPurity.applyEffects(player, sample)) {
            drink(player, config.handDrinkingHydration, config.handDrinkingQuenched);
        }
        // Player#playSound routes through Level#playSound with itself as the excluded listener, so a
        // server-side call is heard by everyone *except* the drinker. Vanilla gets away with it
        // because consumption effects also run client-side; hand drinking is server-only, so the
        // sound has to be broadcast with no exclusion. Volume and pitch match
        // LivingEntity#triggerItemUseEffects, i.e. the potion drinking sound.
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS,
                0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Combined biome, fire-protection and fire-resistance multiplier applied to raw exhaustion,
     * mirroring {@code ThirstHelper#getExhaustionBiomeModifier} and friends from the original mod.
     */
    private static float exhaustionModifier(Player player) {
        ThirstConfig config = ThirstConfig.get();
        // WATER_EVAPORATES replaced DimensionType#ultraWarm; it still means "Nether-like".
        boolean scorching = Boolean.TRUE.equals(player.level().environmentAttributes()
                .getValue(EnvironmentAttributes.WATER_EVAPORATES, player.blockPosition()));
        float modifier = scorching
                ? (float) config.netherThirstDepletionModifier
                : climateModifier(player, config);

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            modifier *= config.fireResistanceDehydrationPercent / 100.0F;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            // getDamageProtection returns twice the enchantment level total, and the original scales
            // it by 0.0625 * 0.75 per level.
            float protection = EnchantmentHelper.getDamageProtection(
                    serverPlayer.level(), serverPlayer, serverPlayer.damageSources().onFire());
            modifier *= Math.max(0.25F, 1.0F - protection * 0.0234375F);
        }
        return modifier;
    }

    private static float climateModifier(Player player, ThirstConfig config) {
        BlockPos pos = player.blockPosition();
        Biome biome = player.level().getBiome(pos).value();

        // The original used Biome#getDownfall, which no longer exists. hasPrecipitation reproduces
        // the same dry/wet split within the original's effective 1.1 - 1.6 humidity range.
        float humidity = biome.hasPrecipitation() ? 1.4F : 1.1F;

        float temperature = biome.getBaseTemperature() + 0.2F;
        if (temperature <= 0.0F) {
            temperature = (float) Math.exp(temperature);
        } else if (temperature > 1.0F) {
            temperature *= 0.5F;
        }

        // The config multiplier is applied before the harshness softening, as in the original.
        float modifier = (float) config.thirstDepletionModifier * (temperature / humidity);
        return modifier < 1.0F ? 1.0F - (1.0F - modifier) * MODIFIER_HARSHNESS : modifier;
    }
}
