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
        }
    }
}
