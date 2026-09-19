package com.thirstwastaken2.sophisticated.drinking;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * How thirsty the player has to be before the Drinking upgrade drinks something, the thirst version of
 * Sophisticated Core's {@code HungerLevel}: as soon as anything is missing, once half of the drink would
 * go into the bar, or only once all of it would.
 */
public enum DrinkAt implements StringRepresentable {
    ANY("any"),
    HALF("half"),
    FULL("full");

    public static final Codec<DrinkAt> CODEC = StringRepresentable.fromEnum(DrinkAt::values);
    public static final StreamCodec<ByteBuf, DrinkAt> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ordinal -> values()[ordinal], DrinkAt::ordinal);

    private static final DrinkAt[] VALUES = values();

    private final String name;

    DrinkAt(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public DrinkAt next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    /** {@code missing} thirst points against a drink that restores {@code thirst}. */
    public boolean allows(int missing, int thirst) {
        return switch (this) {
            case ANY -> missing > 0;
            case HALF -> missing > 0 && thirst / 2 <= missing;
            case FULL -> missing > 0 && thirst <= missing;
        };
    }

    public static DrinkAt byName(String name) {
        for (DrinkAt value : VALUES) {
            if (value.name.equals(name)) return value;
        }
        return HALF;
    }
}
