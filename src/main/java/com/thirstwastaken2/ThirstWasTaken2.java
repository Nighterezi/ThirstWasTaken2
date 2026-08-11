package com.thirstwastaken2;

import com.thirstwastaken2.command.ThirstCommands;
import com.thirstwastaken2.compat.LootIntegration;
import com.thirstwastaken2.compat.createfly.CreateFlyIntegration;
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
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThirstWasTaken2 implements ModInitializer {
    public static final String MOD_ID = "thirstwastaken2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ThirstConfig.load();
        ThirstData.register();
        ThirstComponents.register();
        ThirstItems.register();
        LootIntegration.register();
        if (CreateFlyIntegration.isAvailable()) CreateFlyIntegration.register();

        ServerTickEvents.END_SERVER_TICK.register(ThirstManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(WaterInteractions::tick);
        UseBlockCallback.EVENT.register(ThirstManager::drinkByHand);
        UseBlockCallback.EVENT.register(WaterInteractions::emptyWaterskinOnBlock);
        UseBlockCallback.EVENT.register(WaterInteractions::fillWaterskinFromCauldron);
        UseBlockCallback.EVENT.register(WaterInteractions::transferCauldronPurity);
        UseItemCallback.EVENT.register(WaterInteractions::fillFromWater);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> ThirstCommands.register(dispatcher));

        LOGGER.info("ThirstWasTaken2 initialized for Minecraft 26.2");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
