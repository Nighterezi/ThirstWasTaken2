package com.thirstwastaken.client;

import com.thirstwastaken.ThirstWasTaken;
import com.thirstwastaken.data.ThirstData;
import com.thirstwastaken.data.ThirstManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ThirstHud {
    private static final Identifier ICONS = ThirstWasTaken.id("textures/gui/thirst_icons.png");
    private static final Identifier OVERLAY_ICONS = ThirstWasTaken.id("textures/gui/appleskin_icons.png");
    private static final RandomSource RANDOM = RandomSource.create();

    private ThirstHud() { }

    public static boolean shouldRender(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        return player != null && player.isAlive() && ThirstManager.get(player).enabled()
                && !(player.getVehicle() instanceof LivingEntity) && !minecraft.gui.hud.isHidden();
    }

    public static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (!shouldRender(player)) return;

        ThirstData data = ThirstManager.get(player);
        int left = graphics.guiWidth() / 2 + 91;
        int top = graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(ThirstWasTaken.id("thirst_bar"));

        int exhaustionWidth = Math.min(81, Math.max(0, (int) (data.exhaustion() / 4.0F * 81.0F)));
        if (exhaustionWidth > 0)
            graphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_ICONS, left - exhaustionWidth, top,
                    81 - exhaustionWidth, 18, exhaustionWidth, 9, 256, 256);

        for (int i = 0; i < 10; i++) {
            int index = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            if (data.quenched() <= 0 && data.thirst() > 0 && player.tickCount % (data.thirst() * 3 + 1) == 0) {
                y += RANDOM.nextInt(3) - 1;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, 0, 0, 9, 9, 25, 9);
            if (index < data.thirst()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, 16, 0, 9, 9, 25, 9);
            } else if (index == data.thirst()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, 8, 0, 9, 9, 25, 9);
            }

            float effectiveQuenched = data.quenched() / 2.0F - i;
            if (effectiveQuenched > 0) {
                int u = effectiveQuenched >= 1 ? 27 : effectiveQuenched > 0.5F ? 18 : effectiveQuenched > 0.25F ? 9 : 0;
                graphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_ICONS, x, y, u, 0, 9, 9, 256, 256);
            }
        }
    }
}
