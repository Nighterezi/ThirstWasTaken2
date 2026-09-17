package com.thirstwastaken2.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;

/**
 * What the mod's blocks drop. The hanging pot drops itself; the water in it is lost, as a cauldron's is.
 *
 * <p>Built with vanilla's loot builders and written through vanilla's codec. Fabric's block loot
 * provider would say the same thing, but it was renamed and reshaped for 26.1, and one table is not
 * worth a branch per version.
 */
public final class ThirstBlockLootProvider implements DataProvider {
    private final PackOutput.PathProvider tables;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ThirstBlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.tables = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table");
        this.registries = registries;
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Block Loot Tables";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(lookup -> {
            DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            Identifier id = ThirstWasTaken2.id("blocks/copper_hanging_pot");
            LootTable table = LootTable.lootTable()
                    .setParamSet(LootContextParamSets.BLOCK)
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ThirstItems.COPPER_HANGING_POT))
                            .when(ExplosionCondition.survivesExplosion()))
                    .setRandomSequence(id)
                    .build();
            JsonElement json = LootTable.DIRECT_CODEC.encodeStart(ops, table).getOrThrow();
            return DataProvider.saveStable(cache, json, tables.json(id));
        });
    }
}
