package com.thirstwastaken2.gametest;

import com.thirstwastaken2.ThirstWasTaken2;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * The advancements are a datapack, and the compiler has no opinion about those. Nothing fails to
 * build when an id is misspelled, a parent names an advancement that was renamed, or a reward names
 * a recipe that does not exist: the game drops it and says nothing.
 *
 * <p>{@code ThirstAdvancements} awards its advancements by id, so an id that no longer resolves is
 * exactly the kind of silent nothing these tests exist to catch.
 */
public final class AdvancementGameTest {
    /** Every advancement on the mod's own tab, root first. */
    private static final List<String> TAB = List.of(
            "root", "first_drink", "dirty_water", "boil_water", "purified_water", "sea_water",
            "nether_drink");

    /** Every recipe-book unlock, named after the advancement rather than the recipe it rewards. */
    private static final List<String> RECIPE_UNLOCKS = List.of(
            "clay_bowl", "terracotta_bowl_from_smelting", "terracotta_water_bowl", "waterskin",
            "purify_water_bottle", "purify_water_bowl", "purify_water_bucket");

    @GameTest
    public void theModsAdvancementTabLoaded(GameTestHelper helper) {
        for (String name : TAB) {
            TestFixtures.check(helper, advancement(helper, ThirstWasTaken2.id(name)) != null,
                    "advancement " + name + " did not load");
        }
        helper.succeed();
    }

    /**
     * One tab means one root, and everything else hanging off it. A child whose parent is missing is
     * dropped along with everything under it.
     */
    @GameTest
    public void everyAdvancementHangsOffTheRoot(GameTestHelper helper) {
        for (String name : TAB) {
            AdvancementHolder holder = advancement(helper, ThirstWasTaken2.id(name));
            if (holder == null) continue;
            boolean shouldBeRoot = name.equals("root");
            TestFixtures.check(helper, holder.value().isRoot() == shouldBeRoot,
                    shouldBeRoot ? "the root advancement should have no parent"
                            : name + " should hang off another advancement of this mod");
        }
        helper.succeed();
    }

    /** The whole point of the recipe advancements: they hand out recipes the game actually has. */
    @GameTest
    public void recipeAdvancementsRewardRecipesThatExist(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        for (String name : RECIPE_UNLOCKS) {
            AdvancementHolder holder = advancement(helper, ThirstWasTaken2.id("recipes/misc/" + name));
            TestFixtures.check(helper, holder != null, "recipe advancement " + name + " did not load");
            if (holder == null) continue;

            List<ResourceKey<Recipe<?>>> rewards = holder.value().rewards().recipes();
            TestFixtures.check(helper, !rewards.isEmpty(), name + " should unlock at least one recipe");
            for (ResourceKey<Recipe<?>> recipe : rewards) {
                TestFixtures.check(helper, server.getRecipeManager().byKey(recipe).isPresent(),
                        "recipe " + recipe.identifier() + ", unlocked by " + name + ", does not exist");
            }
        }
        helper.succeed();
    }

    private static AdvancementHolder advancement(GameTestHelper helper, Identifier id) {
        return helper.getLevel().getServer().getAdvancements().get(id);
    }
}
