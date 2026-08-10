package com.thirstwastaken;

import com.thirstwastaken.command.ThirstCommands;
import com.thirstwastaken.data.ThirstData;
import com.thirstwastaken.data.ThirstManager;
import com.thirstwastaken.item.ThirstItems;
import com.thirstwastaken.config.ThirstConfig;
import com.thirstwastaken.purity.ThirstComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import com.thirstwastaken.purity.WaterInteractions;
import com.thirstwastaken.compat.LootIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThirstWasTaken implements ModInitializer {
    public static final String MOD_ID = "thirstwastaken";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ThirstConfig.load();
        ThirstData.register();
        ThirstComponents.register();
        ThirstItems.register();
        LootIntegration.register();
        if (FabricLoader.getInstance().isModLoaded("create"))
            com.thirstwastaken.compat.createfly.CreateFlyIntegration.register();
        ServerTickEvents.END_SERVER_TICK.register(ThirstManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(WaterInteractions::tick);
        UseBlockCallback.EVENT.register(ThirstManager::drinkByHand);
        UseBlockCallback.EVENT.register(WaterInteractions::transferCauldronPurity);
        UseItemCallback.EVENT.register(WaterInteractions::fillBowl);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> ThirstCommands.register(dispatcher));
        LOGGER.info("Thirst Was Taken Fabric initialized for Minecraft 26.2");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
