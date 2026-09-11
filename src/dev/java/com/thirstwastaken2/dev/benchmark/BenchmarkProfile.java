package com.thirstwastaken2.dev.benchmark;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

/** How much a benchmark run measures. Every profile runs the same scenarios; only the scale differs. */
record BenchmarkProfile(String name, int[] playerCounts, int warmupTicks, int measuredTicks, int interactionOps) {
    static final int MAX_PLAYERS = 2000;
    static final int MIN_TICKS = 20;
    static final int MAX_TICKS = 72_000;
    static final int DEFAULT_TICKS = 600;

    static final BenchmarkProfile QUICK =
            new BenchmarkProfile("quick", new int[] {1, 10, 50}, 100, 200, 2_000);
    static final BenchmarkProfile STANDARD =
            new BenchmarkProfile("standard", new int[] {1, 10, 50, 100, 200}, 200, 600, 10_000);
    static final BenchmarkProfile STRESS =
            new BenchmarkProfile("stress", new int[] {1, 100, 250, 500, 1000}, 200, 600, 20_000);

    /** A single player count, for looking at one scale in detail. */
    static BenchmarkProfile players(int count, int measuredTicks) {
        return new BenchmarkProfile("players", new int[] {count}, Math.max(100, measuredTicks / 3), measuredTicks, 5_000);
    }

    String describe() {
        return Arrays.toString(playerCounts) + " players, " + warmupTicks + " warm-up + " + measuredTicks
                + " measured ticks each, " + interactionOps + " ops per interaction";
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        JsonArray counts = new JsonArray();
        for (int count : playerCounts) counts.add(count);
        json.add("playerCounts", counts);
        json.addProperty("warmupTicks", warmupTicks);
        json.addProperty("measuredTicks", measuredTicks);
        json.addProperty("interactionOps", interactionOps);
        return json;
    }
}
