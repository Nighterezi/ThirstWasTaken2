plugins {
    // NeoForge's build plugin. Its version is on the plugin classpath from settings.gradle.kts,
    // which resolves it without applying it.
    id("net.neoforged.moddev")
    `maven-publish`
}

/*
 * The NeoForge node, `26.2.x-neoforge`. One Minecraft version, one loader, the same sources as
 * `26.2.x`. Everything both this and the Fabric nodes need is in gradle/shared.gradle.kts, applied
 * below. See docs/dev/P4-NEOFORGE-PLAN.md.
 */

// Stonecutter supplies `mod.*` and `deps.*` for this node from stonecutter.properties.toml.
// `group` stays unset for the same reason build.gradle.kts leaves it unset; the publication sets
// its own groupId below.
val modId = property("mod.id") as String
/** Published artifact name; deliberately not the lowercase mod id. */
val modName = property("mod.name") as String
val modGroup = property("mod.group") as String
/** Minecraft range written into neoforge.mods.toml, in Maven range syntax, e.g. `[26.2,26.3)`. */
val mcRange = property("mod.mc_range") as String
val neoForgeVersion = property("deps.neoforge") as String

// The loader goes in the version so the jar is `ThirstWasTaken2-1.0.5+26.2-neoforge.jar` and never
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
 * `src/dev` and `src/datagen` are absent on purpose: the dev tools and the generators stay Fabric
 * only. This node reads the datapack and asset JSON that the 26.2 Fabric node writes. `src/gametest`
 * is here, as a mod of its own, below.
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
 * The optional mods runClient loads, the same set the Fabric runClient has minus Mod Menu, which is
 * Fabric only. They go on that run alone: on `runtimeOnly` they would load into runServer and
 * runGametest too, and the gametests expect a server without AppleSkin. ModDevGradle's per-run
 * `additionalRuntimeClasspath` would be the place, but it refuses dependencies from Minecraft 26.2 on,
 * and a run's classpath is its source set's runtime classpath. So runClient gets a source set with no
 * sources of its own, whose runtime classpath is `main`'s plus these.
 */
val clientRunMods: Configuration = configurations.create("clientRunMods")
val clientRun: SourceSet = sourceSets.create("clientRun") {
    runtimeClasspath = sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath + clientRunMods
}

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
    }

    runs {
        // Read out here: inside a run, `project` is the run model's own deprecated accessor.
        val node = project.name
        configureEach {
            // One run directory per node, for the reason build.gradle.kts gives: a world saved by
            // one Minecraft version is not readable by another, and a failed test run must not
            // leave a broken world behind for runServer.
            gameDirectory.set(rootProject.file(if (name == "gametest") "run/$node/gametest" else "run/$node"))
        }

        create("client") {
            client()
            sourceSet = clientRun
        }
        create("server") { server() }
        // The GameTest runner: a dedicated server that runs every registered test headlessly, skips
        // the EULA prompt and exits with the number of failed required tests, the same contract as the
        // Fabric runner. `runGametest` is the task name on every node.
        create("gametest") {
            type = "gameTestServer"
            sourceSet = gametest
            // Vanilla's own JUnit report, at the path the Fabric nodes write theirs to, so CI uploads
            // it the same way.
            programArguments.addAll("--report", layout.buildDirectory.file("gametest/report.xml").get().asFile.absolutePath)
        }

        // Only the gametest run loads the gametest mod. ModDevGradle loads every mod by default.
        val mainMod = mods.named(modId)
        named("client") { loadedMods.set(mainMod.map { setOf(it) }) }
        named("server") { loadedMods.set(mainMod.map { setOf(it) }) }
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
 * Anything else Fabric-specific fails the build here, naming the file, and `checkNeoForgeResources`
 * catches whatever this does not look at. A generator that starts writing a new Fabric shape therefore
 * breaks this node's build rather than loading as a broken recipe.
 */
fun neoForgeJson(node: Any?, file: String): Any? = when (node) {
    is List<*> -> node.map { neoForgeJson(it, file) }
    is Map<*, *> -> when (val type = node["fabric:type"]) {
        null -> node.entries.associate { (key, value) ->
            if (key == "fabric:load_conditions") "neoforge:conditions" to neoForgeConditions(value, file)
            else key as String to neoForgeJson(value, file)
        }
        "fabric:components" -> {
            requireKeys(node, setOf("fabric:type", "base", "components"), file)
            mapOf("neoforge:ingredient_type" to "neoforge:components",
                "items" to node["base"], "components" to node["components"])
        }
        "fabric:any" -> {
            requireKeys(node, setOf("fabric:type", "ingredients"), file)
            mapOf("neoforge:ingredient_type" to "neoforge:compound",
                "children" to neoForgeJson(node["ingredients"], file))
        }
        else -> throw GradleException("$file: no NeoForge translation for the Fabric ingredient type $type")
    }
    else -> node
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
tasks.named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }

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
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modId" }
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
