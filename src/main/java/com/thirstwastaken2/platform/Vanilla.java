package com.thirstwastaken2.platform;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Every vanilla call whose shape differs between the supported Minecraft versions.
 *
 * <p>Callers outside this package use the same signature on every version; the branches live here and
 * nowhere else. Keep each method a one-liner over vanilla — game logic belongs in its own package.
 */
public final class Vanilla {
    private static final FontDescription DROPLET_FONT =
            new FontDescription.Resource(ThirstWasTaken2.id("droplets"));

    private Vanilla() { }

    /** The registry id of a built-in or modded item. */
    public static Identifier itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    /**
     * Whether the position is Nether-like, i.e. water placed there boils away. Replaced
     * {@code DimensionType#ultraWarm} and moved to environment attributes in 26.1.
     */
    public static boolean waterEvaporates(Level level, BlockPos pos) {
        // getValue is generic and hands back a boxed Boolean; unboxing it directly would throw on a
        // dimension that does not define the attribute, on the exhaustion path of every tick.
        return Boolean.TRUE.equals(
                level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos));
    }

    /** Switches a style to the mod's droplet bitmap font. */
    public static Style dropletFont(Style style) {
        return style.withFont(DROPLET_FONT);
    }
}
