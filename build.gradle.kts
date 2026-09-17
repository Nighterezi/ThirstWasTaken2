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

/**
 * The JVM arguments that record a run with JFR, which `-Pprofile` adds to runBenchmark.
 *
 * `settings=profile` is the heavier of JFR's two built-in configurations, and the recording is dumped
 * when the server stops, which is what an unattended benchmark does on its own. The other three are
 * what make the recording worth opening: a stack depth far past JFR's default of 64, because a
 * Minecraft stack is deeper than that and a truncated one merges call sites that are not the same,
 * and the two diagnostic flags that let a sample land where the code actually was instead of at the
 * nearest safepoint.
 */
fun flightRecorder(file: File): List<String> = listOf(
    "-XX:StartFlightRecording=settings=profile,dumponexit=true,filename=${file.absolutePath}",
    "-XX:FlightRecorderOptions:stackdepth=1024",
    "-XX:+UnlockDiagnosticVMOptions",
    "-XX:+DebugNonSafepoints",
)

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
            // A world of its own inside runServer's directory, generated with the fixed seed
            // gradle/shared.gradle.kts puts in server.properties, so a run measures the same terrain
            // on every machine and on both loaders of this Minecraft version, and never the dev world.
            programArguments.addAll("--world", providers.gradleProperty("thirst.benchmark.world").get())

            // `-Pprofile` records the run with JFR, which is in every JDK, and writes
            // run/<node>/benchmark/latest.jfr for JDK Mission Control to open. `settings=profile` is the
            // heavier of JFR's two built-in profiles; the deep stack depth is what makes an allocation
            // event name the mod's own call site rather than a truncated vanilla frame, and
            // DebugNonSafepoints lets a sample land where the code really was rather than at the nearest
            // safepoint. A recording slows the run down and skews every figure in the report, which is
            // why the report says so and aggregate.py refuses a set with one in it.
            if (providers.gradleProperty("profile").isPresent) {
                jvmArguments.addAll(flightRecorder(rootProject.file("run/${project.name}/benchmark/latest.jfr")))
            }
        }
    }

    runConfigs.all {
        // `-Pagent=<file>` answers that file of agent requests once the game is up and then stops it,
        // which is what an unattended run is. Without it the agent is still there, waiting on
        // run/<node>/agent/<side>/in.jsonl. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md.
        providers.gradleProperty("agent").orNull?.let { script ->
            systemProperties.put("thirstwastaken2.agent.script", rootProject.file(script).absolutePath)
            systemProperties.put("thirstwastaken2.agent.script.exit", "true")
        }

        // `-Pdriven` says this client is driven by an agent rather than played: it opens maximised
        // and never takes the mouse pointer, so the desktop stays usable while a script drives it.
        // An unattended `-Pagent=<file>` run implies it. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md.
        if (providers.gradleProperty("driven").isPresent) {
            systemProperties.put("thirstwastaken2.agent.driven", "true")
        }

        // `-Pquickplay=<world>` opens that singleplayer world straight from launch, so an unattended
        // `-Pagent` script starts inside it. The world has to exist in run/<node>/saves.
        if (name == "client") {
            providers.gradleProperty("quickplay").orNull?.let { world ->
                programArguments.addAll("--quickPlaySingleplayer", world)
            }
        }

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
// The dev tools have loader code of their own, under the same rule: the entrypoints, and the small
// seam the agent needs beyond the mod's own `platform/Loader`. See src/dev/java/AGENTS.md.
dev.java.srcDir("src/dev/$loader/java")
dev.resources.srcDir("src/dev/$loader/resources")
// So do the gametests, for the one test that needs a connection reporting the mod's channel.
gametest.java.srcDir("src/gametest/$loader/java")

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
// runServer and runBenchmark run the dev source set. The published jar carries the client classes on
// a dedicated server too, and a mod that loads one of them by name there, as Jade does with the `jade`
// entrypoint, crashes a dev server that lacks them. Only the classes are added, not the client's
// runtime mods, so the client-only dependencies stay out of the server.
dev.runtimeClasspath += sourceSets["client"].output
// The agent's client probes read the HUD, the framebuffer and the key state, so the dev source set
// compiles against `client` as well as against `main`. Loom's split keeps the two apart for the mod,
// where common code reaching a client class is a real mistake; a tool whose whole job is to read what
// a client holds is on both sides by definition. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md.
dev.compileClasspath += sourceSets["client"].compileClasspath + sourceSets["client"].output

/**
 * What runClient runs: the dev tools on top of the client. It cannot simply be handed `dev` the way
 * runServer is, because `dev`'s runtime classpath deliberately carries the client's *classes* without
 * the client's runtime mods, and runClient is meant to load AppleSkin, Jade and the rest. This source
 * set has no sources of its own and exists only to put both on one classpath — the same arrangement
 * build.neoforge.gradle.kts uses for its own extra clients.
 */
val devClient: SourceSet = sourceSets.create("devClient") {
    runtimeClasspath = dev.output + dev.runtimeClasspath + sourceSets["client"].runtimeClasspath
}

loom {
    runs {
        // runClient loads the dev tools too, so an agent can drive a real client through
        // run/<node>/agent/client/. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md.
        named("client") {
            sourceSet = devClient.name
        }
    }
}

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

// The dev tools have a mixin config of their own, for the one mixin that records where the HUD drew
// the bar. It needs the same compatibility level as the rest, for the same reason: a node on Java 25
// writes class files Mixin refuses to read at level 21.
tasks.named<ProcessResources>("processDevResources") {
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
    // The MIT notice of the code this mod is based on has to ship with it, alongside the GPL.
    from(files(rootProject.file("LICENSE"), rootProject.file("CREDITS.md"),
            rootProject.file("licenses/ThirstWasTaken-MIT.txt"))) {
        rename { name -> name.substringBeforeLast('.') + "_$modId" + name.removePrefix(name.substringBeforeLast('.')) }
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
