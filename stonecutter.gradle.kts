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
        // 1.21.11 renamed ResourceLocation to Identifier, ResourceKey#location to #identifier, and
        // moved Util into net.minecraft.util, changing nothing else about any of them.
        string(current.parsed < "1.21.11") {
            replace("Identifier", "ResourceLocation")
            replace(".identifier()", ".location()")
            replace("net.minecraft.util.Util", "net.minecraft.Util")
        }

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
        // The package they came from was spelled `critereon` until 1.21.11. Replacements do not
        // chain, so the older spelling has to be chosen here rather than by a rule of its own.
        val criterion = if (current.parsed < "1.21.11") "critereon" else "criterion"
        string(current.parsed < "26.2") {
            replace("net.minecraft.advancements.triggers.CriteriaTriggers",
                    "net.minecraft.advancements.CriteriaTriggers")
            replace("net.minecraft.advancements.triggers.Criterion",
                    "net.minecraft.advancements.Criterion")
            replace("net.minecraft.advancements.triggers.InventoryChangeTrigger",
                    "net.minecraft.advancements.$criterion.InventoryChangeTrigger")
            replace("net.minecraft.advancements.triggers.ImpossibleTrigger",
                    "net.minecraft.advancements.$criterion.ImpossibleTrigger")
            replace("net.minecraft.advancements.triggers.PlayerTrigger",
                    "net.minecraft.advancements.$criterion.PlayerTrigger")
            replace("net.minecraft.advancements.triggers.RecipeCraftedTrigger",
                    "net.minecraft.advancements.$criterion.RecipeCraftedTrigger")
            replace("net.minecraft.advancements.triggers.RecipeUnlockedTrigger",
                    "net.minecraft.advancements.$criterion.RecipeUnlockedTrigger")
        }

        // 1.21.2 renamed the server-side CONSUME result to SUCCESS_SERVER, both meaning "done, and the
        // client already swung", and Registry#get(ResourceKey) to getValue.
        string(current.parsed < "1.21.2") {
            replace("InteractionResult.SUCCESS_SERVER", "InteractionResult.CONSUME")
            replace("CREATIVE_MODE_TAB.getValue(", "CREATIVE_MODE_TAB.get(")
        }

        // 1.21.4 moved the data generator's model classes under net.minecraft.client, unchanged, and
        // Fabric API moved its model provider into its client package with them.
        string(current.parsed < "1.21.4") {
            replace("net.minecraft.client.data.models.", "net.minecraft.data.models.")
            replace("net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider",
                    "net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider")
        }

        // 1.21.5 renamed the Confusion effect to Nausea and Entity#moveTo to snapTo.
        string(current.parsed < "1.21.5") {
            replace("MobEffects.NAUSEA", "MobEffects.CONFUSION")
            replace(".snapTo(", ".moveTo(")
        }

        // The NeoForge node runs the same tests through a harness of its own, which reads a
        // @GameTest annotation from src/gametest/neoforge instead of Fabric API's. Only the import
        // changes, so a test keeps writing `@GameTest` with no arguments. See src/gametest/java/AGENTS.md.
        string(current.project.endsWith("-neoforge")) {
            replace("import net.fabricmc.fabric.api.gametest.v1.GameTest;",
                    "import com.thirstwastaken2.gametest.neoforge.GameTest;")
        }

        // Fabric API's own @GameTest arrived with 1.21.5. Before it a test uses vanilla's annotation and
        // names Fabric's empty structure as its template; no test body changes.
        string(current.parsed < "1.21.5") {
            replace("import net.fabricmc.fabric.api.gametest.v1.GameTest;",
                    "import net.minecraft.gametest.framework.GameTest;")
            replace("@GameTest",
                    "@GameTest(template = net.fabricmc.fabric.api.gametest.v1.FabricGameTest.EMPTY_STRUCTURE)")
        }
    }
}
