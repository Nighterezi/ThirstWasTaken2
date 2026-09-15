/*
 * The part of the build every node shares, whatever mod loader it builds for: the Java toolchain,
 * the two seam checks, what never belongs in a jar, and the task that collects the jars.
 *
 * Applied by both `build.gradle.kts` (the Fabric nodes) and `build.neoforge.gradle.kts`. Each of
 * those sets the `thirst.requiredJava` extra property first, because the Java version a node needs
 * follows from its Minecraft version and only the node's own script can read that.
 *
 * Two things are deliberately not here:
 *
 * - **The source directory wiring.** Loom splits `main` and `client`; ModDevGradle has no split, so
 *   the NeoForge node compiles the client sources into `main`. The two scripts wire their source
 *   sets differently on purpose. See docs/dev/P4-NEOFORGE-PLAN.md.
 * - **What `buildAndCollect` copies.** The jar a node ships is Loom's remapped jar on Fabric and the
 *   plain `jar` on NeoForge, so each script adds its own inputs to the task registered below.
 */

/** Set by the applying script, from the Minecraft version of its node. */
val requiredJava: JavaVersion =
    JavaVersion.toVersion(project.extensions.extraProperties.get("thirst.requiredJava") as String)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

configure<JavaPluginExtension> {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        vendor.set(JvmVendorSpec.ADOPTIUM)
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

// Per-directory notes for contributors and backup copies of edited textures; they live next to the
// files they describe, not in the jars.
tasks.withType<ProcessResources>().configureEach {
    exclude("**/AGENTS.md", "**/*.bak")
}

/**
 * Fails when loader independent code names a mod loader. `src/main/java` and `src/client/java` are
 * compiled against Fabric API today, so the compiler cannot catch a Fabric import there; this can.
 * It cannot see the methods Fabric API injects into vanilla classes, such as `getAttachedOrCreate`,
 * which only the first NeoForge build will report.
 */
tasks.register("checkLoaderSeam") {
    group = "verification"
    description = "Fails when loader independent sources import a mod loader's API"

    val roots = listOf("src/main/java", "src/client/java").map(rootProject::file)
    val forbidden = Regex("""\b(net\.fabricmc|net\.neoforged)\.""")
    inputs.files(roots.map { fileTree(it) { include("**/*.java") } })

    doLast {
        val offenders = roots.flatMap { root ->
            root.walk().filter { it.extension == "java" }.flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> forbidden.containsMatchIn(line) }
                    .map { (index, line) ->
                        "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                    }
            }
        }
        check(offenders.isEmpty()) {
            "Loader API in loader independent code. Route it through platform/Loader or " +
                "client/platform/ClientLoader instead:\n" + offenders.joinToString("\n")
        }
    }
}

/**
 * Fails when core code carries a Stonecutter version conditional. Minecraft version differences belong
 * in `platform/` and, for injection signatures, `mixin/`; a `//?` block anywhere else in `src/main/java`
 * or `src/client/java` means a seam is missing. This replaced counting blocks as the exit ramp; see
 * docs/dev/PLATFORM-PLAN.md. Loader directories, datagen, gametests and dev tools are outside it.
 */
tasks.register("checkVersionSeam") {
    group = "verification"
    description = "Fails when core sources outside platform/ and mixin/ contain a version conditional"

    val roots = listOf("src/main/java", "src/client/java").map(rootProject::file)
    val allowed = setOf("platform", "mixin")
    inputs.files(roots.map { fileTree(it) { include("**/*.java") } })

    doLast {
        val offenders = roots.flatMap { root ->
            root.walk()
                .filter { it.extension == "java" }
                .filterNot { file -> file.relativeTo(root).invariantSeparatorsPath.split('/').any(allowed::contains) }
                .flatMap { file ->
                    file.readLines().withIndex()
                        .filter { (_, line) -> line.contains("//?") }
                        .map { (index, line) ->
                            "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                        }
                }
        }
        check(offenders.isEmpty()) {
            "Version conditional in core code. Put the difference behind platform/Vanilla or " +
                "client/platform/ClientVanilla instead:\n" + offenders.joinToString("\n")
        }
    }
}

/** Collects the jars every node produces into one directory, for `chiseledBuild`. */
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jar and copies it to build/libs/"
    into(rootProject.layout.buildDirectory.dir("libs"))
}
