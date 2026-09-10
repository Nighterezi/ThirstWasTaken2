pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    // One source tree, one jar per Minecraft version. See the root AGENTS.md.
    id("dev.kikugie.stonecutter") version "0.9.8"
    // 26.1 dropped obfuscation, which changed how Loom consumes mod dependencies. This applies the
    // Loom variant each version needs and keeps `modImplementation` meaning the same thing on both.
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    // Provisions the JDK a version needs when it is not installed locally: 26.1+ wants Java 25,
    // 1.21.11 wants Java 21.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.11")
        version("26.1.x", "26.1.2")
        version("26.2.x", "26.2")
        vcsVersion = "26.2.x"
    }
}

rootProject.name = "ThirstWasTaken2"
