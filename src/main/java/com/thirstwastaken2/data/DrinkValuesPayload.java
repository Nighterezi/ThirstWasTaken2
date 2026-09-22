package com.thirstwastaken2.data;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Every data pack thirst value the server loaded, sent to a client on join and after {@code /reload}
 * so its tooltips agree with what drinking restores. See {@link DataPackDrinks}.
 *
 * <p>Items travel as registry ids, so the packet is a few bytes per entry. A client without the mod
 * never receives it: each loader checks that the channel was negotiated before sending.
 */
public record DrinkValuesPayload(Map<Item, int[]> values) implements CustomPacketPayload {
    public static final Type<DrinkValuesPayload> TYPE = new Type<>(ThirstWasTaken2.id("drink_values"));

    private static final StreamCodec<RegistryFriendlyByteBuf, int[]> AMOUNTS = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, amounts -> amounts[0],
            ByteBufCodecs.VAR_INT, amounts -> amounts[1],
            (thirst, quenched) -> new int[]{thirst, quenched});

    public static final StreamCodec<RegistryFriendlyByteBuf, DrinkValuesPayload> STREAM_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, Item, int[], Map<Item, int[]>>map(
                            IdentityHashMap::new, ByteBufCodecs.registry(Registries.ITEM), AMOUNTS)
                    .map(DrinkValuesPayload::new, DrinkValuesPayload::values);

    @Override
    public Type<DrinkValuesPayload> type() {
        return TYPE;
    }
}
