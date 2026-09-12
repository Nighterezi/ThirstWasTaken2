package com.thirstwastaken2.datagen;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterPurity;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.properties.numeric.CustomModelDataProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * The item models and the model definitions that select between them.
 *
 * <p>Two items dispatch on {@code minecraft:custom_model_data} rather than showing one sprite. The
 * waterskin reads index 0, how many servings are left, which {@code WaterskinItem.setServings}
 * writes; the filled bowl reads index 1, the water's grade, which {@code WaterPurity.setQuality} and
 * the purification recipes write. Both are floats mirrored from a real component, never a separate
 * source of truth.
 *
 * <p>{@code salt_water_bottle} and {@code salt_water_bucket} are handled by
 * {@link ThirstItemModelDefinitionProvider} instead: they are model ids for vanilla's own
 * containers, not items of the mod's own, so there is no item here to hang them on.
 */
public final class ThirstModelProvider extends FabricModelProvider {
    /** Custom model data index the waterskin dispatches on: servings remaining, 0 to 3. */
    private static final int WATERSKIN_INDEX = 0;

    /** Custom model data index the filled bowl dispatches on: water grade, 0 to 4 with salt last. */
    private static final int BOWL_INDEX = 1;

    /** The dispatch value sea water gets, one past the last fresh grade. */
    private static final int BOWL_SALT = WaterPurity.MAX + 1;

    public ThirstModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        // The mod adds no blocks. The cauldron's stored quality is a property on vanilla's block and
        // needs no model of its own, because the water level already has one.
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.generateFlatItem(ThirstItems.CLAY_BOWL, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ThirstItems.TERRACOTTA_BOWL, ModelTemplates.FLAT_ITEM);

        // The plain filled-bowl sprite. Nothing selects it, because every grade has one of its own,
        // but it is what a resource pack that does not know about grades falls back to.
        generators.createFlatItemModel(ThirstItems.TERRACOTTA_WATER_BOWL, ModelTemplates.FLAT_ITEM);

        generators.itemModelOutput.accept(ThirstItems.TERRACOTTA_WATER_BOWL,
                dispatch(generators, ThirstItems.TERRACOTTA_WATER_BOWL, BOWL_INDEX, bowlVariants()));
        generators.itemModelOutput.accept(ThirstItems.WATERSKIN,
                dispatch(generators, ThirstItems.WATERSKIN, WATERSKIN_INDEX, waterskinVariants()));

        // The sea-water bucket sprite. Its definition is written next door; only the model belongs
        // here, because it is an ordinary flat item model that happens to have no item.
        // 26.1 wrapped the texture in a Material, which 1.21.11 has no overload for.
        //? if >=26.1 {
        TextureMapping bucket = TextureMapping.layer0(
                new net.minecraft.client.resources.model.sprite.Material(
                        ThirstItemModelDefinitionProvider.SALT_WATER_BUCKET_MODEL));
        //?} else
        /*TextureMapping bucket = TextureMapping.layer0(ThirstItemModelDefinitionProvider.SALT_WATER_BUCKET_MODEL);*/

        ModelTemplates.FLAT_ITEM.create(
                ThirstItemModelDefinitionProvider.SALT_WATER_BUCKET_MODEL, bucket, generators.modelOutput);
    }

    /** The filled bowl: one sprite per fresh grade, then one for sea water. */
    private static List<String> bowlVariants() {
        List<String> suffixes = new ArrayList<>();
        for (int purity = WaterPurity.MIN; purity <= WaterPurity.MAX; purity++) {
            suffixes.add("_purity_" + purity);
        }
        suffixes.add("_salty");
        return List.copyOf(suffixes);
    }

    /** The waterskin: empty, then one sprite per serving it still holds. */
    private static List<String> waterskinVariants() {
        List<String> suffixes = new ArrayList<>();
        for (int servings = 0; servings <= 3; servings++) {
            suffixes.add("_" + servings);
        }
        return List.copyOf(suffixes);
    }

    /**
     * Generates one flat model per suffix and returns a range dispatch over them, with the first
     * suffix as the fallback and the rest reached at thresholds 1, 2, 3 and so on.
     *
     * <p>The index is {@code custom_model_data}'s, not the entry's: the thresholds are the values
     * the item actually carries, so a bowl of grade 2 selects the third sprite by holding a 2.
     */
    private static ItemModel.Unbaked dispatch(
            ItemModelGenerators generators, Item item, int index, List<String> suffixes) {
        ItemModel.Unbaked fallback = null;
        List<RangeSelectItemModel.Entry> entries = new ArrayList<>();

        for (int variant = 0; variant < suffixes.size(); variant++) {
            Identifier model = generators.createFlatItemModel(
                    item, suffixes.get(variant), ModelTemplates.FLAT_ITEM);
            if (variant == 0) {
                fallback = ItemModelUtils.plainModel(model);
            } else {
                entries.add(ItemModelUtils.override(ItemModelUtils.plainModel(model), variant));
            }
        }

        return ItemModelUtils.rangeSelect(new CustomModelDataProperty(index), fallback, entries);
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Models";
    }
}
