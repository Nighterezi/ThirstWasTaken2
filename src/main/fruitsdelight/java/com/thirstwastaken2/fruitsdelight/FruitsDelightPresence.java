package com.thirstwastaken2.fruitsdelight;

import net.neoforged.fml.loading.LoadingModList;
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
 * Whether Fruits Delight is installed, and whether each class a mixin targets still has its method.
 *
 * <p>Names no class of Fruits Delight's or L2 Core's and no Minecraft class, so the mixin plugin can ask
 * before anything they hold is loaded. It reads FML's list of discovered mods, which is complete before
 * any mixin config is read, and reads class files without loading them. L2 Core is nested in Fruits
 * Delight's jar and is not a mod file of its own there, which is why a target is looked up on the
 * classpath rather than in a mod's jar.
 */
public final class FruitsDelightPresence {
    public static final String MOD_ID = "fruitsdelight";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;
    private static final Map<String, Boolean> METHODS = new ConcurrentHashMap<>();

    private FruitsDelightPresence() { }

    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            known = LoadingModList.get().getModFileById(MOD_ID) != null;
            present = known;
        }
        return known;
    }

    /**
     * Whether {@code targetClassName}, a binary name, declares a method called {@code method}: read off
     * the class file, which never loads the class. Asked once per mixin, at startup, and logged when the
     * class or the method is gone, which is an upstream change the integration has to follow.
     */
    public static boolean hasMethod(String targetClassName, String method) {
        if (!isPresent()) return false;
        return METHODS.computeIfAbsent(targetClassName + '#' + method, key -> readMethod(targetClassName, method));
    }

    private static boolean readMethod(String targetClassName, String method) {
        String resource = targetClassName.replace('.', '/') + ".class";
        try (InputStream in = FruitsDelightPresence.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                LOGGER.warn("Fruits Delight is installed without {}, so it is not a version ThirstWasTaken2 supports; "
                        + "sea water may make its juice", targetClassName);
                return false;
            }
            boolean[] found = {false};
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (name.equals(method)) found[0] = true;
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            if (!found[0]) {
                LOGGER.warn("{} has no {}, so it is not a version ThirstWasTaken2 supports; "
                        + "sea water may make Fruits Delight's juice", targetClassName, method);
            }
            return found[0];
        } catch (IOException e) {
            LOGGER.warn("Could not read {} to look for {}; leaving that part of the integration off", targetClassName, method, e);
            return false;
        }
    }
}
