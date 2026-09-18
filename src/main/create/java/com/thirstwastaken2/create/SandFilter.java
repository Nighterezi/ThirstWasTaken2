package com.thirstwastaken2.create;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The Sand Filter's block, item, block entity type and fluid capability. Only registered alongside Create. */
public final class SandFilter {
    private static final String NAME = "sand_filter";

    private static Block block;
    private static Item item;
    private static BlockEntityType<SandFilterBlockEntity> blockEntity;

    private SandFilter() { }

    public static Block block() {
        return block;
    }

    public static BlockEntityType<SandFilterBlockEntity> blockEntity() {
        return blockEntity;
    }

    static void register(IEventBus modBus) {
        modBus.addListener(SandFilter::onRegister);
        modBus.addListener(SandFilter::onCapabilities);
        modBus.addListener(SandFilter::onCreativeTab);
    }

    private static void onRegister(RegisterEvent event) {
        // NeoForge fires blocks first, then items and block entity types, so each one can use the last.
        event.register(Registries.BLOCK, helper -> {
            // Copper like the original, which took Create's copper metal properties.
            block = new SandFilterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .noOcclusion());
            helper.register(ThirstWasTaken2.id(NAME), block);
        });
        event.register(Registries.ITEM, helper -> {
            item = new BlockItem(block, new Item.Properties());
            helper.register(ThirstWasTaken2.id(NAME), item);
        });
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            blockEntity = BlockEntityType.Builder.of(SandFilterBlockEntity::new, block).build(null);
            helper.register(ThirstWasTaken2.id(NAME), blockEntity);
            ThirstWasTaken2.LOGGER.info("Create found, registered the Sand Filter");
        });
    }

    /** Pipes, pumps and hands find the two tanks through the block capability, by side. */
    private static void onCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, blockEntity, SandFilterBlockEntity::fluidHandler);
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ThirstItems.CREATIVE_TAB_KEY)) event.accept(item);
    }
}
