package com.thirstwastaken2.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Writes every datapack and asset JSON the mod ships into
 * {@code src/main/generated/<minecraft version>}, which {@code build.gradle.kts} adds as a resource
 * root of {@code main}.
 *
 * <p>This mod is {@code thirstwastaken2-datagen}, but everything it writes belongs to
 * {@code thirstwastaken2}, which is what {@link #getEffectiveModId()} says.
 */
public final class ThirstDatagen implements DataGeneratorEntrypoint {
    @Override
    public String getEffectiveModId() {
        return "thirstwastaken2";
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ThirstRecipeProvider::new);
        pack.addProvider(ThirstAdvancementProvider::new);
        pack.addProvider(ThirstDamageTypeProvider::new);
        pack.addProvider(ThirstDamageTypeTagProvider::new);
        pack.addProvider(ThirstBiomeTagProvider::new);
        pack.addProvider(ThirstModelProvider::new);
        pack.addProvider(ThirstItemModelDefinitionProvider::new);
    }
}
