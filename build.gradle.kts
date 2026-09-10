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

loom {
    splitEnvironmentSourceSets()

    mods {
        register("thirstwastaken2") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }

    runConfigs.all {
        // One run directory per version. Sharing a single one would hand a 26.2 world to a 1.21.11
        // server, which fails on world format rather than on anything the mod did.
        runDirectory = rootProject.file("run/${project.name}")
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

// Per-directory notes for contributors; they live next to the files they describe, not in the jars.
tasks.withType<ProcessResources>().configureEach {
    exclude("**/AGENTS.md")
}

// Registered lazily: withSourcesJar() below adds the task after this block is evaluated.
tasks.withType<Jar>().matching { it.name.endsWith("sourcesJar") }.configureEach {
    exclude("**/AGENTS.md")
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
