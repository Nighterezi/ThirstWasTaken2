package com.thirstwastaken2.compat;

import com.thirstwastaken2.purity.ThirstComponents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

/**
 * Fabric replacement for the original Forge global loot modifiers, which seeded structure chests and
 * Piglin barters with water bottles of varying purity.
 */
public final class LootIntegration {
    private static final Set<ResourceKey<LootTable>> CHESTS = Set.of(
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.BASTION_OTHER,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.SHIPWRECK_SUPPLY,
            BuiltInLootTables.SIMPLE_DUNGEON);

    private LootIntegration() { }

    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin()) return;
            if (CHESTS.contains(key)) {
                table.withPool(waterPool(true));
            } else if (BuiltInLootTables.PIGLIN_BARTERING.equals(key)) {
                table.withPool(waterPool(false));
            }
        });
    }

    private static LootPool.Builder waterPool(boolean chest) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(water(2).setWeight(chest ? 10 : 2))
                .add(water(3).setWeight(chest ? 10 : 1))
                .add(EmptyLootItem.emptyItem().setWeight(chest ? 20 : 37));
    }

    private static LootPoolSingletonContainer.Builder<?> water(int purity) {
        return LootItem.lootTableItem(Items.POTION)
                .apply(SetPotionFunction.setPotion(Potions.WATER))
                .apply(SetComponentsFunction.setComponent(ThirstComponents.WATER_PURITY, purity))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)));
    }
}
