package com.thirstwastaken2.dev.agent.thirst;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * What a client that is being driven rather than played does with its window and the mouse pointer.
 *
 * <p>A driven client is one an agent sends requests to while the person who launched it is doing
 * something else on the same desktop. Two things about a played client get in the way of that, and
 * both are turned around by {@code -Pdriven}:
 *
 * <ul>
 *   <li><b>The pointer.</b> Minecraft takes the cursor as soon as the window is focused with a world
 *       open, and holds it inside the frame until a screen opens. Clicking the window to glance at
 *       the bar then costs the pointer everywhere else on the desktop. A driven client needs none of
 *       it: every key the agent holds goes through the game's own key state and every command goes
 *       through the player's connection, so nothing here is ever steered by a physical mouse.
 *       {@code MouseGrabMixin} refuses the grab outright rather than releasing it a tick later, so
 *       the cursor is never hidden and never warped to the middle of the window.</li>
 *   <li><b>The window size.</b> The run tasks open a small window, which is right for two clients
 *       side by side and wrong for reading a HUD. A driven one is maximised instead. Maximised, not
 *       full screen: exclusive full screen takes the whole display and is exactly what a person
 *       working beside it does not want.</li>
 * </ul>
 *
 * <p>It is off by default because it is the opposite of what a manual pass needs: without the grab
 * there is no mouse look and no click reaching the world, so a person, or computer use standing in
 * for one, cannot play the client at all. An unattended run turns it on by itself, since
 * {@code -Pagent=<file>} means nobody is at the keyboard.
 */
public final class ClientWindow {
    /**
     * Read once, from a system property rather than from anything the game holds, because
     * {@code MouseGrabMixin} asks before the mod has initialized.
     */
    private static final boolean DRIVEN =
            Boolean.parseBoolean(System.getProperty("thirstwastaken2.agent.driven", "false"))
                    || System.getProperty("thirstwastaken2.agent.script") != null;

    private ClientWindow() { }

    /** Whether this client is being driven by an agent rather than played by a person. */
    public static boolean driven() { return DRIVEN; }

    /**
     * Maximises the window and gives the pointer back, once, on the first client tick. The release is
     * not redundant with the mixin: a client started with {@code --quickPlayMultiplayer} can be in a
     * world with the cursor already taken before this runs, and the mixin only refuses the next grab.
     */
    public static void open(Minecraft minecraft) {
        if (!DRIVEN) return;
        GLFW.glfwMaximizeWindow(AgentClientVanilla.windowHandle(minecraft));
        if (minecraft.mouseHandler.isMouseGrabbed()) minecraft.mouseHandler.releaseMouse();
    }
}
