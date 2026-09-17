package com.thirstwastaken2.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.block.HangingPotBlock;
import com.thirstwastaken2.block.ThirstBlocks;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The copper hanging pot's blockstate and the models it assembles.
 *
 * <p>The pot is a multipart of three pieces. The pot itself, {@code models/block/copper_hanging_pot},
 * and the frame template it hangs from are Blockbench models and stay hand-written in
 * {@code src/main/resources}, as does the item's template, which is both of them in one model with its
 * own display transforms. What is written here is what differs by version or is repetitive: the frame
 * and the item, which name vanilla's chain texture, renamed in 1.21.9, and one flat water surface per
 * fill level and water quality, each with its own texture from {@code tools/generate_pot_water.py}.
 *
 * <p>Everything is assembled as JSON, the one shape that has not changed. The blockstate's builder
 * classes changed twice across the supported versions, so it is parsed into whatever each version's
 * model output takes instead.
 */
final class HangingPotModels {
    private static final String NAME = "copper_hanging_pot";
    private static final Identifier POT = model(NAME);
    private static final Identifier FRAME = model(NAME + "_frame");
    private static final Identifier FRAME_TEMPLATE = model("template_" + NAME + "_frame");
    private static final Identifier ITEM = ThirstWasTaken2.id("item/" + NAME);
    private static final Identifier ITEM_TEMPLATE = model("template_" + NAME + "_item");
    //? if >=1.21.9 {
    private static final String CHAIN_TEXTURE = "minecraft:block/iron_chain";
    //?} else
    /*private static final String CHAIN_TEXTURE = "minecraft:block/chain";*/

    private HangingPotModels() { }

    static void generate(BlockModelGenerators generators) {
        JsonObject frame = withChain(FRAME_TEMPLATE);
        generators.modelOutput.accept(FRAME, () -> frame);

        JsonArray parts = new JsonArray();
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.Z, Direction.Axis.X }) {
            // The models are drawn with the crossbar along Z.
            int rotation = axis == Direction.Axis.X ? 90 : 0;
            JsonObject hanging = when(HangingPotBlock.AXIS.getName(), axis.getSerializedName());
            hanging.addProperty(HangingPotBlock.HANGING.getName(), "true");
            parts.add(part(hanging, FRAME, rotation));
            parts.add(part(when(HangingPotBlock.AXIS.getName(), axis.getSerializedName()), POT, rotation));
        }

        for (int level = 1; level <= HangingPotBlock.CAPACITY; level++) {
            for (Map.Entry<String, String> water : waters().entrySet()) {
                Identifier surface = model(NAME + "_water_" + level + "_" + water.getValue());
                JsonElement json = surface(level, water.getValue());
                generators.modelOutput.accept(surface, () -> json);

                JsonObject condition = when(HangingPotBlock.LEVEL.getName(), Integer.toString(level));
                condition.addProperty(WaterPurity.BLOCK_PURITY.getName(), water.getKey());
                parts.add(part(condition, surface, 0));
            }
        }

        JsonObject blockState = new JsonObject();
        blockState.add("multipart", parts);
        generators.blockStateOutput.accept(blockState(blockState));
    }

    /**
     * The item shows the pot on its frame in 3D, the way a chest or a crafting table does, rather than a
     * flat sprite. Before 1.21.4 the item's own model is what the game reads; from 1.21.4 a definition
     * points at it.
     */
    static void item(net.minecraft.client.data.models.ItemModelGenerators generators) {
        JsonObject item = withChain(ITEM_TEMPLATE);
        //? if >=1.21.4 {
        generators.modelOutput.accept(ITEM, () -> item);
        generators.itemModelOutput.accept(com.thirstwastaken2.item.ThirstItems.COPPER_HANGING_POT,
                net.minecraft.client.data.models.model.ItemModelUtils.plainModel(ITEM));
        //?} else
        /*generators.output.accept(ITEM, () -> item);*/
    }

    /** A model that fills {@code template}'s chain slot with this version's chain texture. */
    private static JsonObject withChain(Identifier template) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", template.toString());
        JsonObject textures = new JsonObject();
        textures.addProperty("chain", CHAIN_TEXTURE);
        json.add("textures", textures);
        // NeoForge before 26.1 reads the render type off the model; everything else ignores the key.
        json.addProperty("render_type", "minecraft:cutout");
        return json;
    }

    // The blockstate generator became a pair of block and parsed definition in 1.21.5, and the
    // definition class was replaced in 26.1. Before 1.21.5 it hands over the JSON itself.
    //? if >=26.1 {
    private static net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator blockState(JsonObject json) {
        var definition = net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher.CODEC
                .parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        return new net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator() {
            @Override
            public net.minecraft.world.level.block.Block block() {
                return ThirstBlocks.COPPER_HANGING_POT;
            }

            @Override
            public net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher create() {
                return definition;
            }
        };
    }
    //?}
    //? if >=1.21.5 <26.1 {
    /*private static net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator blockState(JsonObject json) {
        var definition = net.minecraft.client.renderer.block.model.BlockModelDefinition.CODEC
                .parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        return new net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator() {
            @Override
            public net.minecraft.world.level.block.Block block() {
                return ThirstBlocks.COPPER_HANGING_POT;
            }

            @Override
            public net.minecraft.client.renderer.block.model.BlockModelDefinition create() {
                return definition;
            }
        };
    }
    *///?}
    //? if <1.21.5 {
    /*private static net.minecraft.client.data.models.blockstates.BlockStateGenerator blockState(JsonObject json) {
        return new net.minecraft.client.data.models.blockstates.BlockStateGenerator() {
            @Override
            public net.minecraft.world.level.block.Block getBlock() {
                return ThirstBlocks.COPPER_HANGING_POT;
            }

            @Override
            public JsonElement get() {
                return json;
            }
        };
    }
    *///?}

    /**
     * The stored-quality values each water texture is drawn for, and the texture's suffix, the same
     * suffixes the filled bowl uses. A pot whose quality was never set holds the default grade, which
     * the client cannot read from the server's config, so it shows clean water, the default default.
     */
    private static Map<String, String> waters() {
        Map<String, String> waters = new LinkedHashMap<>();
        for (int purity = WaterPurity.MIN; purity <= WaterPurity.MAX; purity++) {
            int stored = purity + 1;
            String values = purity == 2 ? WaterPurity.BLOCK_UNSET + "|" + stored : Integer.toString(stored);
            waters.put(values, "purity_" + purity);
        }
        waters.put(Integer.toString(WaterPurity.BLOCK_SALT), "salty");
        return waters;
    }

    /** One water surface filling the inside of the pot, rising half a pixel a serving from its floor. */
    private static JsonElement surface(int level, String water) {
        double height = 1.5 + (level - 1) * 0.5;
        JsonObject json = new JsonObject();
        json.addProperty("parent", "minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("water", ThirstWasTaken2.id("block/" + NAME + "_water_" + water).toString());
        textures.addProperty("particle", "#water");
        json.add("textures", textures);

        JsonObject face = new JsonObject();
        face.add("uv", numbers(4, 4, 12, 12));
        face.addProperty("texture", "#water");
        JsonObject faces = new JsonObject();
        faces.add("up", face);
        JsonObject element = new JsonObject();
        element.add("from", numbers(4, height, 4));
        element.add("to", numbers(12, height, 12));
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        json.add("elements", elements);
        return json;
    }

    private static JsonObject part(JsonObject when, Identifier model, int rotation) {
        JsonObject apply = new JsonObject();
        apply.addProperty("model", model.toString());
        if (rotation != 0) apply.addProperty("y", rotation);
        JsonObject part = new JsonObject();
        part.add("when", when);
        part.add("apply", apply);
        return part;
    }

    private static JsonObject when(String property, String value) {
        JsonObject when = new JsonObject();
        when.addProperty(property, value);
        return when;
    }

    private static JsonArray numbers(double... values) {
        JsonArray array = new JsonArray();
        for (double value : values) {
            if (value == Math.rint(value)) array.add((int) value);
            else array.add(value);
        }
        return array;
    }

    private static Identifier model(String name) {
        return ThirstWasTaken2.id("block/" + name);
    }
}
