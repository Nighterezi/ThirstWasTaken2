package com.thirstwastaken2.datagen;

import com.thirstwastaken2.damage.ThirstDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import java.util.concurrent.CompletableFuture;

/**
 * Dehydration bypasses armour, like starvation. The entry is optional, because the tag it adds to is
 * vanilla's and the damage type it names is the mod's: a datapack that removes
 * {@code thirstwastaken2:dehydrate} must not break every other entry in the tag with it.
 */
public final class ThirstDamageTypeTagProvider extends FabricTagsProvider<DamageType> {
    public ThirstDamageTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.DAMAGE_TYPE, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(DamageTypeTags.BYPASSES_ARMOR).addOptional(ThirstDamageTypes.DEHYDRATE);
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Damage Type Tags";
    }
}
