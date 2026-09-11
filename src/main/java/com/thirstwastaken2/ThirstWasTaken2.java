package com.thirstwastaken2;

import com.thirstwastaken2.command.ThirstCommands;
import com.thirstwastaken2.compat.LootIntegration;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterInteractions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThirstWasTaken2 implements ModInitializer {
    public static final String MOD_ID = "thirstwastaken2";
    /** The Minecraft version this jar was built against; substituted per version at build time. */
    public static final String MINECRAFT = /*$ minecraft*/ "26.2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** System property that forces {@link #DEV} on or off. */
    public static final String DEV_PROPERTY = "thirstwastaken2.dev";
    /**
     * Whether this is a development run rather than a published jar: true under every Loom run task
     * ({@code runServer}, {@code runClient}, {@code runGametest}, {@code runBenchmark}), false in the jar
     * players install. {@code -Dthirstwastaken2.dev=true|false} overrides the detection either way.
     *
     * <p>Dev-only tooling such as {@code /thirst benchmark} registers nothing unless this is set. It also
     * lives in the separate {@code dev} source set, so it cannot reach a published jar even when the flag
     * is forced on.
     */
    public static final boolean DEV = detectDev();

    @Override
    public void onInitialize() {
        ThirstConfig.load();
        ThirstData.register();
        ThirstComponents.register();
        ThirstItems.register();
        LootIntegration.register();

        ServerTickEvents.END_SERVER_TICK.register(ThirstManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(WaterInteractions::tick);
        UseBlockCallback.EVENT.register(ThirstManager::drinkByHand);
        UseBlockCallback.EVENT.register(WaterInteractions::emptyWaterskinOnBlock);
        UseBlockCallback.EVENT.register(WaterInteractions::fillWaterskinFromCauldron);
        UseBlockCallback.EVENT.register(WaterInteractions::transferCauldronPurity);
        UseItemCallback.EVENT.register(WaterInteractions::fillFromWater);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> ThirstCommands.register(dispatcher));

        LOGGER.info("ThirstWasTaken2 initialized for Minecraft {}{}", MINECRAFT, DEV ? " (dev)" : "");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static boolean detectDev() {
        String forced = System.getProperty(DEV_PROPERTY);
        return forced != null ? Boolean.parseBoolean(forced) : FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
