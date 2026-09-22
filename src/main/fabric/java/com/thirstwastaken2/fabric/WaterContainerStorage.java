package com.thirstwastaken2.fabric;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterContainers;
import com.thirstwastaken2.platform.FabricTransfer;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.Iterator;
import java.util.List;

/**
 * The waterskin and the terracotta bowls as Fabric Transfer API fluid storage, so a Create Fly Spout
 * or Item Drain, and any other mod's pipe or tank, can fill and empty them. The same rules as NeoForge's
 * {@code WaterContainerFluids}:
 *
 * <ul>
 *     <li>A serving is 250 mB, 20250 droplets, NeoForge's size rather than Fabric's 27000-droplet bottle,
 *     so both loaders move the same water. Only whole servings move.</li>
 *     <li>A container that holds water only takes more of the same grade.</li>
 *     <li>Water with no grade fills an empty container as {@code defaultPurity}.</li>
 * </ul>
 *
 * <p>The grade travels on the fluid as one component, {@code water_purity} or {@code water_salty}, like
 * Create's fluid stacks. Any other component on the water is ignored.
 *
 * <p>A storage with one view rather than a {@code SingleSlotStorage}: Create Fly wraps a slotted item
 * storage with a capacity of zero, so its Spout would take every waterskin for full.
 */
public final class WaterContainerStorage implements Storage<FluidVariant>, StorageView<FluidVariant> {
    public static final long SERVING = FluidConstants.BUCKET / 4;
    private static final WaterQuality[] QUALITIES = {
            WaterQuality.fresh(0), WaterQuality.fresh(1), WaterQuality.fresh(2), WaterQuality.fresh(3),
            WaterQuality.SALT};
    private static DataComponentPatch[] patches;

    private final ContainerItemContext context;

    private WaterContainerStorage(ContainerItemContext context) {
        this.context = context;
    }

    /** Called once by {@link ThirstWasTaken2Fabric}, after the items are registered. */
    public static void register() {
        FluidStorage.ITEM.registerForItems((stack, context) -> new WaterContainerStorage(context),
                ThirstItems.WATERSKIN, ThirstItems.TERRACOTTA_BOWL, ThirstItems.TERRACOTTA_WATER_BOWL);
    }

    /** Water of {@code quality} as a variant. */
    public static FluidVariant water(WaterQuality quality) {
        return FluidVariant.of(Fluids.WATER, patch(quality));
    }

    /**
     * The grade {@code variant} carries, {@code defaultPurity} when none, or {@code null} for anything but
     * water. Only this mod's two components are read: other mods add their own to the water they hold,
     * as Create Fly's tanks add {@code create:fluid_max_capacity} after their first fill.
     */
    public static WaterQuality quality(FluidVariant variant) {
        if (!variant.isOf(Fluids.WATER)) return null;
        if (Boolean.TRUE.equals(FabricTransfer.component(variant, ThirstComponents.WATER_SALTY))) {
            return WaterQuality.SALT;
        }
        Integer purity = FabricTransfer.component(variant, ThirstComponents.WATER_PURITY);
        return WaterQuality.fresh(purity != null ? purity : ThirstConfig.get().defaultPurity);
    }

    private static DataComponentPatch patch(WaterQuality quality) {
        DataComponentPatch[] built = patches;
        if (built == null) {
            built = new DataComponentPatch[QUALITIES.length];
            for (int i = 0; i < QUALITIES.length; i++) {
                built[i] = QUALITIES[i] instanceof WaterQuality.Fresh fresh
                        ? DataComponentPatch.builder().set(ThirstComponents.WATER_PURITY, fresh.purity()).build()
                        : DataComponentPatch.builder().set(ThirstComponents.WATER_SALTY, true).build();
            }
            patches = built;
        }
        return built[quality instanceof WaterQuality.Fresh fresh ? fresh.purity() : QUALITIES.length - 1];
    }

    private ItemStack container() {
        return context.getItemVariant().toStack();
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack container = container();
        WaterQuality added = quality(resource);
        if (added == null || !WaterContainers.handles(container) || context.getAmount() == 0) return 0;
        int held = WaterContainers.servings(container);
        if (held > 0 && !added.equals(WaterPurity.quality(container))) return 0;
        int servings = (int) Math.min(maxAmount / SERVING, WaterContainers.capacity(container) - held);
        if (servings <= 0) return 0;
        ItemStack filled = WaterContainers.holding(container, added, held + servings);
        if (filled == null || context.exchange(ItemVariant.of(filled), 1, transaction) != 1) return 0;
        return servings * SERVING;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack container = container();
        int held = WaterContainers.servings(container);
        if (held == 0 || context.getAmount() == 0
                || !WaterPurity.quality(container).equals(quality(resource))) {
            return 0;
        }
        int servings = (int) Math.min(maxAmount / SERVING, held);
        if (servings <= 0) return 0;
        ItemStack emptied = WaterContainers.holding(container, WaterPurity.quality(container), held - servings);
        if (emptied == null || context.exchange(ItemVariant.of(emptied), 1, transaction) != 1) return 0;
        return servings * SERVING;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return List.<StorageView<FluidVariant>>of(this).iterator();
    }

    @Override
    public boolean isResourceBlank() {
        return WaterContainers.servings(container()) == 0;
    }

    @Override
    public FluidVariant getResource() {
        ItemStack container = container();
        return WaterContainers.servings(container) == 0 ? FluidVariant.blank() : water(WaterPurity.quality(container));
    }

    @Override
    public long getAmount() {
        return WaterContainers.servings(container()) * SERVING;
    }

    @Override
    public long getCapacity() {
        return WaterContainers.capacity(container()) * SERVING;
    }
}
