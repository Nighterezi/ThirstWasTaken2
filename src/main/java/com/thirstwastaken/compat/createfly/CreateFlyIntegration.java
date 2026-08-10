package com.thirstwastaken.compat.createfly;

import com.thirstwastaken.ThirstWasTaken;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.loader.api.FabricLoader;
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

/**
 * Optional Create Fly 26.2 counterpart of the original Create Sand Filter.
 *
 * <p>Nothing in this package may be loaded unless {@link #isAvailable()} returned true - the block
 * entity extends a Create class and would otherwise fail to link.
 */
public final class CreateFlyIntegration {
    /** A class that only exists in the Create Fly distribution we compile against. */
    private static final String MARKER_CLASS = "com.zurrtum.create.foundation.blockEntity.SmartBlockEntity";

    private static Block sandFilter;
    private static Item sandFilterItem;
    private static BlockEntityType<SandFilterBlockEntity> sandFilterEntity;

    private CreateFlyIntegration() { }

    /**
     * A mod may claim the {@code create} id without being the Create Fly port we bind to, so the
     * marker class is probed as well instead of trusting the id alone.
     */
    public static boolean isAvailable() {
        if (!FabricLoader.getInstance().isModLoaded("create")) return false;
        try {
            Class.forName(MARKER_CLASS, false, CreateFlyIntegration.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError error) {
            ThirstWasTaken.LOGGER.warn("A mod with id 'create' is installed but is not Create Fly; "
                    + "skipping the Sand Filter integration");
            return false;
        }
    }

    public static Block sandFilter() { return sandFilter; }

    public static BlockEntityType<SandFilterBlockEntity> sandFilterEntity() { return sandFilterEntity; }

    public static void register() {
        if (sandFilter != null) return;

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ThirstWasTaken.id("sand_filter"));
        sandFilter = Registry.register(BuiltInRegistries.BLOCK, blockKey, new SandFilterBlock(
                BlockBehaviour.Properties.of().strength(3.0F).requiresCorrectToolForDrops().setId(blockKey)));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ThirstWasTaken.id("sand_filter"));
        sandFilterItem = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(sandFilter, new Item.Properties().setId(itemKey)));

        sandFilterEntity = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ThirstWasTaken.id("sand_filter"),
                new BlockEntityType<>(SandFilterBlockEntity::new, Set.of(sandFilter)));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(sandFilterItem));
        ThirstWasTaken.LOGGER.info("Enabled Create Fly Sand Filter integration");
    }
}
