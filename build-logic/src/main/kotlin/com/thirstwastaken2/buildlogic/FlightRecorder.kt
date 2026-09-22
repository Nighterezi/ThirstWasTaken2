package com.thirstwastaken2.buildlogic

import java.io.File

/**
 * The JVM arguments that record a run with JFR, which `-Pprofile` adds to runBenchmark on both loaders.
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
