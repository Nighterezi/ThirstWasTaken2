package com.thirstwastaken2.client;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.client.platform.ClientLoader;

public final class ThirstWasTaken2Client {
    /** One droplet row, as tall as vanilla's hunger bar. */
    private static final int THIRST_BAR_HEIGHT = 10;

    private ThirstWasTaken2Client() { }

    /** Called once by the loader's client entrypoint. */
    public static void initialize() {
        ClientLoader.addRightStatusBar(ThirstWasTaken2.id("thirst_bar"), THIRST_BAR_HEIGHT,
                ThirstHud::shouldRender, ThirstHud::render);
    }
}
