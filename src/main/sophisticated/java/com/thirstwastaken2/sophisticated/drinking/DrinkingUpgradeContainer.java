package com.thirstwastaken2.sophisticated.drinking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicContainer;

/** The settings tab's server half: the filter slots, and the two settings the advanced tab changes. */
public final class DrinkingUpgradeContainer extends UpgradeContainerBase<DrinkingUpgradeWrapper, DrinkingUpgradeContainer> {
    private static final String DATA_DRINK_AT = "drinkAt";
    private static final String DATA_MIN_PURITY = "minPurity";

    private final FilterLogicContainer<FilterLogic> filterLogicContainer = new FilterLogicContainer<>(
            supplyFromWrapper(DrinkingUpgradeWrapper::getFilterLogic), this, slots::add);

    public DrinkingUpgradeContainer(Player player, int containerId, DrinkingUpgradeWrapper wrapper,
                                    UpgradeContainerType<DrinkingUpgradeWrapper, DrinkingUpgradeContainer> type) {
        super(player, containerId, wrapper, type);
    }

    @Override
    public void handlePacket(CompoundTag data) {
        // From 1.21.5 a tag's getters answer with an Optional, or a fallback.
        if (data.contains(DATA_DRINK_AT)) {
            //? if >=1.21.5 {
            setDrinkAt(DrinkAt.byName(data.getStringOr(DATA_DRINK_AT, "")));
            //?} else
            /*setDrinkAt(DrinkAt.byName(data.getString(DATA_DRINK_AT)));*/
        } else if (data.contains(DATA_MIN_PURITY)) {
            //? if >=1.21.5 {
            setMinPurity(data.getIntOr(DATA_MIN_PURITY, DrinkingUpgradeWrapper.DEFAULT_MIN_PURITY));
            //?} else
            /*setMinPurity(data.getInt(DATA_MIN_PURITY));*/
        }
        filterLogicContainer.handlePacket(data);
    }

    public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public DrinkAt getDrinkAt() {
        return upgradeWrapper.getDrinkAt();
    }

    public void setDrinkAt(DrinkAt drinkAt) {
        upgradeWrapper.setDrinkAt(drinkAt);
        sendDataToServer(() -> {
            CompoundTag data = new CompoundTag();
            data.putString(DATA_DRINK_AT, drinkAt.getSerializedName());
            return data;
        });
    }

    public int getMinPurity() {
        return upgradeWrapper.getMinPurity();
    }

    public void setMinPurity(int minPurity) {
        upgradeWrapper.setMinPurity(minPurity);
        sendDataToServer(() -> {
            CompoundTag data = new CompoundTag();
            data.putInt(DATA_MIN_PURITY, minPurity);
            return data;
        });
    }
}
