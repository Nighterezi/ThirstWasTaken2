package com.thirstwastaken.compat;

import com.thirstwastaken.purity.ThirstComponents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

/** Fabric replacement for Forge global loot modifiers and Piglin water trades. */
public final class LootIntegration {
    private static final Set<String> CHESTS = Set.of(
            "minecraft:chests/abandoned_mineshaft", "minecraft:chests/bastion_other",
            "minecraft:chests/nether_bridge", "minecraft:chests/shipwreck_supply",
            "minecraft:chests/simple_dungeon");

    private LootIntegration() { }

    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            String id = key.identifier().toString();
            if (source.isBuiltin() && CHESTS.contains(id)) table.withPool(waterPool(true));
            if (source.isBuiltin() && id.equals("minecraft:gameplay/piglin_bartering")) table.withPool(waterPool(false));
        });
    }

    private static LootPool.Builder waterPool(boolean chest) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        pool.add(water(2).setWeight(chest ? 10 : 2));
        pool.add(water(3).setWeight(chest ? 10 : 1));
        pool.add(EmptyLootItem.emptyItem().setWeight(chest ? 20 : 37));
        return pool;
    }

    private static net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder<?> water(int purity) {
        return LootItem.lootTableItem(Items.POTION)
                .apply(SetPotionFunction.setPotion(Potions.WATER))
                .apply(SetComponentsFunction.setComponent(ThirstComponents.WATER_PURITY, purity))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)));
    }
}
