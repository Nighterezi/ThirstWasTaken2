package com.thirstwastaken2.data;

import com.thirstwastaken2.advancement.ThirstAdvancements;
import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.damage.ThirstDamageTypes;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.platform.Vanilla;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
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
    /** Matches the original mod's syncTimer cadence for peaceful regeneration. */
    private static final int SLOW_TICK_INTERVAL = 11;
    private static final int DAMAGE_INTERVAL = 40;
    /** Softens any modifier below 1, exactly like the original MODIFIER_HARSHNESS. */
    private static final float MODIFIER_HARSHNESS = 0.5F;
    /** Nausea's extra drain per tick, from the original DEPLETES_WHEN_NAUSEA branch. */
    private static final float NAUSEA_EXHAUSTION = 0.06F;
    /** What {@code HungerMobEffect#applyEffectTick} charges per amplifier level, every tick. */
    private static final float HUNGER_EXHAUSTION = 0.005F;
    /**
     * How long a computed exhaustion modifier is reused. Climate, armour and Fire Resistance change far
     * more slowly than vanilla charges exhaustion, and it takes seconds of exhaustion to spend a single
     * point, so a lag of one second cannot be seen.
     */
    private static final int MODIFIER_REFRESH_TICKS = 20;
    /**
     * The tick only writes exhaustion, and so only syncs it, once it has moved into another step of this size
     * or spent a point. The client draws exhaustion as the HUD's partly drained droplet, which changes at 0 and
     * 2, and as AppleSkin's 81 px strip; a quarter point keeps both readable while a sprinting player costs
     * about two sync packets a second instead of one every tick. The unwritten remainder stays on the
     * player's {@link ExhaustionTracker}, so the drain itself is exact.
     *
     * <p>Diverges from the original mod, which saved every change: a player who leaves loses less than one
     * step of exhaustion, a sixteenth of a point.
     */
    private static final float SYNC_STEP = 0.25F;

    private ThirstManager() { }

    public static ThirstData get(Player player) {
        return player.getAttachedOrCreate(ThirstData.TYPE);
    }

    public static void set(Player player, ThirstData data) {
        player.setAttached(ThirstData.TYPE, data);
    }

    /** Applies exhaustion straight away, for one-off sources such as drinking salt water. */
    public static void addExhaustion(Player player, float amount) {
        if (!drains(player, amount)) return;
        ThirstData data = get(player);
        if (!data.enabled()) return;
        set(player, data.addExhaustion(amount * exhaustionModifier(player)));
    }

    /**
     * Mirrors vanilla food exhaustion. Vanilla charges it on nearly every tick a player sprints, swims,
     * jumps or fights, sometimes more than once a tick, and every attachment write sends a sync packet.
     * The raw amount is therefore only collected here and applied once per tick by {@link #tick}.
     */
    public static void mirrorExhaustion(Player player, float amount) {
        if (drains(player, amount)) ExhaustionTracker.of(player).pending += amount;
    }

    /** Riding a mount does not dehydrate you, matching the original's isSitting guard. */
    private static boolean drains(Player player, float amount) {
        return amount != 0.0F && !player.level().isClientSide()
                && !player.getAbilities().invulnerable && !player.isPassenger();
    }

    public static void drink(Player player, int hydration, int quenched) {
        if (!player.level().isClientSide()) set(player, get(player).drink(hydration, quenched));
    }

    /** Plain water follows vanilla food rules: it cannot be consumed while the visible bar is full. */
    public static boolean canDrinkWater(Player player) {
        ThirstData data = get(player);
        return !data.enabled() || player.getAbilities().invulnerable || data.thirst() < ThirstData.MAX;
    }

    public static void drinkItem(Player player, ItemStack stack) {
        int[] value = ThirstApi.hydration(stack);
        if (value == null) return;
        boolean quenches = WaterPurity.applyEffects(player, stack);
        // Salt water is drunk without quenching anything, and still counts as having been drunk.
        if (WaterPurity.isWaterContainer(stack)) {
            ThirstAdvancements.drank(player, WaterPurity.quality(stack));
        }
        if (quenches) drink(player, value[0], value[1]);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) tickPlayer(player);
    }

    /**
     * One player's share of {@link #tick}. Public for the dev benchmark, which ticks simulated players
     * that are not in the player list.
     */
    public static void tickPlayer(ServerPlayer player) {
        ExhaustionTracker tracker = ExhaustionTracker.of(player);
        float mirrored = tracker.pending;
        float unsynced = tracker.unsynced;
        tracker.pending = 0.0F;
        tracker.unsynced = 0.0F;

        ThirstData data = get(player);
        if (!data.enabled() || player.getAbilities().invulnerable) return;

        ThirstConfig config = ThirstConfig.get();
        Difficulty difficulty = player.level().getDifficulty();
        boolean peaceful = difficulty == Difficulty.PEACEFUL && !config.thirstDepletionInPeaceful;

        // The Hunger effect already routes through causeFoodExhaustion; the original cancels that
        // contribution back out so poisoned food does not double as dehydration. Both sides are raw
        // amounts from the same tick, so they cancel exactly instead of leaving float noise behind that
        // would still cost a sync packet.
        float raw = mirrored;
        MobEffectInstance hunger = player.getEffect(MobEffects.HUNGER);
        if (hunger != null) raw -= HUNGER_EXHAUSTION * (hunger.getAmplifier() + 1);
        if (config.depletesWhenNauseous && player.hasEffect(MobEffects.NAUSEA)) raw += NAUSEA_EXHAUSTION;

        float added = unsynced + (raw == 0.0F ? 0.0F : raw * exhaustionModifier(player));
        boolean slowTick = player.tickCount % SLOW_TICK_INTERVAL == 0;
        boolean regenerates = peaceful && slowTick && data.thirst() < ThirstData.MAX;
        // The same clamp ThirstData#addExhaustion applies.
        float exhaustion = Math.max(0.0F, data.exhaustion() + added);

        int thirst = data.thirst();
        if (!regenerates && exhaustion <= ThirstData.EXHAUSTION_PER_POINT
                && sameSyncStep(data.exhaustion(), exhaustion)) {
            // Nothing the client would draw differently: carry it rather than build a record and send it.
            tracker.unsynced = exhaustion - data.exhaustion();
        } else {
            ThirstData updated = data.addExhaustion(added).consumeExhaustion(peaceful);
            if (peaceful && slowTick) updated = updated.regenerate(1);
            if (!updated.equals(data)) set(player, updated);
            thirst = updated.thirst();
        }

        if (thirst <= 0 && player.tickCount % DAMAGE_INTERVAL == 0) {
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

        WaterQuality quality = WaterPurity.sampleAt(level, pos);
        ItemStack sample = WaterPurity.setQuality(
                new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
        if (WaterPurity.applyEffects(player, sample)) {
            drink(player, config.handDrinkingHydration, config.handDrinkingQuenched);
        }
        ThirstAdvancements.drank(player, quality);
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

    /** Whether two exhaustion values fall in the same {@link #SYNC_STEP}, so the client would draw them alike. */
    private static boolean sameSyncStep(float a, float b) {
        return Math.floor(a / SYNC_STEP) == Math.floor(b / SYNC_STEP);
    }

    /**
     * The exhaustion modifier, reused for {@link #MODIFIER_REFRESH_TICKS}. Reading armour protection
     * builds a loot context for every enchantment on every equipped item, and this runs on every tick a
     * player moves. Changing dimension or config recomputes it straight away.
     */
    private static float exhaustionModifier(Player player) {
        ExhaustionTracker tracker = ExhaustionTracker.of(player);
        Level level = player.level();
        int generation = ThirstConfig.generation();
        if (tracker.modifierLevel != level || tracker.modifierGeneration != generation
                || player.tickCount >= tracker.modifierExpiresAt) {
            tracker.modifier = computeExhaustionModifier(player);
            tracker.modifierLevel = level;
            tracker.modifierGeneration = generation;
            tracker.modifierExpiresAt = player.tickCount + MODIFIER_REFRESH_TICKS;
        }
        return tracker.modifier;
    }

    /**
     * Combined biome, fire-protection and fire-resistance multiplier applied to raw exhaustion,
     * mirroring {@code ThirstHelper#getExhaustionBiomeModifier} and friends from the original mod.
     */
    private static float computeExhaustionModifier(Player player) {
        ThirstConfig config = ThirstConfig.get();
        boolean scorching = Vanilla.waterEvaporates(player.level(), player.blockPosition());
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
