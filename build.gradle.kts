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
        // runner gets its own again, so a failed run cannot leave a broken world behind for
        // runServer. The benchmark shares runServer's directory, world and accepted EULA, so the
        // two cannot run at the same time.
        runDirectory = if (name == "gametest") {
            rootProject.file("run/${project.name}/gametest")
        } else {
            rootProject.file("run/${project.name}")
        }
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

// Registered lazily: withSourcesJar() below adds the task after this block is evaluated.
tasks.withType<Jar>().matching { it.name.endsWith("sourcesJar") }.configureEach {
    exclude("**/AGENTS.md", "**/*.bak")
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
