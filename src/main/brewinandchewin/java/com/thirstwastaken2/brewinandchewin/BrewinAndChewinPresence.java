package com.thirstwastaken2.brewinandchewin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Whether Brewin' and Chewin' is installed and still has each class and method a mixin targets.
 *
 * <p>Names no Brewin' and Chewin' class, no Minecraft class and no loader, so the mixin plugin can ask
 * before anything a mixin targets is loaded: every answer here is a resource lookup or a read of a class
 * file, neither of which loads a class. Both loaders compile this directory, which is why it probes the
 * classpath rather than asking a mod list.
 *
 * <p>Each target is probed on its own, so a class renamed upstream skips the mixins on it and leaves the
 * rest working. The keg's water all goes through one private method, {@code fluidExtract}, the likeliest
 * thing to move, so a mixin on it asks {@link #hasMethod} as well.
 */
public final class BrewinAndChewinPresence {
    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static final Map<String, Boolean> TARGETS = new ConcurrentHashMap<>();

    private BrewinAndChewinPresence() { }

    /**
     * Whether a mixin on {@code targetClassName}, a binary name as Mixin passes it, may be applied. A
     * missing target is logged once and skipped; with the mod absent, every target is, silently.
     */
    public static boolean hasTarget(String targetClassName) {
        return TARGETS.computeIfAbsent(targetClassName, name -> has(name.replace('.', '/') + ".class"));
    }

    /**
     * Whether {@code targetClassName}, a binary name, declares a method called {@code method}: read off
     * the class file, which never loads the class. Asked once per mixin, at startup, and logged when the
     * class is there but the method is not, which is an upstream change the integration has to follow.
     */
    public static boolean hasMethod(String targetClassName, String method) {
        if (!hasTarget(targetClassName)) return false;
        String resource = targetClassName.replace('.', '/') + ".class";
        try (InputStream in = BrewinAndChewinPresence.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return false;
            boolean[] found = {false};
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (name.equals(method)) found[0] = true;
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            if (!found[0]) {
                LOGGER.warn("Brewin' and Chewin' has no {}.{}, so it is not a version ThirstWasTaken2 supports; "
                        + "water poured into a keg loses its grade", targetClassName, method);
            }
            return found[0];
        } catch (IOException e) {
            LOGGER.warn("Could not read {} to look for {}; leaving that part of the integration off", targetClassName, method, e);
            return false;
        }
    }

    private static boolean has(String resource) {
        return BrewinAndChewinPresence.class.getClassLoader().getResource(resource) != null;
    }
}
