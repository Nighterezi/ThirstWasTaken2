package com.thirstwastaken2.gametest;

import com.mojang.authlib.GameProfile;
import com.thirstwastaken2.gametest.platform.CapturingConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A player that is really in the level and is really tracked, but whose connection goes nowhere: every
 * packet sent to it is kept in a list instead.
 *
 * <p>This is not the benchmark's simulated player. That one is deliberately outside the level and
 * untracked, which is right for measuring server cost and wrong for anything synced: the loader works
 * out who to tell from who is watching, so a player nobody watches can never show that the mod told
 * the wrong person. These are added to the level so that they do watch each other, which is what makes
 * "only the owner is told" a claim a test can fail.
 *
 * <p>They cost what a real player costs, so a test makes a handful and removes them again.
 */
final class SyncPlayer extends ServerPlayer {
    private final List<Packet<?>> captured = new ArrayList<>();

    private SyncPlayer(ServerLevel level, int index) {
        super(level.getServer(), level, profile(index), ClientInformation.createDefault());
        CapturingConnection.install(this, captured::add);
    }

    /** Placed at {@code at} and added to the level, so the chunk map starts tracking them. */
    static List<SyncPlayer> place(ServerLevel level, BlockPos at, int count) {
        List<SyncPlayer> players = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            SyncPlayer player = new SyncPlayer(level, index);
            player.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
            level.addNewPlayer(player);
            players.add(player);
        }
        return players;
    }

    static void remove(ServerLevel level, List<SyncPlayer> players) {
        for (SyncPlayer player : players) {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    /**
     * Does for these players what the server does every tick for the players in its player list, which
     * these are not in: sends the chunks their tracking view is waiting on, and acknowledges the batch
     * the way a real client would. Until those chunks are sent, the chunk map says the chunk is not
     * tracked, no player ever ends up watching another, and the check below would have nothing to
     * prove.
     */
    static void sendChunks(List<SyncPlayer> players) {
        for (SyncPlayer player : players) {
            player.connection.chunkSender.sendNextChunks(player);
            player.connection.chunkSender.onChunkBatchReceivedByClient(64.0F);
        }
    }

    /**
     * How many of {@code players} the chunk map has watching {@code at}. The loader builds its sync
     * list from exactly this, so a test that reads fewer than it placed is not testing anything and
     * has to say so. Only these players are counted: a gametest server has mock players of other
     * tests wandering about, and they are not the fixture.
     */
    static int watching(ServerLevel level, BlockPos at, List<SyncPlayer> players) {
        List<ServerPlayer> watchers =
                level.getChunkSource().chunkMap.getPlayers(level.getChunkAt(at).getPos(), false);
        int counted = 0;
        for (SyncPlayer player : players) {
            if (watchers.contains(player)) counted++;
        }
        return counted;
    }

    /** The custom payloads sent to this player since the last {@link #clear}. */
    List<ClientboundCustomPayloadPacket> payloads() {
        List<ClientboundCustomPayloadPacket> payloads = new ArrayList<>();
        for (Packet<?> packet : captured) {
            if (packet instanceof ClientboundCustomPayloadPacket payload) payloads.add(payload);
        }
        return payloads;
    }

    /** The payload ids sent to this player, for a failure message that names what actually arrived. */
    String payloadIds() {
        List<String> ids = new ArrayList<>();
        for (ClientboundCustomPayloadPacket payload : payloads()) {
            ids.add(payload.payload().type().id().toString());
        }
        return ids.isEmpty() ? "nothing" : String.join(", ", ids);
    }

    void clear() {
        captured.clear();
    }

    String who() {
        return getScoreboardName();
    }

    /** Never ticked: these exist to be sent to, not to play. */
    @Override
    public void tick() {
    }

    @Override
    public void updateOptions(ClientInformation settings) {
    }

    /** Derived from the index, so a run reuses the previous run's stats and advancements rather than piling up. */
    private static GameProfile profile(int index) {
        UUID uuid = UUID.nameUUIDFromBytes(("thirstwastaken2-sync:" + index).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, "thirstsync" + index);
    }
}
