plugins {
    // Picks the Loom variant the active Minecraft version needs. See settings.gradle.kts.
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

// Stonecutter supplies `mod.*` and `deps.*` per version from stonecutter.properties.toml.
// `group` stays unset on purpose: every version node shares this script, and Loom keys parts of its
// cache off the project coordinates. The publication sets its own groupId below.
val modId = property("mod.id") as String
/** Published artifact name; deliberately not the lowercase mod id. */
val modName = property("mod.name") as String
val modGroup = property("mod.group") as String
/** Minecraft range written into fabric.mod.json, e.g. `~26.2`. */
val mcCompat = property("mod.mc_compat") as String
val loaderVersion = property("deps.fabric_loader") as String

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = modName

/** Minecraft 26.1 moved to Java 25; 1.21.x still runs on Java 21. */
val requiredJava: JavaVersion =
    if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

repositories {
    // Loom supplies the Minecraft and Fabric repositories.
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

// Server-side gametests. They are their own source set and their own small mod, so none of it can
// reach the published jar. See src/gametest/java/AGENTS.md.
val gametest: SourceSet = sourceSets.create("gametest")

// Development tooling such as /thirst benchmark. The same arrangement as the gametests: its own source
// set and its own small mod, loaded by runServer and runBenchmark, never packaged. See
// src/dev/java/AGENTS.md.
val dev: SourceSet = sourceSets.create("dev")

loom {
    splitEnvironmentSourceSets()

    mods {
        register("thirstwastaken2") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
        register("thirstwastaken2-gametest") {
            sourceSet(gametest)
        }
        register("thirstwastaken2-dev") {
            sourceSet(dev)
        }
    }

    runs {
        register("gametest") {
            server()
            displayName = "Game Test"
            sourceSet = gametest.name
            // Turns the dedicated server into the GameTest runner. It skips the EULA prompt and the
            // normal server startup, runs every @GameTest method headlessly, then exits non-zero if
            // any of them failed. The runner only checks that the flag is set, not its value.
            systemProperties.put("fabric-api.gametest", "true")
            systemProperties.put(
                "fabric-api.gametest.report-file",
                layout.buildDirectory.file("gametest/report.xml").get().asFile.absolutePath,
            )
        }

        // runServer loads the dev tools, so /thirst benchmark can be typed into its console.
        named("server") {
            sourceSet = dev.name
        }

        // Unattended benchmark: starts the dedicated server, runs `/thirst benchmark <-Pbenchmark>` from
        // the console once it is up, writes run/<version>/benchmark/latest.json and stops the server.
        register("benchmark") {
            server()
            displayName = "Benchmark"
            sourceSet = dev.name
            systemProperties.put("thirstwastaken2.benchmark", providers.gradleProperty("benchmark").getOrElse("standard"))
            systemProperties.put("thirstwastaken2.benchmark.exit", "true")
        }
    }

    runConfigs.all {
        // One run directory per version. Sharing a single one would hand a 26.2 world to a 1.21.11
        // server, which fails on world format rather than on anything the mod did. The gametest
        // runner and datagen get their own again, so a failed run cannot leave a broken world behind
        // for runServer. The benchmark shares runServer's directory, world and accepted EULA, so the
        // two cannot run at the same time.
        runDirectory = when (name) {
            "gametest" -> rootProject.file("run/${project.name}/gametest")
            "datagen" -> rootProject.file("run/${project.name}/datagen")
            else -> rootProject.file("run/${project.name}")
        }
    }
}

/**
 * Every datapack and asset JSON the mod ships, written by `src/datagen`. The directory is keyed by
 * Minecraft version rather than by build node, because two nodes of the same Minecraft version on
 * different loaders produce byte-identical files and should share one directory. It is a resource
 * root of `main`, so the jar picks it up with no further wiring.
 *
 * Regenerate with `:<version>:runDatagen`; `:<version>:checkDatagen` fails when the committed files
 * and the generators have drifted apart. See src/datagen/java/AGENTS.md.
 */
val generatedResources: File = rootProject.file("src/main/generated/${sc.current.version}")

fabricApi.configureDataGeneration {
    outputDirectory.set(generatedResources)
    // Its own source set and its own small mod, like the gametests and the dev tools, so none of the
    // generator code can reach the published jar.
    createSourceSet = true
    modId = "thirstwastaken2-datagen"
    // The item model providers live in net.minecraft.client.data, so datagen runs as a client.
    client = true
    // Every advancement the mod awards by id is `minecraft:impossible`, which strict validation
    // reads as an unreachable advancement.
    strictValidation = false
}

// Loom adds the datagen output to `main`'s resources by reading the source directories back and
// setting them again, which flattens them to plain files and loses the task dependency Stonecutter
// had attached to the one it generates. Without this, building any node other than the active one
// fails: the tasks that read those resources have not been told to wait for them.
tasks.named("processResources") { dependsOn("stonecutterGenerate") }

/*
 * Datagen keeps a hash cache of what it last wrote and skips a file whose hash still matches, so a
 * generated file edited by hand survives a regeneration, and a file no longer generated at all is
 * only deleted while the cache still remembers writing it. Emptying the directory first costs
 * nothing at 58 small files and makes the task mean what its name says: what is on disk afterwards
 * is what the generators produce, and `checkDatagen` can trust the difference.
 */
tasks.named("runDatagen") {
    doFirst {
        generatedResources.deleteRecursively()
    }
}

// The gametests and the dev tools compile and run against the mod itself and against everything the
// mod uses.
gametest.compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
gametest.runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
dev.compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
dev.runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output

/**
 * Adds a client-only mod dependency. Loom prefixes these configurations with `mod` where it remaps
 * dependencies and leaves them bare where it does not, so resolve the name that actually exists.
 */
fun clientMod(configuration: String, notation: String) {
    val prefixed = "mod${configuration.replaceFirstChar(Char::uppercase)}"
    val target = if (configurations.findByName(prefixed) != null) prefixed else configuration
    dependencies.add(target, notation)
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // No-op from 26.1 on, which ships unobfuscated.
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Optional integrations. The mod runs without either, but both are compiled against, so they
    // have to resolve on every version.
    clientMod("clientCompileOnly", "maven.modrinth:modmenu:${property("deps.modmenu")}")
    clientMod("clientCompileOnly", "maven.modrinth:appleskin:${property("deps.appleskin")}")
    // Test the client HUD and food tooltips alongside AppleSkin in runClient.
    clientMod("clientRuntimeOnly", "maven.modrinth:appleskin:${property("deps.appleskin")}")
    // AppleSkin uses Cloth Config for its Mod Menu configuration screen.
    clientMod("clientRuntimeOnly", "maven.modrinth:cloth-config:${property("deps.cloth_config")}")
    clientMod("clientRuntimeOnly", "maven.modrinth:modmenu:${property("deps.modmenu")}")
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft" to mcCompat,
        "loader" to loaderVersion,
        "java" to requiredJava.majorVersion,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
}

// The client source set currently has Java only. Keep its runtime classpath entry present so Fabric
// Loader does not report build/resources/client as a missing path during runClient.
tasks.named("runClient") {
    doFirst {
        layout.buildDirectory.dir("resources/client").get().asFile.mkdirs()
    }
}

// Per-directory notes for contributors and backup copies of edited textures; they live next to the
// files they describe, not in the jars.
tasks.withType<ProcessResources>().configureEach {
    exclude("**/AGENTS.md", "**/*.bak")
}

// Registered lazily: withSourcesJar() below adds the task after this block is evaluated. The
// dependency is the one described above, which the sources jar needs for the same reason
// processResources does; `.cache` is datagen's hash cache, which Loom keeps out of the mod jar but
// not out of this one.
tasks.withType<Jar>().matching { it.name.endsWith("sourcesJar") }.configureEach {
    dependsOn("stonecutterGenerate")
    mustRunAfter("runDatagen")
    exclude("**/AGENTS.md", "**/*.bak", "**/.cache/**")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modId" }
    }
}

/**
 * Regenerates the datapack and asset JSON and fails when the result differs from what is committed.
 * This is the check that keeps `src/datagen` and `src/main/generated` from drifting apart: editing a
 * generated file by hand, or a generator without regenerating, both fail here.
 *
 * It asks git rather than diffing trees itself, because git already knows which files are committed
 * and which are new, and datagen writes in place.
 */
tasks.register("checkDatagen") {
    group = "verification"
    description = "Fails when the committed generated resources do not match what the generators produce"
    dependsOn("runDatagen")

    val root = rootProject.projectDir
    val generated = generatedResources

    doLast {
        val relative = root.toPath().relativize(generated.toPath()).toString().replace('\\', '/')
        val process = ProcessBuilder("git", "status", "--porcelain", "--", relative)
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val changes = process.inputStream.bufferedReader().readText().trim()
        check(process.waitFor() == 0) { "git status failed:\n$changes" }
        check(changes.isEmpty()) {
            "Generated resources are out of date. Run \":${project.name}:runDatagen\" and commit:\n$changes"
        }
    }
}

/** Collects the jars every version produces into one directory, for `chiseledBuild`. */
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jar and copies it to build/libs/"
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = modGroup
            from(components["java"])
        }
    }
    repositories { }
}
