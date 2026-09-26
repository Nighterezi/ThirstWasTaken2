package com.thirstwastaken2.neoforge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * {@code thirstwastaken2:item_enabled}: holds while the config has not switched off {@code item}, one of
 * the mod's own items. Datagen writes it as a Fabric condition, and this node's build rewrites it to
 * this one (see {@code neoForgeConditions} in {@code build.neoforge.gradle.kts}).
 */
public record ItemEnabledCondition(Identifier item) implements ICondition {
    public static final MapCodec<ItemEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(ItemEnabledCondition::item)
    ).apply(instance, ItemEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return ThirstConfig.get().isItemEnabled(item.toString());
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
