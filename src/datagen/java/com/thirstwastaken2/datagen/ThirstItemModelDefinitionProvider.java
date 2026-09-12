package com.thirstwastaken2.datagen;

import com.thirstwastaken2.ThirstWasTaken2;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The two item model definitions that belong to no item of the mod's own.
 *
 * <p>Sea water is carried in vanilla's water bottle and water bucket, which cannot be given a model
 * of ours at registration, so {@code WaterPurity.setQuality} points their
 * {@code minecraft:item_model} component at these ids instead and clears it when the water is not
 * salty. {@link ThirstModelProvider} cannot write them, because everything it writes is keyed by an
 * {@link net.minecraft.world.item.Item} and these have none, so they are written here straight into
 * {@code assets/thirstwastaken2/items/}.
 *
 * <p>The bottle ships no texture on purpose. It reuses vanilla's own {@code minecraft:item/potion}
 * model and replaces only the tint, so a resource pack that reshapes potions reshapes this too. The
 * bucket has no tinted overlay layer to borrow, so it gets a flat model over a one-off recolour of
 * the water bucket sprite; that model is written by {@link ThirstModelProvider}, which is where
 * every other flat item model lives.
 */
public final class ThirstItemModelDefinitionProvider implements DataProvider {
    /** Sea colour, ARGB. The only place it is written; nothing on the Java side knows it. */
    private static final int SEA_TINT = 0xFF33BCB8;

    /** Vanilla's potion model, borrowed whole so resource packs keep control of the bottle's shape. */
    private static final Identifier POTION_MODEL = Identifier.withDefaultNamespace("item/potion");

    /** The flat model {@link ThirstModelProvider} writes for the sea-water bucket, and its texture. */
    public static final Identifier SALT_WATER_BUCKET_MODEL = ThirstWasTaken2.id("item/salt_water_bucket");

    private final PackOutput.PathProvider definitions;

    public ThirstItemModelDefinitionProvider(FabricPackOutput output) {
        this.definitions = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        Map<Identifier, ClientItem> items = new LinkedHashMap<>();
        items.put(ThirstWasTaken2.id("salt_water_bottle"),
                definition(ItemModelUtils.tintedModel(POTION_MODEL, ItemModelUtils.constantTint(SEA_TINT))));
        items.put(ThirstWasTaken2.id("salt_water_bucket"),
                definition(ItemModelUtils.plainModel(SALT_WATER_BUCKET_MODEL)));

        return DataProvider.saveAll(writer, ClientItem.CODEC, definitions, items);
    }

    private static ClientItem definition(ItemModel.Unbaked model) {
        return new ClientItem(model, ClientItem.Properties.DEFAULT);
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Item Model Definitions";
    }
}
