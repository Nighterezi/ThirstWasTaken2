package com.thirstwastaken2.datagen;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.ThirstComponents;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
//? if >=26.1 {
import net.minecraft.world.item.ItemStackTemplate;
//?} else
/*import net.minecraft.world.item.ItemStack;*/
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Every recipe the mod ships, and the recipe book unlocks that go with them.
 *
 * <p>Eighteen of the twenty-two are purification recipes, and they are one shape rather than
 * eighteen decisions: for each container, each input grade below the cap and each heat source,
 * boiling bumps the water two grades and stops at {@link #PURIFIED}. Reading them out of
 * {@link #PURIFY_TABLE} is the point of generating them — the eighteen JSON files they replace had
 * to be kept consistent by hand.
 *
 * <p>Salt water carries no {@code water_purity} at all, so an ingredient that demands one already
 * excludes it. Demanding {@code water_salty: false} as well is belt and braces, and it is also what
 * forces anything that hands out water to stamp both components, loot included.
 */
public final class ThirstRecipeProvider extends FabricRecipeProvider {
    /** The grade boiling cannot improve on, so the grade with no recipe of its own. */
    private static final int PURIFIED = 3;

    /** Input grade to output grade: a two grade bump, capped. Index is the input grade. */
    private static final int[] PURIFY_TABLE = { 2, 3, 3 };

    private static final float PURIFY_EXPERIENCE = 0.35F;
    private static final int SMELTING_TIME = 200;
    private static final int CAMPFIRE_TIME = 600;

    public ThirstRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new Recipes(registries, output);
    }

    /** One purifiable container: what holds the water, and what the recipes call it. */
    private record Container(String name, Item item, boolean potion, boolean bowl) {
        static final Container BOTTLE = new Container("bottle", Items.POTION, true, false);
        static final Container BOWL = new Container("bowl", ThirstItems.TERRACOTTA_WATER_BOWL, false, true);
        static final Container BUCKET = new Container("bucket", Items.WATER_BUCKET, false, false);

        static final List<Container> ALL = List.of(BOTTLE, BOWL, BUCKET);
    }

    private static final class Recipes extends RecipeProvider {
        private final HolderGetter<Item> items;

        private Recipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
            this.items = registries.lookupOrThrow(Registries.ITEM);
        }

        @Override
        public void buildRecipes() {
            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ThirstItems.CLAY_BOWL, 4)
                    .pattern("C C")
                    .pattern(" C ")
                    .define('C', Items.CLAY_BALL)
                    .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                    .save(output, recipe("clay_bowl"));

            //? if >=26.1 {
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(ThirstItems.CLAY_BOWL), RecipeCategory.MISC,
                            CookingBookCategory.MISC, ThirstItems.TERRACOTTA_BOWL, 0.1F, SMELTING_TIME)
            //?} else {
            /*SimpleCookingRecipeBuilder.smelting(Ingredient.of(ThirstItems.CLAY_BOWL), RecipeCategory.MISC,
                            ThirstItems.TERRACOTTA_BOWL, 0.1F, SMELTING_TIME)
            *///?}
                    .unlockedBy("has_clay_bowl", has(ThirstItems.CLAY_BOWL))
                    .save(output, recipe("terracotta_bowl_from_smelting"));

            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ThirstItems.WATERSKIN)
                    .pattern(" S ")
                    .pattern("L L")
                    .pattern(" L ")
                    .define('S', Items.STRING)
                    .define('L', Items.LEATHER)
                    .unlockedBy("has_leather", has(Items.LEATHER))
                    .save(output, recipe("waterskin"));

            // A bucket of fresh water poured into a fired bowl. The result is graded 2 rather than
            // sampled, because the bucket's own grade is gone by the time a recipe sees it.
            ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, bowlResult(2))
                    .requires(ThirstItems.TERRACOTTA_BOWL)
                    .requires(DefaultCustomIngredients.components(
                            Ingredient.of(Items.WATER_BUCKET),
                            DataComponentPatch.builder()
                                    .set(ThirstComponents.WATER_SALTY, false)
                                    .build()))
                    .unlockedBy("has_terracotta_bowl", has(ThirstItems.TERRACOTTA_BOWL))
                    .save(output, recipe("terracotta_water_bowl"));

            Container.ALL.forEach(this::purifyRecipes);
        }

        /**
         * The six purification recipes for one container, plus the single recipe book unlock that
         * covers all six. Writing one unlock per recipe would put six identical entries in the
         * recipe book.
         */
        private void purifyRecipes(Container container) {
            AdvancementHolder unlock = purifyUnlock(container);

            boolean first = true;
            for (int purity = 0; purity < PURIFIED; purity++) {
                Ingredient ingredient = purifyIngredient(container, purity);
                var result = purifyResult(container, PURIFY_TABLE[purity]);

                for (Heat heat : Heat.values()) {
                    ResourceKey<Recipe<?>> key = recipe(purifyName(container, purity, heat));
                    // The unlock is one file shared by all six, so only the first accept writes it.
                    output.accept(key, heat.create(ingredient, result), first ? unlock : null);
                    first = false;
                }
            }
        }

        /**
         * The recipe book unlock for one container: holding either the empty or the filled container
         * is enough, and so is already knowing the recipe.
         */
        private AdvancementHolder purifyUnlock(Container container) {
            ResourceKey<Recipe<?>> representative = recipe(purifyName(container, 0, Heat.SMELTING));
            Advancement.Builder builder = rootedRecipeAdvancement()
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(representative))
                    .rewards(purifyRewards(container))
                    .requirements(AdvancementRequirements.Strategy.OR);
            purifyUnlockItems(container).forEach((name, item) -> builder.addCriterion(name, has(item)));
            return builder.build(ThirstWasTaken2.id("recipes/misc/purify_water_" + container.name()));
        }

        /**
         * A recipe advancement hanging off the recipe root.
         *
         * <p>{@code parent(Identifier)} is deprecated for removal, but the root is only published
         * as an identifier, and the overload that survives wants an {@link AdvancementHolder} that
         * nothing here can produce. Vanilla's own recipe builders make the same call, so this is
         * kept in one place until the root is exposed as a holder.
         */
        @SuppressWarnings("removal")
        private static Advancement.Builder rootedRecipeAdvancement() {
            return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        }

        /** All six recipes for the container, so the recipe book learns the whole family at once. */
        private static AdvancementRewards purifyRewards(Container container) {
            AdvancementRewards.Builder rewards = new AdvancementRewards.Builder();
            for (int purity = 0; purity < PURIFIED; purity++) {
                for (Heat heat : Heat.values()) {
                    rewards.addRecipe(recipe(purifyName(container, purity, heat)));
                }
            }
            return rewards.build();
        }

        /** The empty container and the filled one, named the way the criterion is. */
        private static java.util.SequencedMap<String, ItemLike> purifyUnlockItems(Container container) {
            java.util.SequencedMap<String, ItemLike> unlocks = new java.util.LinkedHashMap<>();
            if (container == Container.BOTTLE) {
                unlocks.put("has_glass_bottle", Items.GLASS_BOTTLE);
                unlocks.put("has_potion", Items.POTION);
            } else if (container == Container.BOWL) {
                unlocks.put("has_terracotta_bowl", ThirstItems.TERRACOTTA_BOWL);
                unlocks.put("has_terracotta_water_bowl", ThirstItems.TERRACOTTA_WATER_BOWL);
            } else {
                unlocks.put("has_bucket", Items.BUCKET);
                unlocks.put("has_water_bucket", Items.WATER_BUCKET);
            }
            return unlocks;
        }

        private static String purifyName(Container container, int purity, Heat heat) {
            return "purify_water_" + container.name() + "_" + purity + "_" + heat.suffix;
        }

        private static Ingredient purifyIngredient(Container container, int purity) {
            DataComponentPatch.Builder patch = DataComponentPatch.builder();
            if (container.potion()) {
                patch.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            }
            patch.set(ThirstComponents.WATER_PURITY, purity);
            patch.set(ThirstComponents.WATER_SALTY, false);
            return DefaultCustomIngredients.components(Ingredient.of(container.item()), patch.build());
        }

        //? if >=26.1 {
        private static ItemStackTemplate purifyResult(Container container, int purity) {
        //?} else
        /*private static ItemStack purifyResult(Container container, int purity) {*/
            if (container.bowl()) return bowlResult(purity);

            DataComponentPatch.Builder patch = DataComponentPatch.builder();
            if (container.potion()) {
                patch.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            }
            patch.set(ThirstComponents.WATER_PURITY, purity);
            patch.set(ThirstComponents.WATER_SALTY, false);
            //? if >=26.1 {
            return new ItemStackTemplate(container.item(), patch.build());
            //?} else {
            /*ItemStack stack = new ItemStack(container.item());
            stack.applyComponents(patch.build());
            return stack;
            *///?}
        }

        /**
         * A filled bowl carries its grade twice: once as the component the game reads, and once as
         * custom model data index 1, so the sprite is right without the client having to look the
         * grade up per frame. {@code WaterPurity.setQuality} writes the same pair at runtime.
         */
        /*
         * Before 26.1 a recipe result is a live ItemStack, whose components serialize as the delta
         * from the item's own defaults. The filled bowl already defaults to grade 3, fresh and
         * custom model data [0, 3], so a result that sets exactly those writes nothing and a result
         * for grade 2 writes no `water_salty`. The stack the furnace hands out is the same either
         * way, because what is missing from the file is what the item supplies anyway; only the
         * 1.21.11 files read shorter than the rest. `ItemStackTemplate` is what fixed this.
         */
        //? if >=26.1 {
        private static ItemStackTemplate bowlResult(int purity) {
            return new ItemStackTemplate(ThirstItems.TERRACOTTA_WATER_BOWL, bowlComponents(purity));
        }
        //?} else {
        /*private static ItemStack bowlResult(int purity) {
            ItemStack stack = new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL);
            stack.applyComponents(bowlComponents(purity));
            return stack;
        }
        *///?}

        private static DataComponentPatch bowlComponents(int purity) {
            return DataComponentPatch.builder()
                    .set(ThirstComponents.WATER_PURITY, purity)
                    .set(ThirstComponents.WATER_SALTY, false)
                    .set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                            List.of(0.0F, (float) purity), List.of(), List.of(), List.of()))
                    .build();
        }
    }

    /** Smelting and campfire cooking differ only in how long they take. */
    private enum Heat {
        SMELTING("smelting", SMELTING_TIME),
        CAMPFIRE("campfire", CAMPFIRE_TIME);

        private final String suffix;
        private final int time;

        Heat(String suffix, int time) {
            this.suffix = suffix;
            this.time = time;
        }

        //? if >=26.1 {
        AbstractCookingRecipe create(Ingredient ingredient, ItemStackTemplate result) {
            Recipe.CommonInfo common = new Recipe.CommonInfo(true);
            AbstractCookingRecipe.CookingBookInfo book =
                    new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, "");
            return this == SMELTING
                    ? new SmeltingRecipe(common, book, ingredient, result, PURIFY_EXPERIENCE, time)
                    : new CampfireCookingRecipe(common, book, ingredient, result, PURIFY_EXPERIENCE, time);
        }
        //?} else {
        /*AbstractCookingRecipe create(Ingredient ingredient, ItemStack result) {
            return this == SMELTING
                    ? new SmeltingRecipe("", CookingBookCategory.MISC, ingredient, result, PURIFY_EXPERIENCE, time)
                    : new CampfireCookingRecipe("", CookingBookCategory.MISC, ingredient, result, PURIFY_EXPERIENCE, time);
        }
        *///?}
    }

    private static ResourceKey<Recipe<?>> recipe(String name) {
        return ResourceKey.create(Registries.RECIPE, ThirstWasTaken2.id(name));
    }
}
