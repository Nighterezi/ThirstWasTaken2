package com.thirstwastaken2.culturaldelights;

import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.world.Container;

/**
 * Sea water in Cultural Delights' vat. The vat holds items, not fluid: a water bucket is an ingredient
 * like wheat, matched by the item tag {@code c:buckets/water}, and a bucket of sea water is still a
 * {@code minecraft:water_bucket}. So every water recipe takes it, and only the stack itself says it is
 * salty.
 *
 * <p>Takes the vat as a vanilla {@link Container}, which it is, so this class names nothing of the mod's.
 */
public final class VatWater {
    private VatWater() { }

    /**
     * Whether any slot holds a salty stack. Asked on every vat tick; one component read per slot, and
     * only water containers ever carry the component.
     */
    public static boolean holdsSeaWater(Container vat) {
        for (int slot = 0, size = vat.getContainerSize(); slot < size; slot++) {
            if (WaterPurity.isSalty(vat.getItem(slot))) return true;
        }
        return false;
    }
}
