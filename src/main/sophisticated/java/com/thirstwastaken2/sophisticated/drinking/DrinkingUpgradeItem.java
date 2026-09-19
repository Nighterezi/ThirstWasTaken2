package com.thirstwastaken2.sophisticated.drinking;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeGroup;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DrinkingUpgradeItem extends UpgradeItemBase<DrinkingUpgradeWrapper> {
    public static final UpgradeType<DrinkingUpgradeWrapper> TYPE = new UpgradeType<>(DrinkingUpgradeWrapper::new);

    /**
     * Sophisticated's per-storage limits are its own config, which this mod cannot add to. The Feeding
     * upgrade is unlimited there by default, so this one is unlimited too.
     */
    private static final IUpgradeCountLimitConfig UNLIMITED = new IUpgradeCountLimitConfig() {
        @Override
        public int getMaxUpgradesPerStorage(String storageType, @Nullable Identifier upgradeName) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup group) {
            return Integer.MAX_VALUE;
        }
    };

    private final int filterSlotCount;
    private final boolean advanced;

    /** {@code id} is what the item registers under, which from 1.21.2 it has to know before it is built. */
    DrinkingUpgradeItem(Identifier id, int filterSlotCount, boolean advanced) {
        //? if >=1.21.2 {
        super(UNLIMITED, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        //?} else
        /*super(UNLIMITED);*/
        this.filterSlotCount = filterSlotCount;
        this.advanced = advanced;
    }

    public int getFilterSlotCount() {
        return filterSlotCount;
    }

    /** Only the advanced upgrade lets the player change when to drink and what grade to accept. */
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public UpgradeType<DrinkingUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
