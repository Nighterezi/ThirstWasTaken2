package com.thirstwastaken2.gametest.neoforge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The gametest mod on NeoForge: finds the same test methods Fabric API finds and registers them the
 * way Fabric API does, so every node runs one list of tests under one set of ids.
 *
 * <ul>
 *   <li><b>Discovery.</b> The test classes are the {@code fabric-gametest} entrypoints in this mod's
 *       own {@code fabric.mod.json}, read as plain JSON. A class added there runs on every node.</li>
 *   <li><b>Ids.</b> {@code thirstwastaken2_gametest:<class>_<method>} in snake case, Fabric API's
 *       rule.</li>
 *   <li><b>Defaults.</b> Fabric API's: an empty 8x8x8 structure, 20 ticks, required, no rotation, one
 *       attempt, padding 1. NeoForge ships no empty structure, so this mod has its own. The
 *       environment is an empty one registered here, the same definition as vanilla's
 *       {@code minecraft:default}, which the registration event gives no way to look up.</li>
 * </ul>
 *
 * <p>This is test harness, not a loader seam: nothing in the mod calls it, and no test body changes
 * for it. See {@code src/gametest/java/AGENTS.md}.
 */
@Mod(ThirstWasTaken2GameTests.MOD_ID)
public final class ThirstWasTaken2GameTests {
    /** NeoForge mod ids cannot contain a hyphen, so both loaders use this one. */
    static final String MOD_ID = "thirstwastaken2_gametest";

    private static final int MAX_TICKS = 20;
    private static final int PADDING = 1;

    private final List<TestMethod> tests;

    public ThirstWasTaken2GameTests(IEventBus modBus, ModContainer container) {
        tests = findTests(container);
        modBus.addListener(this::registerFunctions);
        modBus.addListener(this::registerTests);
    }

    private void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> tests.forEach(test -> helper.register(test.id(), test::run)));
    }

    private void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment =
                event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(environment,
                Identifier.fromNamespaceAndPath(MOD_ID, "empty"), MAX_TICKS, 0, true, Rotation.NONE,
                false, 1, 1, false, PADDING);
        for (TestMethod test : tests) {
            event.registerTest(test.id(), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, test.id()), data));
        }
    }

    private static List<TestMethod> findTests(ModContainer container) {
        JsonObject manifest;
        try (InputStream in = container.getModInfo().getOwningFile().getFile().getContents().openFile("fabric.mod.json")) {
            if (in == null) throw new IllegalStateException(MOD_ID + " has no fabric.mod.json to read its test classes from");
            manifest = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<TestMethod> tests = new ArrayList<>();
        for (var entry : manifest.getAsJsonObject("entrypoints").getAsJsonArray("fabric-gametest")) {
            TestClass testClass = new TestClass(load(entry.getAsString()));
            List<TestMethod> found = new ArrayList<>();
            for (Class<?> type = testClass.type(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GameTest.class)) found.add(new TestMethod(testClass, validate(method)));
                }
            }
            if (found.isEmpty()) throw new IllegalStateException("No @GameTest methods in " + entry.getAsString());
            tests.addAll(found);
        }
        return tests;
    }

    /**
     * Loads a test class without initializing it. Test classes keep the mod's items in static fields,
     * and this runs while the mod is constructed, before NeoForge lets anything register.
     */
    private static Class<?> load(String className) {
        try {
            return Class.forName(className, false, ThirstWasTaken2GameTests.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the test class " + className, e);
        }
    }

    /** Fabric API's own checks, so a test that would not run there does not run here either. */
    private static Method validate(Method method) {
        if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != GameTestHelper.class
                || !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                || method.getReturnType() != void.class) {
            throw new IllegalStateException("Test method " + method.getDeclaringClass().getName() + "#" + method.getName()
                    + " must be public, not static, return void and take one GameTestHelper");
        }
        return method;
    }

    /** One instance per test class, created on its first test, the way Fabric API shares an entrypoint. */
    private static final class TestClass {
        private final Class<?> type;
        private Object instance;

        TestClass(Class<?> type) {
            this.type = type;
        }

        Class<?> type() {
            return type;
        }

        synchronized Object instance() {
            if (instance == null) {
                try {
                    instance = type.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot create the test class " + type.getName(), e);
                }
            }
            return instance;
        }
    }

    private record TestMethod(TestClass testClass, Method method) {
        Identifier id() {
            String name = (testClass.type().getSimpleName() + "_" + method.getName())
                    .replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            return Identifier.fromNamespaceAndPath(MOD_ID, name);
        }

        void run(GameTestHelper helper) {
            try {
                method.invoke(testClass.instance(), helper);
            } catch (InvocationTargetException e) {
                // A failed assertion has to reach the runner as itself, or it reports an error instead.
                if (e.getTargetException() instanceof RuntimeException failure) throw failure;
                throw new RuntimeException("Test method " + method.getName() + " threw", e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
