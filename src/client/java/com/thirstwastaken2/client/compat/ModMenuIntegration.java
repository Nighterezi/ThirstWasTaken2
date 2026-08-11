package com.thirstwastaken2.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thirstwastaken2.client.config.ThirstConfigScreen;

/**
 * Registers the config screen with Mod Menu. Mod Menu is a compile-only dependency, so this class is
 * only ever loaded when Mod Menu itself resolves the {@code modmenu} entrypoint.
 */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ThirstConfigScreen::new;
    }
}
