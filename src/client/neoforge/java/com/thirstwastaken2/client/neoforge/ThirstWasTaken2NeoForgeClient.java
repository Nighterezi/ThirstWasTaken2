package com.thirstwastaken2.client.neoforge;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.client.ThirstWasTaken2Client;
import com.thirstwastaken2.client.config.ThirstConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * The NeoForge client mod class, the counterpart of the Fabric {@code client} entrypoint. Everything it
 * starts is loader independent. It also hands NeoForge's mods list the config screen, which Mod Menu
 * finds through its own entrypoint on Fabric.
 */
@Mod(value = ThirstWasTaken2.MOD_ID, dist = Dist.CLIENT)
public final class ThirstWasTaken2NeoForgeClient {
    public ThirstWasTaken2NeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (mod, parent) -> new ThirstConfigScreen(parent));
        ThirstWasTaken2Client.initialize();
    }
}
