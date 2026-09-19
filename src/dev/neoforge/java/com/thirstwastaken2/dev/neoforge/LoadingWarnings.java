package com.thirstwastaken2.dev.neoforge;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.agent.thirst.ClientWindow;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries a driven client past NeoForge's "warnings while loading mods" screen when none of the
 * warnings is this mod's.
 *
 * <p>Other mods' warnings, a deprecated {@code logoFile} in Cloth Config or AppleSkin's old translation
 * key, stop the client on that screen before the world opens, so an unattended {@code -Pagent} run or a
 * {@code -Pquickplay} client waits on a button nobody is there to press. Each warning passed over is
 * written to the log. The screen stays when there is an error, when a warning names this mod, and when
 * a warning names no mod at all, since then nothing says it is someone else's.
 *
 * <p>The screen keeps its issues and its continuation private and has no event of its own, so they
 * are read by reflection. It is the same class, with the same fields, on every NeoForge node.
 */
final class LoadingWarnings {
    private LoadingWarnings() { }

    static void register() {
        if (!ClientWindow.driven()) return;
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            if (event.getScreen() instanceof LoadingErrorScreen screen) passIfNotOurs(screen);
        });
    }

    private static void passIfNotOurs(LoadingErrorScreen screen) {
        try {
            if (!issues(screen, "modLoadErrors").isEmpty()) return;
            List<ModLoadingIssue> warnings = issues(screen, "modLoadWarnings");
            for (ModLoadingIssue warning : warnings) {
                if (!isSomeoneElses(warning)) {
                    ThirstWasTaken2.LOGGER.warn("[ThirstAgent] staying on the loading warnings: {} is ours or names no mod",
                            warning.translationKey());
                    return;
                }
            }
            for (ModLoadingIssue warning : warnings) {
                ThirstWasTaken2.LOGGER.info("[ThirstAgent] passing another mod's loading warning: {} ({})",
                        warning.translationKey(), modId(warning));
            }
            Runnable next = (Runnable) field(screen, "nextScreenTask");
            // After the screen has finished building itself, as the button it stands in for would.
            Minecraft.getInstance().execute(next);
        } catch (ReflectiveOperationException | ClassCastException e) {
            ThirstWasTaken2.LOGGER.warn("[ThirstAgent] could not read the loading warnings screen; leaving it up", e);
        }
    }

    private static boolean isSomeoneElses(ModLoadingIssue warning) {
        String modId = modId(warning);
        return modId != null && !modId.startsWith(ThirstWasTaken2.MOD_ID);
    }

    /** The mod a warning is about, from the mod itself or else the first mod in its file. */
    private static String modId(ModLoadingIssue warning) {
        IModInfo mod = warning.affectedMod();
        if (mod != null) return mod.getModId();
        IModFile file = warning.affectedModFile();
        if (file != null && !file.getModInfos().isEmpty()) return file.getModInfos().getFirst().getModId();
        return null;
    }

    /** The screen's list of formatted issues, unwrapped to the issues themselves. */
    private static List<ModLoadingIssue> issues(LoadingErrorScreen screen, String name) throws ReflectiveOperationException {
        List<ModLoadingIssue> issues = new ArrayList<>();
        for (Object formatted : (List<?>) field(screen, name)) {
            for (RecordComponent component : formatted.getClass().getRecordComponents()) {
                if (component.getType() == ModLoadingIssue.class) {
                    component.getAccessor().setAccessible(true);
                    issues.add((ModLoadingIssue) component.getAccessor().invoke(formatted));
                }
            }
        }
        return issues;
    }

    private static Object field(LoadingErrorScreen screen, String name) throws ReflectiveOperationException {
        Field field = LoadingErrorScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(screen);
    }
}
