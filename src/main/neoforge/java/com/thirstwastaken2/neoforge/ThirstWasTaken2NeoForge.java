package com.thirstwastaken2.neoforge;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.platform.IntegrationEntrypoint;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

import java.lang.annotation.ElementType;

/**
 * The NeoForge mod class. Everything it starts is loader independent, apart from the fluid capability
 * that lets other mods fill and empty the waterskin and the bowls.
 */
@Mod(ThirstWasTaken2.MOD_ID)
public final class ThirstWasTaken2NeoForge {
    public ThirstWasTaken2NeoForge(IEventBus modBus) {
        ThirstWasTaken2.initialize();
        modBus.addListener(WaterContainerCapabilities::register);
        runIntegrationEntrypoints();
    }

    /**
     * The integrations both loaders compile, which may not carry {@code @Mod}; Fabric reaches the same
     * classes through the {@code thirstwastaken2:integration} entrypoint. Only the nodes that compile an
     * integration have its class in the scan data.
     */
    private static void runIntegrationEntrypoints() {
        ModList.get().getModFileById(ThirstWasTaken2.MOD_ID).getFile().getScanResult()
                .getAnnotatedBy(IntegrationEntrypoint.class, ElementType.TYPE)
                .map(annotation -> annotation.clazz().getClassName())
                .sorted()
                .forEach(ThirstWasTaken2NeoForge::run);
    }

    private static void run(String className) {
        try {
            Class<?> type = Class.forName(className, true, ThirstWasTaken2NeoForge.class.getClassLoader());
            ((Runnable) type.getDeclaredConstructor().newInstance()).run();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not run the integration entrypoint " + className, e);
        }
    }
}
