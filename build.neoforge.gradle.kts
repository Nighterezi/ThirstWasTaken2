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
 * `src/gametest`, `src/dev` and `src/datagen` are absent on purpose: the gametest harness is P4's
 * step 6, and the dev tools and the generators stay Fabric only. This node reads the datapack and
 * asset JSON that the 26.2 Fabric node writes.
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

neoForge {
    version = neoForgeVersion

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
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

        create("client") { client() }
        create("server") { server() }
        // The GameTest runner: a dedicated server that runs every registered test headlessly and
        // exits with the number of failures. The harness that registers them is P4's step 6, so
        // this run has nothing to run yet.
        create("gametest") { type = "gameTestServer" }
    }
}

dependencies {
    // Optional integrations the client code compiles against. The mod runs without either; it never
    // takes a hard dependency, and `Loader.isModLoaded` gates every use. Mod Menu is Fabric only, so
    // `ModMenuIntegration` stays in src/client/fabric and the config screen is registered through
    // NeoForge's own IConfigScreenFactory instead.
    //
    // Nothing is on the run classpath yet: putting AppleSkin and Jade, both client mods, on a plain
    // `runtimeOnly` would load them into runServer as well. The client run gets them in P4's step 5,
    // alongside Cloth Config, which AppleSkin's own config screen needs.
    compileOnly("maven.modrinth:appleskin:${property("deps.appleskin")}")
    compileOnly("maven.modrinth:jade:${property("deps.jade")}")
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft" to mcRange,
        "neoforge" to neoForgeVersion,
        "java" to requiredJava.majorVersion,
    )
    inputs.properties(props)
    // The manifest arrives in P4's step 4; `filesMatching` is a no-op until it does.
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
    // Datagen's hash cache, which Loom keeps out of the Fabric mod jar and nothing keeps out of
    // this one.
    exclude("**/.cache/**")
}

// Stonecutter rewrites the versioned comments in `src/` into this node's own source tree, so
// everything that reads those files has to wait for it. Loom needs this for processResources;
// ModDevGradle needs it before it builds the Minecraft artifacts it compiles against.
tasks.named("processResources") { dependsOn("stonecutterGenerate") }
tasks.named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }

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

// Temporary, until P4's step 3 gives this node a Loader of its own: it cannot compile before then.
// A task run on every node at once, such as `./gradlew buildAndCollect`, skips this node rather than
// failing on it; a task that names it, such as `:26.2.x-neoforge:compileJava`, still runs. Step 3
// deletes this block.
val namedOnCommandLine = gradle.startParameter.taskNames.any { it.startsWith(":${project.name}:") }
if (!namedOnCommandLine) {
    tasks.configureEach { enabled = false }
}
