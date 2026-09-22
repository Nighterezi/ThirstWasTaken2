package com.thirstwastaken2.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Does nothing when applied. `id("thirstwastaken2.build-logic")` in a script's `plugins { }` is how
 * that script gets [integrations], [OptionalRunMods] and [flightRecorder] on its classpath.
 */
class BuildLogicPlugin : Plugin<Project> {
    override fun apply(target: Project) { }
}
