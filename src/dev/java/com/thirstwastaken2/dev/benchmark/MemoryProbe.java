package com.thirstwastaken2.dev.benchmark;

import com.google.gson.JsonObject;
import com.thirstwastaken2.api.ThirstApi;
import com.thirstwastaken2.data.ExhaustionTracker;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.purity.WaterPurity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * What the mod keeps per player and in its static caches.
 *
 * <p>Sizes come from counting the bytes the server thread allocates while creating many instances, which is
 * exact and unaffected by garbage collection. The per-player figures use players nobody has touched yet, so
 * they include creating the attachment. Whole-heap numbers are taken by the run itself, after a collection
 * before it starts and after it has cleaned up.
 */
final class MemoryProbe implements Stage {
    private static final int FRESH_PLAYERS = 64;
    /** Keeps these UUIDs apart from the ones the tick scenarios use. */
    private static final int FRESH_INDEX = 9_000;
    private static final int INSTANCES = 10_000;

    private final BenchmarkWorld world;
    private final List<BenchmarkPlayer> fresh = new ArrayList<>();
    private JsonObject result;

    MemoryProbe(BenchmarkWorld world) {
        this.world = world;
    }

    @Override
    public String name() {
        return "memory probe";
    }

    @Override
    public String progress() {
        return "creating players " + fresh.size() + "/" + FRESH_PLAYERS;
    }

    /** The probe's report section, or null while it has not finished. */
    JsonObject result() {
        return result;
    }

    void release() {
        fresh.clear();
    }

    @Override
    public boolean run(long deadlineNanos) {
        while (fresh.size() < FRESH_PLAYERS) {
            if (System.nanoTime() >= deadlineNanos) return false;
            BenchmarkPlayer player = world.extraPlayer(FRESH_INDEX + fresh.size());
            world.standInField(player, fresh.size());
            fresh.add(player);
        }
        result = measure();
        fresh.clear();
        return true;
    }

    private JsonObject measure() {
        JsonObject json = new JsonObject();
        json.addProperty("allocationTracking", Metrics.allocationTracking());
        json.addProperty("thirstDataBytes", bytesPerInstance(() -> new ThirstData(ThirstData.MAX, 5, 1.5F, true)));
        json.addProperty("exhaustionTrackerBytes", bytesPerInstance(ExhaustionTracker::new));

        long bytes = Metrics.allocatedBytes();
        for (BenchmarkPlayer player : fresh) {
            ThirstManager.get(player);
            player.causeFoodExhaustion(0.1F);
            ThirstManager.tickPlayer(player);
        }
        json.addProperty("firstTouchBytesPerPlayer", Metrics.round((Metrics.allocatedBytes() - bytes) / (double) FRESH_PLAYERS));

        bytes = Metrics.allocatedBytes();
        for (BenchmarkPlayer player : fresh) {
            player.causeFoodExhaustion(0.028F);
            ThirstManager.tickPlayer(player);
        }
        json.addProperty("steadyTickBytesPerPlayer", Metrics.round((Metrics.allocatedBytes() - bytes) / (double) FRESH_PLAYERS));

        json.addProperty("thirstCacheEntries", mapSize(ThirstApi.class, "CACHE"));
        json.addProperty("purityInfoEntries", mapSize(WaterPurity.class, "INFO"));
        return json;
    }

    private static double bytesPerInstance(Supplier<Object> factory) {
        Object[] keep = new Object[INSTANCES];
        long bytes = Metrics.allocatedBytes();
        for (int i = 0; i < INSTANCES; i++) keep[i] = factory.get();
        long allocated = Metrics.allocatedBytes() - bytes;
        return keep[INSTANCES - 1] == null ? 0.0 : Metrics.round(allocated / (double) INSTANCES);
    }

    /** Entries in one of the mod's private static caches, or -1 when it cannot be read. */
    private static int mapSize(Class<?> owner, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null) instanceof Map<?, ?> map ? map.size() : -1;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return -1;
        }
    }
}
