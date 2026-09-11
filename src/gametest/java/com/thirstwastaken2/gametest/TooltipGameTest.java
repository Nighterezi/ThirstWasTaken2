package com.thirstwastaken2.gametest;

import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import com.thirstwastaken2.tooltip.ThirstTooltip;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The lines the mod contributes to an item tooltip.
 *
 * <p>Assertions look at translation keys rather than rendered text, because a dedicated server never
 * loads the mod's language files and would report every key as itself.
 */
public final class TooltipGameTest {
    @GameTest
    public void waterContainerShowsPurity(GameTestHelper helper) {
        List<Component> lines = linesFor(bowl(WaterQuality.fromPurity(3, false)));

        TestFixtures.check(helper, hasKeyStartingWith(lines, "thirst.purity."),
                "a water container should get a purity line, got " + keys(lines));
        helper.succeed();
    }

    @GameTest
    public void saltWaterShowsSalinity(GameTestHelper helper) {
        List<Component> salty = linesFor(bowl(new WaterQuality(25, true)));
        List<Component> fresh = linesFor(bowl(new WaterQuality(25, false)));

        TestFixtures.check(helper, hasKey(salty, "thirst.water.salty"),
                "salt water should get a salinity line, got " + keys(salty));
        TestFixtures.check(helper, !hasKey(fresh, "thirst.water.salty"),
                "fresh water should not, got " + keys(fresh));
        helper.succeed();
    }

    @GameTest
    public void waterskinShowsItsServings(GameTestHelper helper) {
        ItemStack empty = new ItemStack(ThirstItems.WATERSKIN);
        ItemStack filled = new ItemStack(ThirstItems.WATERSKIN);
        WaterskinItem.addWater(filled, new WaterQuality(10, false), 2);

        TestFixtures.check(helper,
                hasKey(linesFor(empty), "tooltip.thirstwastaken2.waterskin.empty"),
                "an empty waterskin should say so");
        TestFixtures.check(helper,
                hasKey(linesFor(filled), "tooltip.thirstwastaken2.waterskin.servings"),
                "a filled waterskin should report its servings");
        helper.succeed();
    }

    @GameTest
    public void hydrationRowsAreRendered(GameTestHelper helper) {
        // The bowl restores 4 thirst and 5 quenched in the default config, so both rows appear.
        List<Component> lines = linesFor(bowl(WaterQuality.fromPurity(3, false)));

        long droplets = lines.stream().filter(line -> !(line.getContents() instanceof TranslatableContents)).count();
        TestFixtures.check(helper, droplets >= 2,
                "a drink should get a thirst row and a quenched row, got " + keys(lines));
        helper.succeed();
    }

    @GameTest
    public void dropletRowsRoundHalvesUp(GameTestHelper helper) {
        // Two units per droplet, and a leftover half unit still gets a droplet of its own.
        TestFixtures.check(helper, ThirstTooltip.thirst(0) == null,
                "an item that restores nothing should get no row");
        TestFixtures.check(helper, length(ThirstTooltip.thirst(4)) == 2,
                "4 units is 2 droplets, got " + length(ThirstTooltip.thirst(4)));
        TestFixtures.check(helper, length(ThirstTooltip.thirst(5)) == 3,
                "5 units rounds up to 3 droplets, got " + length(ThirstTooltip.thirst(5)));
        TestFixtures.check(helper, length(ThirstTooltip.quenched(40)) == 10,
                "the row is capped at 10 droplets, got " + length(ThirstTooltip.quenched(40)));
        helper.succeed();
    }

    @GameTest
    public void cachedLinesAreHandedOutAsCopies(GameTestHelper helper) {
        // Lines are built once and copied out. Restyling a returned line in place, as other mods are
        // free to do, must not leak into the next tooltip.
        ((MutableComponent) WaterPurity.tooltip(3)).withStyle(ChatFormatting.OBFUSCATED);
        ((MutableComponent) ThirstTooltip.thirst(4)).withStyle(ChatFormatting.OBFUSCATED);

        TestFixtures.check(helper, !WaterPurity.tooltip(3).getStyle().isObfuscated(),
                "restyling one purity line should not change the next one");
        TestFixtures.check(helper, !ThirstTooltip.thirst(4).getStyle().isObfuscated(),
                "restyling one droplet row should not change the next one");
        helper.succeed();
    }

    @GameTest
    public void plainItemsGetNoLines(GameTestHelper helper) {
        List<Component> lines = linesFor(new ItemStack(net.minecraft.world.item.Items.STONE));

        TestFixtures.check(helper, lines.isEmpty(),
                "a stone block should get no thirst lines, got " + keys(lines));
        helper.succeed();
    }

    private static List<Component> linesFor(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        ThirstTooltip.appendTo(stack, lines::add);
        return lines;
    }

    private static ItemStack bowl(WaterQuality quality) {
        return WaterPurity.setQuality(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
    }

    private static int length(Component row) {
        return row == null ? 0 : row.getString().length();
    }

    private static boolean hasKey(List<Component> lines, String key) {
        return lines.stream().anyMatch(line -> keyOf(line).equals(key));
    }

    private static boolean hasKeyStartingWith(List<Component> lines, String prefix) {
        return lines.stream().anyMatch(line -> keyOf(line).startsWith(prefix));
    }

    private static String keyOf(Component line) {
        return line.getContents() instanceof TranslatableContents translatable
                ? translatable.getKey()
                : "";
    }

    private static String keys(List<Component> lines) {
        return lines.stream().map(line -> keyOf(line).isEmpty() ? "<droplets>" : keyOf(line)).toList().toString();
    }
}
