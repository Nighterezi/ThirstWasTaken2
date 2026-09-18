plugins {
    // NeoForge's build plugin. Its version is on the plugin classpath from settings.gradle.kts,
    // which resolves it without applying it.
    id("net.neoforged.moddev")
    `maven-publish`
}

/*
 * The NeoForge node, `26.2.x-neoforge`. One Minecraft version, one loader, the same sources as
 * `26.2.x`. Everything both this and the Fabric nodes need is in gradle/shared.gradle.kts, applied
 * below. See src/main/java/com/thirstwastaken2/platform/AGENTS.md.
 */

// Stonecutter supplies `mod.*` and `deps.*` for this node from stonecutter.properties.toml.
// `group` stays unset for the same reason build.gradle.kts leaves it unset; the publication sets
// its own groupId below.
val modId = property("mod.id") as String
/** The dev tools' mod id. NeoForge ids cannot contain a hyphen, so it is not the Fabric `-dev` spelling. */
val devModId = "thirstwastaken2_dev"
/** `-Pagent=<file>`: a file of agent requests to answer once, unattended. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md. */
val agentScript: String? = providers.gradleProperty("agent").orNull?.let { rootProject.file(it).absolutePath }

/** `-Pdriven`: this client is driven by an agent, not played. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md. */
val drivenClient: Boolean = providers.gradleProperty("driven").isPresent
/** Published artifact name; deliberately not the lowercase mod id. */
val modName = property("mod.name") as String
val modGroup = property("mod.group") as String
/** Minecraft range written into neoforge.mods.toml, in Maven range syntax, e.g. `[26.2,26.3)`. */
val mcRange = property("mod.mc_compat") as String
val neoForgeVersion = property("deps.neoforge") as String

// The loader goes in the version so the jar is `ThirstWasTaken2-1.0.6+26.2-neoforge.jar` and never
// collides with the Fabric jar of the same Minecraft version in build/libs/.
version = "${property("mod.version")}+${sc.current.version}-neoforge"
base.archivesName = modName

/** Minecraft 26.1 moved to Java 25; 1.21.x still runs on Java 21. The same rule as the Fabric nodes. */
val requiredJava: JavaVersion =
    if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

repositories {
    // ModDevGradle supplies the NeoForged, Mojang and Minecraft libraries repositories.
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

/**
 * The mod loader this node builds for. Loader code lives beside the source set it belongs to, in
 * `src/main/<loader>` and `src/client/<loader>`, and only this loader's directories are compiled.
 * Everything else in `src/main/java` and `src/client/java` is loader independent; `checkLoaderSeam`
 * enforces it. See src/main/java/com/thirstwastaken2/platform/AGENTS.md.
 */
val loader = "neoforge"

/*
 * One source set on NeoForge. ModDevGradle has no split between `main` and `client`, so the client
 * sources compile into `main` here. The four Fabric nodes keep `loom.splitEnvironmentSourceSets()`,
 * so the compiler still catches client code reached from common code on four nodes out of five.
 *
 * `src/datagen` is absent on purpose: the generators stay Fabric only, and this node reads the
 * datapack and asset JSON that the 26.2 Fabric node writes. `src/gametest` and `src/dev` are both
 * here, each as a mod of its own, below.
 *
 * `src/client` cannot simply be listed as a directory of `main`. Stonecutter preprocesses
 * `src/<name>` only for a source set called `<name>`, so without a `client` source set nothing
 * writes this node's copy of the client sources, and a `src/client/...` directory on `main` then
 * resolves to nothing and compiles nothing, without an error. The `client` source set below exists
 * only to be preprocessed; it is never compiled or packaged. Its output goes into `main` by path:
 * the sources themselves when this is the active node, which Stonecutter leaves in place, and
 * Stonecutter's generated copy otherwise.
 */
sourceSets.create("client")
val clientSources: File =
    if (sc.current.isActive) rootProject.file("src/client")
    else layout.buildDirectory.dir("generated/stonecutter/client").get().asFile

sourceSets.main {
    java.srcDir("src/main/$loader/java")
    resources.srcDir("src/main/$loader/resources")
    java.srcDir(files(clientSources.resolve("java"), clientSources.resolve("$loader/java"))
        .builtBy("stonecutterGenerateClient"))
    resources.srcDir(files(clientSources.resolve("resources"), clientSources.resolve("$loader/resources"))
        .builtBy("stonecutterGenerateClient"))
    // Written by `:26.2.x:runDatagen`, keyed by Minecraft version rather than by node so both
    // nodes of a Minecraft version share one directory. See src/datagen/java/AGENTS.md.
    resources.srcDir(rootProject.file("src/main/generated/${sc.current.version}"))
}

/**
 * Create's Modrinth version id, set only on the nodes that build the Sand Filter, today `1.21.1-neoforge`.
 * The integration is its own source directory that only such a node compiles, and only there does the
 * manifest name its mixin config. See src/main/create/AGENTS.md.
 */
val createVersion = findProperty("deps.create") as String?

/**
 * The libraries Create bundles inside its jar: Ponder (with Catnip), Flywheel and Registrate. The Sand
 * Filter extends classes whose supertypes live there, so the compiler needs them; at runtime FML reads
 * them out of Create's jar itself.
 */
val createLibraries = createVersion?.let { version ->
    val resolved = configurations.detachedConfiguration(dependencies.create("maven.modrinth:create:$version"))
        .apply { isTransitive = false }
    tasks.register<Sync>("createLibraries") {
        description = "Copies the libraries Create bundles out of its jar, for the compiler"
        from(resolved.elements.map { jars -> jars.map { zipTree(it) } }) {
            include("META-INF/jarjar/*.jar")
            eachFile { path = name }
        }
        includeEmptyDirs = false
        into(layout.buildDirectory.dir("create"))
    }
}

if (createVersion != null) {
    sourceSets.main {
        java.srcDir("src/main/create/java")
        resources.srcDir("src/main/create/resources")
    }
}

/*
 * The same gametests the Fabric nodes run, as their own small mod, so none of it reaches the jar.
 * `src/gametest/neoforge` holds the harness that finds and registers them, in place of Fabric API's;
 * the test classes themselves are shared, and stonecutter.gradle.kts swaps their one Fabric import.
 * See src/gametest/java/AGENTS.md.
 */
val gametest: SourceSet = sourceSets.create("gametest") {
    java.srcDir("src/gametest/$loader/java")
    resources.srcDir("src/gametest/$loader/resources")
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

/*
 * The dev tools, as their own small mod, the same arrangement as the gametests: the agent and
 * `/thirst benchmark`, both of them here. `src/dev/neoforge` holds the entrypoints, the loader seam the
 * agent needs and this loader's `BenchmarkPlayer`, which is the one class of the benchmark that names a
 * loader: it extends NeoForge's `FakePlayer` where the Fabric copy extends Fabric API's.
 * See src/dev/java/AGENTS.md.
 */
val dev: SourceSet = sourceSets.create("dev") {
    java.srcDir("src/dev/$loader/java")
    resources.srcDir("src/dev/$loader/resources")
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

/*
 * The optional mods runClient loads, the same set the Fabric runClient has minus Mod Menu, which is
 * Fabric only. They go on that run alone: on `runtimeOnly` they would load into runServer and
 * runGametest too, and the gametests expect a server without AppleSkin. ModDevGradle's per-run
 * `additionalRuntimeClasspath` would be the place, but it refuses dependencies from Minecraft 26.2 on,
 * and a run's classpath is its source set's runtime classpath. So runClient gets a source set with no
 * sources of its own, whose runtime classpath is `main`'s plus these.
 *
 * The dev tools ride along on both of these, so an agent can drive runServer and every client through
 * run/<node>/agent/<name>/.
 */
val clientRunMods: Configuration = configurations.create("clientRunMods")
/*
 * Added to, never replaced. A source set's own runtime classpath is where ModDevGradle puts DevLaunch,
 * whose `Main` is the class every run is launched through, so a source set that assigns its classpath
 * outright launches nothing: `Could not find or load main class net.neoforged.devlaunch.Main`.
 */
val clientRun: SourceSet = sourceSets.create("clientRun") {
    runtimeClasspath += dev.output + sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath +
        clientRunMods
}
val serverRun: SourceSet = sourceSets.create("serverRun") {
    runtimeClasspath += dev.output + sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
}

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

neoForge {
    version = neoForgeVersion

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
        // NeoForge mod ids cannot contain a hyphen; the Fabric nodes use the same id.
        create("thirstwastaken2_gametest") {
            sourceSet(gametest)
        }
        create(devModId) {
            sourceSet(dev)
        }
    }

    runs {
        // Read out here: inside a run, `project` is the run model's own deprecated accessor.
        val node = project.name
        configureEach {
            // `-Pagent=<file>` answers that file of agent requests once the game is up and then stops
            // it, which is what an unattended run is. Without it the agent is still there, waiting on
            // run/<node>/agent/<name>/in.jsonl. See src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md.
            agentScript?.let {
                systemProperty("thirstwastaken2.agent.script", it)
                systemProperty("thirstwastaken2.agent.script.exit", "true")
            }

            // `-Pdriven` says this client is driven by an agent rather than played: it opens
            // maximised and never takes the mouse pointer, so the desktop stays usable while a
            // script drives it. An unattended `-Pagent=<file>` run implies it.
            if (drivenClient) systemProperty("thirstwastaken2.agent.driven", "true")

            // One run directory per node, for the reason build.gradle.kts gives: a world saved by
            // one Minecraft version is not readable by another, and a failed test run must not
            // leave a broken world behind for runServer. The extra clients get one each as well:
            // two running clients cannot share a directory, and theirs must not touch runClient's.
            // The benchmark falls into the last branch on purpose: it shares runServer's world and
            // accepted EULA, exactly as it does on the Fabric nodes.
            gameDirectory.set(rootProject.file(when (name) {
                "gametest" -> "run/$node/gametest"
                "manualA", "manualB" -> "run/manual-$node-${name.last()}"
                else -> "run/$node"
            }))
        }

        create("client") {
            client()
            sourceSet = clientRun
            // `-Pquickplay=<world>` opens that singleplayer world straight from launch, so an
            // unattended `-Pagent` script starts inside it. The world has to exist in run/<node>/saves.
            providers.gradleProperty("quickplay").orNull?.let { world ->
                programArguments.addAll("--quickPlaySingleplayer", world)
            }
        }
        create("server") {
            server()
            sourceSet = serverRun
        }
        // Unattended benchmark: starts the dedicated server, runs `/thirst benchmark <-Pbenchmark>` from
        // the console once it is up, writes run/<node>/benchmark/latest.json and stops the server. It
        // shares runServer's directory, world and accepted EULA, so the two cannot run at the same time.
        create("benchmark") {
            server()
            sourceSet = serverRun
            systemProperty("thirstwastaken2.benchmark",
                providers.gradleProperty("benchmark").getOrElse("standard"))
            systemProperty("thirstwastaken2.benchmark.exit", "true")
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
                jvmArguments.addAll(flightRecorder(rootProject.file("run/$node/benchmark/latest.jfr")))
            }
        }
        // Two more clients, for the checklist items that need a second player: MANUAL-TESTING.md's
        // "Sync to the client" section, where each player has to see their own bar and no one else's.
        // Two dev clients cannot both be `Dev` on one server, so each is named here, and
        // --quickPlayMultiplayer joins runServer on this machine from the title screen.
        listOf("A", "B").forEach { tester ->
            create("manual$tester") {
                client()
                sourceSet = clientRun
                programArguments.addAll(
                    "--username", "Tester$tester",
                    "--width", "1100", "--height", "700",
                    "--quickPlayMultiplayer", "localhost:25565",
                )
                // Each client answers its own queue, run/manual-<node>-<A|B>/agent/<A|B>/, which is
                // what "each player sees only their own bar" is read out of.
                systemProperty("thirstwastaken2.agent", tester)
            }
        }
        // The GameTest runner: a dedicated server that runs every registered test headlessly, skips
        // the EULA prompt and exits with the number of failed required tests, the same contract as the
        // Fabric runner. `runGametest` is the task name on every node.
        create("gametest") {
            type = "gameTestServer"
            sourceSet = gametest
            // Vanilla's own JUnit report, at the path the Fabric nodes write theirs to, so CI uploads
            // it the same way. The server's --report option arrived with 1.21.5; before it the harness
            // installs the same reporter itself when this property names a file.
            val report = layout.buildDirectory.file("gametest/report.xml").get().asFile.absolutePath
            if (sc.current.parsed >= "1.21.5") programArguments.addAll("--report", report)
            else systemProperty("thirstwastaken2.gametest.report", report)
        }

        // ModDevGradle loads every declared mod by default, and each run only has the classes of the
        // source sets on its own classpath. So each one is told exactly which mods to load: the
        // gametests are their own mod and belong to the gametest run alone, and the dev tools belong
        // to every run an agent may drive.
        val mainMod = mods.named(modId)
        val devMod = mods.named(devModId)
        val gametestMod = mods.named("thirstwastaken2_gametest")
        listOf("client", "server", "benchmark", "manualA", "manualB").forEach { run ->
            named(run) { loadedMods.set(mainMod.zip(devMod) { main, agent -> setOf(main, agent) }) }
        }
        named("gametest") { loadedMods.set(mainMod.zip(gametestMod) { main, tests -> setOf(main, tests) }) }
    }
}

dependencies {
    // Optional integrations the client code compiles against. The mod runs without either; it never
    // takes a hard dependency, and `Loader.isModLoaded` gates every use. Mod Menu is Fabric only, so
    // `ModMenuIntegration` stays in src/client/fabric and the config screen is registered through
    // NeoForge's own IConfigScreenFactory instead.
    compileOnly("maven.modrinth:appleskin:${property("deps.appleskin")}")
    compileOnly("maven.modrinth:jade:${property("deps.jade")}")

    clientRunMods("maven.modrinth:appleskin:${property("deps.appleskin")}")
    clientRunMods("maven.modrinth:jade:${property("deps.jade")}")
    // AppleSkin's own config screen.
    clientRunMods("maven.modrinth:cloth-config:${property("deps.cloth_config")}")

    if (createVersion != null && createLibraries != null) {
        compileOnly("maven.modrinth:create:$createVersion") { isTransitive = false }
        compileOnly(files(createLibraries.map { it.destinationDir.listFiles().orEmpty().toList() })
            .builtBy(createLibraries))
        // Test the Sand Filter with pipes, pumps and spouts in runClient.
        clientRunMods("maven.modrinth:create:$createVersion") { isTransitive = false }
    }
}

/*
 * Datagen runs on Fabric only, so the recipes and advancements it writes use Fabric's spellings for
 * three things NeoForge also has, under other names. This node rewrites them as it copies the files,
 * rather than datagen writing a second copy or NeoForge registering Fabric's names: an alias cannot
 * work, because NeoForge reads the ingredient type from a different key altogether.
 *
 * | Fabric                                                   | NeoForge                                             |
 * |----------------------------------------------------------|------------------------------------------------------|
 * | `fabric:type` `fabric:components`, `base`, `components`  | `neoforge:ingredient_type` `neoforge:components`, `items`, `components` |
 * | `fabric:type` `fabric:any`, `ingredients`                | `neoforge:ingredient_type` `neoforge:compound`, `children` |
 * | `fabric:load_conditions`, `fabric:all_mods_loaded`       | `neoforge:conditions`, one `neoforge:mod_loaded` per mod |
 *
 * Both components ingredients take a `DataComponentPatch` and match a stack that carries at least the
 * listed values, which is NeoForge's default `strict: false`, so `strict` is left out.
 *
 * On 1.21.1 NeoForge 21.1 reads the ingredient type from vanilla's own `type` key, and Fabric writes
 * `base` as a whole ingredient, `{"item": ...}`, where NeoForge's `items` is a holder set, so the item
 * id is taken out of it. Later versions write `base` as the holder set already.
 *
 * Anything else Fabric-specific fails the build here, naming the file, and `checkNeoForgeResources`
 * catches whatever this does not look at. A generator that starts writing a new Fabric shape therefore
 * breaks this node's build rather than loading as a broken recipe.
 */
/** The key NeoForge reads a custom ingredient's type from: vanilla's `type` until it took one of its own. */
val ingredientTypeKey = if (sc.current.parsed > "1.21.1") "neoforge:ingredient_type" else "type"

fun neoForgeJson(node: Any?, file: String): Any? = when (node) {
    is List<*> -> node.map { neoForgeJson(it, file) }
    is Map<*, *> -> when (val type = node["fabric:type"]) {
        null -> node.entries.associate { (key, value) ->
            if (key == "fabric:load_conditions") "neoforge:conditions" to neoForgeConditions(value, file)
            else key as String to neoForgeJson(value, file)
        }
        "fabric:components" -> {
            requireKeys(node, setOf("fabric:type", "base", "components"), file)
            mapOf(ingredientTypeKey to "neoforge:components",
                "items" to neoForgeItems(node["base"], file), "components" to node["components"])
        }
        "fabric:any" -> {
            requireKeys(node, setOf("fabric:type", "ingredients"), file)
            mapOf(ingredientTypeKey to "neoforge:compound",
                "children" to neoForgeJson(node["ingredients"], file))
        }
        else -> throw GradleException("$file: no NeoForge translation for the Fabric ingredient type $type")
    }
    else -> node
}

/**
 * A components ingredient's `base` as the holder set NeoForge's `items` reads: an id, a `#tag` or a list
 * of ids. It already is one from 1.21.2; on 1.21.1 it is a vanilla ingredient, a single item or tag.
 */
fun neoForgeItems(base: Any?, file: String): Any? = when {
    base is String -> base
    base is List<*> && base.all { it is String } -> base
    base is Map<*, *> && base.keys == setOf("item") -> base["item"]
    base is Map<*, *> && base.keys == setOf("tag") -> "#${base["tag"]}"
    else -> throw GradleException("$file: no NeoForge translation for the components ingredient base $base")
}

fun neoForgeConditions(conditions: Any?, file: String): List<Map<String, Any?>> =
    (conditions as List<*>).flatMap { condition ->
        condition as Map<*, *>
        if (condition["condition"] != "fabric:all_mods_loaded") {
            throw GradleException("$file: no NeoForge translation for the Fabric load condition ${condition["condition"]}")
        }
        (condition["values"] as List<*>).map { mapOf("type" to "neoforge:mod_loaded", "modid" to it) }
    }

fun requireKeys(node: Map<*, *>, keys: Set<String>, file: String) {
    val unknown = node.keys - keys
    if (unknown.isNotEmpty()) throw GradleException("$file: no NeoForge translation for $unknown in ${node["fabric:type"]}")
}

/** Written by `:26.2.x:runDatagen`; the only files whose Fabric spellings are translated. */
val generatedResources = rootProject.file("src/main/generated/${sc.current.version}")

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft" to mcRange,
        "neoforge" to neoForgeVersion,
        "java" to requiredJava.majorVersion,
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
    // Datagen's hash cache, which Loom keeps out of the Fabric mod jar and nothing keeps out of
    // this one.
    exclude("**/.cache/**")

    // Only a node that compiles the Sand Filter may name its mixin config, or FML would fail to find it
    // on every other one. It is appended to the built manifest, with Create as an optional dependency,
    // rather than templated into the source manifest every NeoForge node shares.
    inputs.property("create", createVersion ?: "")
    if (createVersion != null) {
        val manifest = destinationDir.resolve("META-INF/neoforge.mods.toml")
        doLast {
            manifest.appendText("""
                |
                |[[mixins]]
                |config = "thirstwastaken2.create.mixins.json"
                |
                |[[dependencies.thirstwastaken2]]
                |modId = "create"
                |type = "optional"
                |ordering = "NONE"
                |side = "BOTH"
                |""".trimMargin())
        }
    }

    // Translates the generated JSON in place, once it is copied. Only files that came from the
    // generated root are read, and only those naming Fabric are rewritten, so the rest keep their
    // bytes. The Copy task copies every file again whenever any input changes, so a translated file
    // is never translated twice.
    val output = destinationDir
    doLast {
        generatedResources.walk().filter { it.isFile && it.extension == "json" }.forEach { source ->
            val relative = source.relativeTo(generatedResources).invariantSeparatorsPath
            val target = output.resolve(relative)
            if (!target.isFile) return@forEach
            val text = target.readText()
            if (!text.contains("\"fabric:")) return@forEach
            val translated = neoForgeJson(groovy.json.JsonSlurper().parseText(text), relative)
            target.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(translated)))
        }
    }
}

// The dev tools have a mixin config of their own, for the one mixin that records where the HUD drew
// the bar. It needs the same compatibility level as the rest, for the same reason: a node on Java 25
// writes class files Mixin refuses to read at level 21.
tasks.named<ProcessResources>("processDevResources") {
    inputs.property("java", requiredJava.majorVersion)
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
}

/**
 * Fails when a Fabric key survives in the processed resources, from datagen or from a hand-written
 * file, because NeoForge would load it as a broken recipe or ignore the condition. CI runs it on the
 * NeoForge job; see the translation above processResources.
 */
tasks.register("checkNeoForgeResources") {
    group = "verification"
    description = "Fails when a Fabric-only JSON key survives into the NeoForge resources"

    val processed = tasks.processResources.map { it.destinationDir }
    inputs.dir(processed)
    val fabricKey = Regex(""""fabric:[^"]*"\s*:""")

    doLast {
        val root = processed.get()
        val offenders = root.walk().filter { it.isFile && it.extension == "json" }.flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> fabricKey.containsMatchIn(line) }
                .map { (index, line) -> "${file.relativeTo(root).invariantSeparatorsPath}:${index + 1}: ${line.trim()}" }
        }.toList()
        check(offenders.isEmpty()) {
            "Fabric keys in the NeoForge resources. Translate the shape in build.neoforge.gradle.kts " +
                "or stop writing it:\n" + offenders.joinToString("\n")
        }
    }
}

// Stonecutter rewrites the versioned comments in `src/` into this node's own source tree, so
// everything that reads those files has to wait for it. Loom needs this for processResources;
// ModDevGradle needs it before it builds the Minecraft artifacts it compiles against.
tasks.named("processResources") { dependsOn("stonecutterGenerate") }

/*
 * Every NeoForge node decompiles and recompiles Minecraft in createMinecraftArtifacts, which takes
 * minutes and gigabytes each. With org.gradle.parallel=true several nodes would do it at once, so a
 * shared build service lets one run at a time. The class is compiled once for this script, so every
 * node that applies it registers the same service.
 */
abstract class CreateMinecraftArtifactsMutex : BuildService<BuildServiceParameters.None>
val createMinecraftArtifactsMutex = gradle.sharedServices
    .registerIfAbsent("createMinecraftArtifactsMutex", CreateMinecraftArtifactsMutex::class) { maxParallelUsages = 1 }

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
    usesService(createMinecraftArtifactsMutex)
}

// Vanilla's reporter writes the file but not the directory it goes in.
tasks.named("runGametest") {
    val reportDir = layout.buildDirectory.dir("gametest")
    doFirst { reportDir.get().asFile.mkdirs() }
}

// The toolchain, the seam checks, the jar excludes and `buildAndCollect` are shared with the Fabric
// nodes. The Java version is passed in because it follows from the node's Minecraft version, which
// only this script can read.
extra["thirst.requiredJava"] = requiredJava.majorVersion
apply(from = rootProject.file("gradle/shared.gradle.kts"))

tasks.jar {
    // The MIT notice of the code this mod is based on has to ship with it, alongside the GPL.
    from(files(rootProject.file("LICENSE"), rootProject.file("CREDITS.md"),
            rootProject.file("licenses/ThirstWasTaken-MIT.txt"))) {
        rename { name -> name.substringBeforeLast('.') + "_$modId" + name.removePrefix(name.substringBeforeLast('.')) }
    }
}

// What this node hands `buildAndCollect`, which gradle/shared.gradle.kts registers: ModDevGradle
// does not remap, so the plain jar is the one that ships.
tasks.named<Copy>("buildAndCollect") {
    from(tasks.jar.flatMap { it.archiveFile }, tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
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
