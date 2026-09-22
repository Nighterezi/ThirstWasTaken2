package com.thirstwastaken2.kaleidoscope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Whether Kaleidoscope Cookery is installed, is a build the integration was written against, and still
 * has each class a mixin targets.
 *
 * <p>Names no Kaleidoscope Cookery class, no Minecraft class and no loader, so the mixin plugin can ask
 * before anything a mixin targets is loaded: every answer here is a resource lookup, which never loads a
 * class. {@code Class.forName} would load a target before Mixin has transformed it. Both loaders compile
 * this directory, which is why it probes the classpath rather than asking a mod list.
 *
 * <p>The teapot is the version check. The official Fabric build stopped at 1.0.1, under the same mod id
 * and package as Refabricated but without a teapot, so a Kaleidoscope Cookery with no teapot is that
 * build, and the whole integration stays off. Past that, each target is probed on its own, so a class
 * renamed upstream skips the mixins on it and leaves the rest working.
 */
public final class KaleidoscopePresence {
    private static final String MOD = "com/github/ysbbbbbb/kaleidoscopecookery/KaleidoscopeCookery.class";
    private static final String TEAPOT =
            "com/github/ysbbbbbb/kaleidoscopecookery/blockentity/kitchen/TeapotBlockEntity.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean supported;
    private static final Map<String, Boolean> TARGETS = new ConcurrentHashMap<>();

    private KaleidoscopePresence() { }

    /** Whether a Kaleidoscope Cookery the integration supports is installed. Warns once if one is not. */
    public static boolean isSupported() {
        Boolean known = supported;
        if (known == null) {
            known = has(TEAPOT);
            if (!known && has(MOD)) {
                LOGGER.warn("Kaleidoscope Cookery is installed but has no teapot, so it is the official Fabric "
                        + "build, which ThirstWasTaken2 does not support; install Kaleidoscope Cookery "
                        + "Refabricated instead. Its drinks and soups still restore thirst, but water poured "
                        + "into a stockpot comes back out without its grade");
            }
            supported = known;
        }
        return known;
    }

    /**
     * Whether a mixin on {@code targetClassName}, a binary name as Mixin passes it, may be applied: the
     * mod is supported and still has that class. A missing target is logged once and skipped.
     */
    public static boolean hasTarget(String targetClassName) {
        if (!isSupported()) return false;
        return TARGETS.computeIfAbsent(targetClassName, name -> {
            boolean found = has(name.replace('.', '/') + ".class");
            if (!found) {
                LOGGER.warn("Kaleidoscope Cookery has no {}, so it is not a version ThirstWasTaken2 supports; "
                        + "what the integration does there is disabled", name);
            }
            return found;
        });
    }

    private static boolean has(String resource) {
        return KaleidoscopePresence.class.getClassLoader().getResource(resource) != null;
    }
}
