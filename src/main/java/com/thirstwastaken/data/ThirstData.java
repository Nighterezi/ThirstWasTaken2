package com.thirstwastaken.data;

import com.thirstwastaken.config.ThirstConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thirstwastaken.ThirstWasTaken;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ThirstData(int thirst, int quenched, float exhaustion, boolean enabled) {
    public static final int MAX = 20;

    public static final Codec<ThirstData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("thirst").forGetter(ThirstData::thirst),
            Codec.INT.fieldOf("quenched").forGetter(ThirstData::quenched),
            Codec.FLOAT.fieldOf("exhaustion").forGetter(ThirstData::exhaustion),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(ThirstData::enabled)
    ).apply(instance, ThirstData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ThirstData::thirst,
            ByteBufCodecs.VAR_INT, ThirstData::quenched,
            ByteBufCodecs.FLOAT, ThirstData::exhaustion,
            ByteBufCodecs.BOOL, ThirstData::enabled,
            ThirstData::new
    );

    public static final AttachmentType<ThirstData> TYPE = AttachmentRegistry.create(ThirstWasTaken.id("player_data"), builder -> builder
            .initializer(ThirstData::full)
            .persistent(CODEC)
            .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

    private static boolean registered;

    public static void register() {
        if (!registered) registered = true;
    }

    public static ThirstData full() {
        return new ThirstData(MAX, 5, 0.0F, true);
    }

    public ThirstData drink(int hydration, int quenchness) {
        int overflow = ThirstConfig.get().extraHydrationConvertsToQuenched
                ? Math.max(thirst + hydration - MAX, 0) : 0;
        int newThirst = Math.min(MAX, thirst + hydration);
        int newQuenched = Math.min(newThirst, quenched + quenchness + overflow);
        return new ThirstData(newThirst, newQuenched, exhaustion, enabled);
    }

    public ThirstData addExhaustion(float amount) {
        return new ThirstData(thirst, quenched, Math.max(0.0F, exhaustion + amount), enabled);
    }

    public ThirstData consumeExhaustion(boolean peaceful) {
        if (!enabled || exhaustion <= 4.0F) return this;
        float nextExhaustion = exhaustion - 4.0F;
        if (quenched > 0) return new ThirstData(thirst, quenched - 1, nextExhaustion, true);
        return new ThirstData(peaceful ? thirst : Math.max(0, thirst - 1), 0, nextExhaustion, true);
    }

    public ThirstData withLevels(int thirst, int quenched) {
        int safeThirst = Math.max(0, Math.min(MAX, thirst));
        return new ThirstData(safeThirst, Math.max(0, Math.min(safeThirst, quenched)), exhaustion, enabled);
    }

    public ThirstData withEnabled(boolean value) {
        return new ThirstData(thirst, quenched, exhaustion, value);
    }
}
