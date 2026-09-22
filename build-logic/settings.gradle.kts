/*
 * The build code both loader scripts share, as plain Kotlin: data and pure functions only. It depends
 * on nothing but Gradle's own Kotlin, never on Loom, ModDevGradle or Stonecutter, and never knows which
 * node is active; the loader scripts pass that in and make every `sourceSets`, `loom` and `neoForge`
 * call themselves. See docs/dev/BUILD-STRUCTURE-PLAN.md, item 2.
 */
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "build-logic"
