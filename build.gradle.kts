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
        register("thirstwastaken2_gametest") {
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
 * The mod loader this node builds for. Loader code lives beside the source set it belongs to, in
 * `src/main/<loader>` and `src/client/<loader>`, and only this loader's directories are compiled.
 * Everything else in `src/main/java` and `src/client/java` is loader independent; `checkLoaderSeam`
 * enforces it. The NeoForge node has a script of its own, build.neoforge.gradle.kts. See
 * src/main/java/com/thirstwastaken2/platform/AGENTS.md.
 *
 * The directories sit inside `src/main` and `src/client` rather than in a `src/<loader>` of their
 * own because Stonecutter only rewrites versioned comments under `src/<source set>`: anywhere else,
 * every node but the active one would compile an empty directory.
 */
val loader = "fabric"

sourceSets.main {
    java.srcDir("src/main/$loader/java")
    resources.srcDir("src/main/$loader/resources")
}
sourceSets.named("client") {
    java.srcDir("src/client/$loader/java")
    resources.srcDir("src/client/$loader/resources")
}

/**
 * The Create Fly version this node compiles the Sand Filter against, or null where it does not. Create
 * Fly is a Fabric-only port with no release for every Minecraft version the mod supports, so the
 * integration is its own pair of source directories that only such a node compiles, and the
 * manifest only names its entrypoints and mixin config there. See src/main/createfly/AGENTS.md.
 */
val createFly = findProperty("deps.create_fly") as String?

/**
 * Create Fly's classes, without the files that make it a mod. Its class tweaker makes vanilla's
 * `Container` implement one of Create's interfaces, and Loom bakes the tweakers of every mod on the
 * compile classpath into the one Minecraft jar all of this node's runs share - so the gametests and
 * runServer, which run without Create Fly, would fail to load `Container`. Turning transitive tweakers
 * off is not an option, because Fabric API's own injected methods need them. Compiled against as a
 * plain library, Create Fly leaves the Minecraft jar alone; in runClient and in a player's game,
 * Fabric Loader applies its tweaker from the real jar as usual.
 */
val createFlyClasses = createFly?.let { version ->
    val resolved = configurations.detachedConfiguration(dependencies.create("maven.modrinth:create-fly:$version"))
        .apply { isTransitive = false }
    tasks.register<Jar>("createFlyClasses") {
        description = "Copies Create Fly's classes into a jar Loom does not treat as a mod"
        destinationDirectory = layout.buildDirectory.dir("createfly")
        archiveFileName = "create-fly-$version-classes.jar"
        from(resolved.elements.map { jars -> jars.map { zipTree(it) } }) {
            include("com/zurrtum/**")
        }
    }
}

if (createFly != null) {
    sourceSets.main {
        java.srcDir("src/main/createfly/java")
        resources.srcDir("src/main/createfly/resources")
    }
    sourceSets.named("client") {
        java.srcDir("src/client/createfly/java")
    }
    // The benchmark's Create Fly operations. They run only when Create Fly is on the benchmark's classpath,
    // which `-Pcreate` asks for below.
    dev.java.srcDir("src/dev/createfly/java")
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

    // Optional integrations. The mod runs without any of them, but all are compiled against, so they
    // have to resolve on every version.
    clientMod("clientCompileOnly", "maven.modrinth:modmenu:${property("deps.modmenu")}")
    clientMod("clientCompileOnly", "maven.modrinth:appleskin:${property("deps.appleskin")}")
    clientMod("clientCompileOnly", "maven.modrinth:jade:${property("deps.jade")}")
    // Test the client HUD and food tooltips alongside AppleSkin in runClient.
    clientMod("clientRuntimeOnly", "maven.modrinth:appleskin:${property("deps.appleskin")}")
    // AppleSkin uses Cloth Config for its Mod Menu configuration screen.
    clientMod("clientRuntimeOnly", "maven.modrinth:cloth-config:${property("deps.cloth_config")}")
    clientMod("clientRuntimeOnly", "maven.modrinth:modmenu:${property("deps.modmenu")}")
    // Test the water purity line Jade shows when looking at water or a cauldron.
    clientMod("clientRuntimeOnly", "maven.modrinth:jade:${property("deps.jade")}")
    // Test the drinks and meals Farmer's Delight adds, and the c:drinks tag it fills.
    clientMod("clientRuntimeOnly", "maven.modrinth:farmers-delight-refabricated:${property("deps.farmersdelight")}")

    if (createFlyClasses != null) {
        // The Sand Filter extends Create classes on both sides, so both source sets compile against it.
        compileOnly(files(createFlyClasses))
        "clientCompileOnly"(files(createFlyClasses))
        // Test the Sand Filter with pipes, pumps and spouts in runClient.
        clientMod("clientRuntimeOnly", "maven.modrinth:create-fly:$createFly")
        // `-Pcreate` puts Create Fly on runServer and runBenchmark too, to benchmark the mod with it
        // installed. Off by default, so the usual benchmark measures the mod alone.
        if (providers.gradleProperty("create").isPresent) {
            "devRuntimeOnly"("maven.modrinth:create-fly:$createFly")
        }
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft" to mcCompat,
        "loader" to loaderVersion,
        "java" to requiredJava.majorVersion,
    )
    inputs.properties(props)
    inputs.property("createFly", createFly ?: "")
    filesMatching("fabric.mod.json") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }

    // Only a node that compiles the Sand Filter may name its entrypoints and mixin config, or Fabric
    // Loader would fail to find them on every other one. They are added to the built manifest rather
    // than templated into the source, which has to stay valid JSON for Loom to read.
    if (createFly != null) {
        val manifest = destinationDir.resolve("fabric.mod.json")
        doLast {
            @Suppress("UNCHECKED_CAST")
            val json = groovy.json.JsonSlurper().parse(manifest) as MutableMap<String, Any>
            @Suppress("UNCHECKED_CAST")
            val entrypoints = json.getValue("entrypoints") as MutableMap<String, Any>
            entrypoints["thirstwastaken2:createfly"] = listOf("com.thirstwastaken2.createfly.CreateFlyEntrypoint")
            entrypoints["thirstwastaken2:createfly_client"] =
                listOf("com.thirstwastaken2.client.createfly.CreateFlyClientEntrypoint")
            @Suppress("UNCHECKED_CAST")
            (json.getValue("mixins") as MutableList<Any>).add(1, "thirstwastaken2.createfly.mixins.json")
            manifest.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(json)))
        }
    }
}

// The client mixins have their own configs, in the client source set: the loader independent one in
// src/client/resources and Fabric's own in src/client/fabric/resources.
tasks.named<ProcessResources>("processClientResources") {
    inputs.property("java", requiredJava.majorVersion)
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
}

// Keep the client source set's runtime classpath entry present so Fabric Loader does not report
// build/resources/client as a missing path during runClient.
tasks.named("runClient") {
    doFirst {
        layout.buildDirectory.dir("resources/client").get().asFile.mkdirs()
    }
}

// Registered lazily: withSourcesJar(), in gradle/shared.gradle.kts below, adds the task after this
// block is evaluated. The dependency is the one described above, which the sources jar needs for the
// same reason processResources does; `.cache` is datagen's hash cache, which Loom keeps out of the mod
// jar but not out of this one.
tasks.withType<Jar>().matching { it.name.endsWith("sourcesJar") }.configureEach {
    dependsOn("stonecutterGenerate")
    mustRunAfter("runDatagen")
    exclude("**/AGENTS.md", "**/*.bak", "**/.cache/**")
}

// The toolchain, the seam checks, the jar excludes and `buildAndCollect` are shared with the
// NeoForge node, which cannot apply this script. The Java version is passed in because it follows
// from the node's Minecraft version, which only this script can read.
extra["thirst.requiredJava"] = requiredJava.majorVersion
apply(from = rootProject.file("gradle/shared.gradle.kts"))

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

// What this node hands `buildAndCollect`, which gradle/shared.gradle.kts registers: Loom's remapped
// jar, not the plain one.
tasks.named<Copy>("buildAndCollect") {
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
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
