package com.thirstwastaken2.dev.fabric;

import com.thirstwastaken2.dev.ThirstDev;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * The dev tools mod's main entrypoint on Fabric: {@code /thirst benchmark}, and the agent on a
 * dedicated server.
 *
 * <p>Both tools are installed from {@link ThirstDev}, which names no loader; this class is only the
 * loader's way in. Fabric runs this entrypoint on a client as well, where the agent belongs to
 * {@link ThirstDevFabricClient} instead, so the server-side install is skipped there and the
 * benchmark, which a client's integrated server can run too, is not.
 */
public final class ThirstDevFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!ThirstDev.enabled()) return;
        ThirstDev.installBenchmark();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) ThirstDev.initializeServer();
    }
}
