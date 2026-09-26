package com.thirstwastaken2.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code Runnable} that an integration compiled by both loaders runs once the mod has
 * initialized, since such an integration may name neither loader's entrypoint API. Fabric finds it
 * through the {@code thirstwastaken2:integration} entrypoint, which the integration table writes into
 * {@code fabric.mod.json}; NeoForge by this annotation, in the mod's scan data. Either way the class is
 * constructed with no arguments and run on the main thread, after {@code ThirstWasTaken2.initialize}.
 *
 * <p>It is a root for {@code checkOptionalSeam}: it names nothing of the mod it serves, and hands over
 * through a static call after asking whether that mod is installed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IntegrationEntrypoint {
}
