package com.thirstwastaken2.gametest;

import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/** Storing, mixing and emptying the three servings a waterskin holds. */
public final class WaterskinGameTest {
    private static final int CLEAN = 10;
    private static final int DIRTY = 50;

    @GameTest
    public void mixingLandsBetweenTheTwoSources(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);

        WaterskinItem.addWater(skin, new WaterQuality(CLEAN, false), 1);
        TestFixtures.check(helper, WaterskinItem.servings(skin) == 1,
                "one serving added, got " + WaterskinItem.servings(skin));
        WaterskinItem.addWater(skin, new WaterQuality(DIRTY, false), 1);
        TestFixtures.check(helper, WaterskinItem.servings(skin) == 2,
                "two servings stored, got " + WaterskinItem.servings(skin));

        int mixed = WaterPurity.quality(skin).contamination();
        TestFixtures.check(helper, mixed > CLEAN && mixed < DIRTY,
                "mixing should land between " + CLEAN + " and " + DIRTY + ", got " + mixed);
        helper.succeed();
    }

    @GameTest
    public void oneSaltyServingMakesTheWholeSkinSalty(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);

        WaterskinItem.addWater(skin, new WaterQuality(CLEAN, false), 1);
        WaterskinItem.addWater(skin, new WaterQuality(CLEAN, true), 1);

        TestFixtures.check(helper, WaterPurity.isSalty(skin),
                "salinity must not be diluted away by mixing");
        helper.succeed();
    }

    @GameTest
    public void fillingStopsAtCapacity(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);

        boolean filled = WaterskinItem.addWater(skin, new WaterQuality(CLEAN, false), 99);
        TestFixtures.check(helper, filled, "filling an empty waterskin should succeed");
        TestFixtures.check(helper, WaterskinItem.servings(skin) == WaterskinItem.CAPACITY,
                "should hold exactly " + WaterskinItem.CAPACITY + ", got " + WaterskinItem.servings(skin));

        boolean again = WaterskinItem.addWater(skin, new WaterQuality(CLEAN, false), 1);
        TestFixtures.check(helper, !again, "a full waterskin should refuse more water");
        helper.succeed();
    }

    @GameTest
    public void emptyingClearsTheStoredQuality(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);
        WaterskinItem.addWater(skin, new WaterQuality(DIRTY, true), WaterskinItem.CAPACITY);

        WaterskinItem.removeWater(skin, WaterskinItem.CAPACITY);

        TestFixtures.check(helper, WaterskinItem.servings(skin) == 0,
                "the waterskin should be empty, got " + WaterskinItem.servings(skin));
        TestFixtures.check(helper, !skin.has(ThirstComponents.WATER_CONTAMINATION),
                "an empty waterskin must not remember the water it held");
        TestFixtures.check(helper, !WaterPurity.isSalty(skin),
                "an empty waterskin must not stay salty");
        helper.succeed();
    }

    /**
     * The waterskin only has a reason to exist while a filled bowl holds one drink. A stackable
     * filled bowl would put dozens of drinks in a single slot and make the waterskin pointless, so
     * this locks the balance in rather than leaving it to a code review.
     */
    @GameTest
    public void aFilledBowlDoesNotOutclassTheWaterskin(GameTestHelper helper) {
        TestFixtures.check(helper, !new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL).isStackable(),
                "a filled water bowl must not stack");
        TestFixtures.check(helper, !new ItemStack(ThirstItems.WATERSKIN).isStackable(),
                "the waterskin must not stack either");
        TestFixtures.check(helper, new ItemStack(ThirstItems.TERRACOTTA_BOWL).isStackable(),
                "the empty bowl is just a container and should still stack");
        helper.succeed();
    }

    @GameTest
    public void anEmptyWaterskinIsNotAWaterContainer(GameTestHelper helper) {
        ItemStack skin = new ItemStack(ThirstItems.WATERSKIN);

        TestFixtures.check(helper, !WaterPurity.isWaterContainer(skin),
                "an empty waterskin holds no water");
        WaterskinItem.addWater(skin, new WaterQuality(CLEAN, false), 1);
        TestFixtures.check(helper, WaterPurity.isWaterContainer(skin),
                "a filled waterskin holds water");
        helper.succeed();
    }
}
