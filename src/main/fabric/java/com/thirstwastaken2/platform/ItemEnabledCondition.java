package com.thirstwastaken2.platform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.config.ThirstConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;

/**
 * {@code thirstwastaken2:item_enabled}: holds while the config has not switched off {@code item}, one of
 * the mod's own items. Datagen puts it on every recipe that makes one of them, so a pack that switches
 * an item off loses its recipes on the next data load. The NeoForge build translates it to its own
 * condition of the same id.
 *
 * <p>Here rather than beside the Fabric entrypoint because the parameter of {@code test} changed in
 * 1.21.2, and a fork belongs in {@code platform/}.
 */
public record ItemEnabledCondition(Identifier item) implements ResourceCondition {
    public static final MapCodec<ItemEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(ItemEnabledCondition::item)
    ).apply(instance, ItemEnabledCondition::new));
    public static final ResourceConditionType<ItemEnabledCondition> TYPE =
            ResourceConditionType.create(ThirstWasTaken2.id("item_enabled"), CODEC);

    static void register() {
        ResourceConditions.register(TYPE);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    //? if >=1.21.2 {
    @Override
    public boolean test(net.minecraft.resources.RegistryOps.RegistryInfoLookup registryInfo) {
        return ThirstConfig.get().isItemEnabled(item.toString());
    }
    //?} else {
    /*@Override
    public boolean test(net.minecraft.core.HolderLookup.Provider registryLookup) {
        return ThirstConfig.get().isItemEnabled(item.toString());
    }
    *///?}
}
