package com.thirstwastaken2.dev.benchmark;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerLevel;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A server player that never joins: it is not in the player list or the level, never ticks on its own,
 * cannot be hurt, and every packet sent to it is dropped by Fabric's fake connection. It still runs every
 * server-side path the mod hooks, which is all the benchmark needs.
 *
 * <p>Constructed directly rather than through {@code FakePlayer.get}, which caches instances, so each run
 * starts from a fresh entity instead of one still carrying the previous run's effects and attachments. The
 * UUID is derived from the index, so the stats and advancement objects vanilla caches per UUID are reused by
 * the next run instead of piling up until the server stops.
 */
final class BenchmarkPlayer extends FakePlayer {
    BenchmarkPlayer(ServerLevel level, int index) {
        super(level, new GameProfile(uuid(index), "thirstbench" + index));
    }

    private static UUID uuid(int index) {
        return UUID.nameUUIDFromBytes(("thirstwastaken2-benchmark:" + index).getBytes(StandardCharsets.UTF_8));
    }
}
