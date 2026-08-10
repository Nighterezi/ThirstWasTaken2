package com.thirstwastaken.compat.createfly;

import com.thirstwastaken.ThirstWasTaken;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;

/** Optional Create Fly 26.2 counterpart of the original Create Sand Filter. */
public final class CreateFlyIntegration {
    public static final Block SAND_FILTER;
    public static final Item SAND_FILTER_ITEM;
    public static final BlockEntityType<SandFilterBlockEntity> SAND_FILTER_ENTITY;

    static {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ThirstWasTaken.id("sand_filter"));
        SAND_FILTER = Registry.register(BuiltInRegistries.BLOCK, blockKey,
                new SandFilterBlock(BlockBehaviour.Properties.of().strength(3.0F).requiresCorrectToolForDrops().setId(blockKey)));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ThirstWasTaken.id("sand_filter"));
        SAND_FILTER_ITEM = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(SAND_FILTER, new Item.Properties().setId(itemKey)));
        SAND_FILTER_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ThirstWasTaken.id("sand_filter"),
                new BlockEntityType<>(SandFilterBlockEntity::new, Set.of(SAND_FILTER)));
    }

    private CreateFlyIntegration() { }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(SAND_FILTER_ITEM));
        ThirstWasTaken.LOGGER.info("Enabled Create Fly Sand Filter integration");
    }
}
