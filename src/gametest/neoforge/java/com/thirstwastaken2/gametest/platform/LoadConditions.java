package com.thirstwastaken2.gametest.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * A data file's load conditions, read the way NeoForge reads them as the file loads: through its own
 * codec, which only knows a condition type that was registered. The file is the one the build
 * translated from Fabric's spelling, so this also checks that translation.
 */
public final class LoadConditions {
    private static final String KEY = "neoforge:conditions";

    private LoadConditions() { }

    /** Whether {@code json} carries load conditions at all. */
    public static boolean present(JsonObject json) {
        return json.has(KEY);
    }

    /** Whether the game would load {@code json} now; true when it carries no conditions. */
    public static boolean hold(JsonObject json) {
        JsonElement conditions = json.get(KEY);
        if (conditions == null) return true;
        return ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, conditions).getOrThrow().stream()
                .allMatch(condition -> condition.test(ICondition.IContext.EMPTY));
    }
}
