plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project

version = mod_version
group = maven_group

base {
    archivesName.set(archives_base_name)
}

repositories {
    // Loom supplies the Minecraft and Fabric repositories.
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
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
}

dependencies {
    "minecraft"(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    // Optional config-screen entry point; the mod works without Mod Menu installed.
    "clientCompileOnly"(libs.modmenu)
    // Test the client HUD and food tooltips alongside AppleSkin in runClient.
    "clientCompileOnly"(libs.appleskin)
    "clientRuntimeOnly"(libs.appleskin)
    // AppleSkin uses Cloth Config for its Mod Menu configuration screen.
    "clientRuntimeOnly"(libs.cloth.config)
    "clientRuntimeOnly"(libs.modmenu)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
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
    options.release.set(25)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories { }
}
