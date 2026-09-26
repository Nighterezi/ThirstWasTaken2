package com.thirstwastaken2.culturaldelights;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Whether the Cultural Delights installed has the vat, and whether the vat still has each method a mixin
 * targets.
 *
 * <p>Names no Cultural Delights class and no Minecraft class, so the mixin plugin can ask before anything
 * of the mod's is loaded. It reads FML's list of discovered mod files, which is complete before any mixin
 * config is read, and reads class files without loading them.
 */
public final class CulturalDelightsPresence {
    public static final String MOD_ID = "culturaldelights";
    /** The vat's block entity. Only 0.18 has one, so an older jar under the same id is not enough. */
    private static final String MARKER = "com/baisylia/culturaldelights/block/entity/custom/VatBlockEntity.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;
    private static final Map<String, Boolean> METHODS = new ConcurrentHashMap<>();

    private CulturalDelightsPresence() { }

    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            ModFileInfo culturalDelights = LoadingModList.get().getModFileById(MOD_ID);
            known = culturalDelights != null && Files.exists(culturalDelights.getFile().findResource(MARKER));
            if (culturalDelights != null && !known) {
                LOGGER.info("Cultural Delights is installed without the vat of 0.18; the vat integration is off");
            }
            present = known;
        }
        return known;
    }

    /**
     * Whether {@code targetClassName}, a binary name, declares a method called {@code method}: read off
     * the class file, which never loads the class. Asked once per mixin, at startup, and logged when the
     * class is there but the method is not, which is an upstream change the integration has to follow.
     */
    public static boolean hasMethod(String targetClassName, String method) {
        if (!isPresent()) return false;
        return METHODS.computeIfAbsent(targetClassName + '#' + method, key -> readMethod(targetClassName, method));
    }

    private static boolean readMethod(String targetClassName, String method) {
        String resource = targetClassName.replace('.', '/') + ".class";
        try (InputStream in = CulturalDelightsPresence.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                LOGGER.warn("Cultural Delights has no {}, so it is not a version ThirstWasTaken2 supports", targetClassName);
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
                LOGGER.warn("Cultural Delights has no {}.{}, so it is not a version ThirstWasTaken2 supports; "
                        + "its vat may brew from sea water", targetClassName, method);
            }
            return found[0];
        } catch (IOException e) {
            LOGGER.warn("Could not read {} to look for {}; leaving that part of the integration off", targetClassName, method, e);
            return false;
        }
    }
}
