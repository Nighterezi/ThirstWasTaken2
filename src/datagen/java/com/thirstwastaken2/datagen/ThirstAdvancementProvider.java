package com.thirstwastaken2.datagen;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.advancements.triggers.RecipeCraftedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The mod's own advancement tab.
 *
 * <p>Every advancement here except the root and {@code boil_water} has a single
 * {@code minecraft:impossible} criterion and is granted by id from
 * {@code com.thirstwastaken2.advancement.ThirstAdvancements}, so the ids and the criterion name have
 * to match that class. {@code AdvancementGameTest} is what catches it when they stop matching.
 *
 * <p>{@code boil_water} is the exception: a furnace credits the player who takes the result, so
 * vanilla's {@code recipe_crafted} trigger can see it. The nine campfire recipes cannot be in it,
 * because a campfire has no player to credit.
 */
public final class ThirstAdvancementProvider extends FabricAdvancementProvider {
    /** The criterion name {@code ThirstAdvancements} awards by. */
    private static final String CRITERION = "thirst";

    public ThirstAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        AdvancementHolder root = builder()
                .display(
                        ThirstItems.WATERSKIN,
                        title("root"),
                        description("root"),
                        Identifier.withDefaultNamespace("block/terracotta"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                // The root is the one advancement nothing grants by hand: everyone gets it on their
                // first tick, which is what makes the tab appear at all.
                .addCriterion(CRITERION, new Criterion<>(CriteriaTriggers.TICK,
                        new PlayerTrigger.TriggerInstance(java.util.Optional.empty())))
                .build(ThirstWasTaken2.id("root"));
        consumer.accept(root);

        AdvancementHolder firstDrink = awarded(consumer, root, "first_drink",
                ThirstItems.TERRACOTTA_WATER_BOWL, AdvancementType.TASK);

        AdvancementHolder dirtyWater = awarded(consumer, firstDrink, "dirty_water",
                Items.MUD, AdvancementType.TASK);

        AdvancementHolder boilWater = boilWater(dirtyWater);
        consumer.accept(boilWater);

        awarded(consumer, boilWater, "purified_water", Items.GLASS_BOTTLE, AdvancementType.TASK);
        awarded(consumer, firstDrink, "sea_water", Items.KELP, AdvancementType.GOAL);
        awarded(consumer, firstDrink, "nether_drink", Items.MAGMA_BLOCK, AdvancementType.GOAL);
    }

    /** One advancement the drinking code grants by id, so its only criterion is impossible. */
    private static AdvancementHolder awarded(
            Consumer<AdvancementHolder> consumer,
            AdvancementHolder parent,
            String name,
            ItemLike icon,
            AdvancementType type) {
        AdvancementHolder advancement = builder()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion(CRITERION, new Criterion<>(CriteriaTriggers.IMPOSSIBLE,
                        new ImpossibleTrigger.TriggerInstance()))
                .build(ThirstWasTaken2.id(name));
        consumer.accept(advancement);
        return advancement;
    }

    /**
     * Boiling any of the nine smelting recipes, as an OR. Vanilla sees this one without help, which
     * is why it is also the parent of {@code purified_water} rather than a sibling.
     */
    private static AdvancementHolder boilWater(AdvancementHolder parent) {
        Advancement.Builder builder = builder()
                .parent(parent)
                .display(Items.FURNACE, title("boil_water"), description("boil_water"), null,
                        AdvancementType.TASK, true, true, false);

        for (String container : List.of("bottle", "bowl", "bucket")) {
            for (int purity = 0; purity < 3; purity++) {
                ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                        ThirstWasTaken2.id("purify_water_" + container + "_" + purity + "_smelting"));
                builder.addCriterion(container + "_" + purity, new Criterion<>(CriteriaTriggers.RECIPE_CRAFTED,
                        new RecipeCraftedTrigger.TriggerInstance(java.util.Optional.empty(), key, List.of())));
            }
        }

        // Any one of the nine is enough, so one requirements list holding all of them.
        return builder
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(ThirstWasTaken2.id("boil_water"));
    }

    /**
     * A builder with the telemetry flag left off, which {@code Advancement.Builder.advancement()}
     * would set. Vanilla only ever reads that flag for advancements in the {@code minecraft}
     * namespace, so setting it on ours would write a line into every file that nothing can act on.
     */
    private static Advancement.Builder builder() {
        return new Advancement.Builder();
    }

    private static Component title(String name) {
        return Component.translatable("advancements.thirstwastaken2." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.thirstwastaken2." + name + ".description");
    }

    @Override
    public String getName() {
        return "ThirstWasTaken2 Advancements";
    }
}
