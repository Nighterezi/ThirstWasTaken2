package com.thirstwastaken2.createfly;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** The Sand Filter's block, item and block entity type. Only registered alongside Create Fly. */
public final class SandFilter {
    private static final String NAME = "sand_filter";

    private static Block block;
    private static BlockEntityType<SandFilterBlockEntity> blockEntity;

    private SandFilter() { }

    public static Block block() {
        return block;
    }

    public static BlockEntityType<SandFilterBlockEntity> blockEntity() {
        return blockEntity;
    }

    static void register() {
        if (block != null) return;

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ThirstWasTaken2.id(NAME));
        // Copper like the original, which took Create's copper metal properties.
        block = Registry.register(BuiltInRegistries.BLOCK, blockKey, new SandFilterBlock(
                BlockBehaviour.Properties.of()
                        .setId(blockKey)
                        .mapColor(MapColor.COLOR_ORANGE)
                        .strength(3.0F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.COPPER)
                        .noOcclusion()));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ThirstWasTaken2.id(NAME));
        Item item = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));

        // Fabric's builder, because vanilla's constructor is private before 26.2. Create Fly widens it for
        // itself, but that widening is not on the classpath the integration compiles against.
        blockEntity = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ThirstWasTaken2.id(NAME),
                FabricBlockEntityTypeBuilder.create(SandFilterBlockEntity::new, block).build());

        CreativeModeTabEvents.modifyOutputEvent(ThirstItems.CREATIVE_TAB_KEY).register(entries -> entries.accept(item));
        ThirstWasTaken2.LOGGER.info("Create Fly found, registered the Sand Filter");
    }
}
