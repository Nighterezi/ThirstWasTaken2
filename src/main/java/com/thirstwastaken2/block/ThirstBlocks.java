package com.thirstwastaken2.block;

import com.thirstwastaken2.platform.Vanilla;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ThirstBlocks {
    /**
     * Breaks quickly by hand, so it needs no tool tag. A piston knocks
     * it loose rather than pushing a pot of water around.
     */
    public static final HangingPotBlock COPPER_HANGING_POT = Vanilla.registerBlock("copper_hanging_pot",
            HangingPotBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY));
    /** The same pot in dark cast iron. It behaves exactly like the copper one. */
    public static final HangingPotBlock IRON_HANGING_POT = Vanilla.registerBlock("iron_hanging_pot",
            HangingPotBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    private ThirstBlocks() { }

    /**
     * Builds and registers the blocks, in this class's static initializer, the same way
     * {@code ThirstItems.register} does. Runs before the items, which place them.
     */
    public static void register() { }
}
