plugins {
    id("dev.kikugie.stonecutter")
}

// The version the source tree is currently checked out for. Switch it with
// `./gradlew "Set active project to <name>"`; every version is built regardless by `chiseledBuild`.
stonecutter active "26.2.x"


stonecutter parameters {
    // Bakes the target Minecraft version into the jar, so the startup log line is never stale.
    swaps["minecraft"] = "\"${node.metadata.version}\";"

    replacements {
        // 26.1 renamed the HUD draw target while keeping the drawing methods identical, so the
        // whole difference is the type name.
        string(current.parsed < "26.1") {
            replace("GuiGraphicsExtractor", "GuiGraphics")
            // The Fabric data generation API renamed both of these for 26.1 without changing what
            // they do, so the datagen providers name the newer pair and get the older one here.
            replace("FabricPackOutput", "FabricDataOutput")
            replace("FabricTagsProvider", "FabricTagProvider")
        }

        // 26.2 split the advancement trigger classes out of `net.minecraft.advancements` into
        // `triggers` and `predicates`, keeping every class name. Only the datagen providers name
        // them, and only in imports, so each one is replaced whole rather than by package prefix:
        // `CriteriaTriggers` and `Criterion` did not move into `criterion` with the rest.
        string(current.parsed < "26.2") {
            replace("net.minecraft.advancements.triggers.CriteriaTriggers",
                    "net.minecraft.advancements.CriteriaTriggers")
            replace("net.minecraft.advancements.triggers.Criterion",
                    "net.minecraft.advancements.Criterion")
            replace("net.minecraft.advancements.triggers.ImpossibleTrigger",
                    "net.minecraft.advancements.criterion.ImpossibleTrigger")
            replace("net.minecraft.advancements.triggers.PlayerTrigger",
                    "net.minecraft.advancements.criterion.PlayerTrigger")
            replace("net.minecraft.advancements.triggers.RecipeCraftedTrigger",
                    "net.minecraft.advancements.criterion.RecipeCraftedTrigger")
            replace("net.minecraft.advancements.triggers.RecipeUnlockedTrigger",
                    "net.minecraft.advancements.criterion.RecipeUnlockedTrigger")
        }
    }
}
