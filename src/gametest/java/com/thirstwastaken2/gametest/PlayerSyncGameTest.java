package com.thirstwastaken2.gametest;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.gametest.platform.CapturingConnection;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * That a player is told their own thirst and no one else's, with several players in range of each
 * other — the one item of MANUAL-TESTING.md's sync checks that two pairs of eyes on
 * two screens were the only way to check.
 *
 * <p>NeoForge only, and deliberately so: what this exists to catch is {@code Loader.syncsTo}, which is
 * NeoForge's own decision, and its Fabric counterpart is a value handed to Fabric API rather than a
 * function the mod writes. {@code CapturingConnection} on Fabric says why it cannot run there; the
 * check is made with two agent clients instead. See {@code src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md}.
 *
 * <p>The mechanism matters to how this is written. Both loaders work out who to sync to from who is
 * watching the player, so a simulated player nobody watches would be told only about itself whatever
 * the predicate said, and the test would pass even with the predicate broken wide open. So the players
 * are really added to the level, {@link SyncPlayer#settle} does the two things the server would do for
 * them until the chunk map has each of them watching the others, and the first two assertions are that
 * it worked: that they are watching the chunk, and that a broadcast about one of them reaches all
 * three. A run where that is not so fails as a broken fixture rather than passing on nothing.
 *
 * <p>Those two are what make the rest worth reading. Change {@code Loader.syncsTo} to {@code return
 * true} and this test goes red; before the control was there it did not, and this test proved less
 * than it claimed.
 */
public final class PlayerSyncGameTest {
    /** Three is enough: one owner, and two others who must be told nothing. */
    private static final int PLAYERS = 3;
    /** Ticks of {@link SyncPlayer#settle} for the chunk map to end up with everyone watching everyone. */
    private static final int SETTLE = 8;

    @GameTest
    public void eachPlayerIsToldOnlyTheirOwnThirst(GameTestHelper helper) {
        if (!CapturingConnection.available()) {
            ThirstWasTaken2.LOGGER.info("[ThirstGameTest] per-player sync is not checked on this loader: {}",
                    CapturingConnection.unavailable());
            helper.succeed();
            return;
        }

        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        List<SyncPlayer> players = SyncPlayer.place(level, at, PLAYERS);
        List<String> problems = new ArrayList<>();

        int[] step = {0};
        helper.startSequence()
                .thenExecuteFor(SETTLE, () -> SyncPlayer.settle(players, at, step[0]++))
                .thenExecute(() -> {
                    int watching = SyncPlayer.watching(level, at, players);
                    if (watching < PLAYERS) {
                        problems.add("only " + watching + " of " + PLAYERS + " simulated players are "
                                + "watching the test chunk after " + SETTLE + " ticks (the level holds "
                                + level.players().size() + " players), so nothing here would be sent to "
                                + "anyone and the check proves nothing");
                    }
                })
                // The control: until the others really are watching the owner, "only the owner was told"
                // is true of a fixture that could not tell anyone anything, and the mutation this test
                // exists to catch — a predicate that accepts everybody — goes unnoticed.
                .thenExecute(() -> {
                    int reached = SyncPlayer.reachedByBroadcastAbout(level, players, players.get(0));
                    if (reached < PLAYERS) {
                        problems.add("a broadcast about " + players.get(0).who() + " reached " + reached
                                + " of " + PLAYERS + " simulated players, so they are not in each other's "
                                + "tracking sets and a sync to everyone would look the same as a sync to "
                                + "one; the fixture is broken, not the mod");
                    }
                })
                .thenExecute(() -> change(players, 0, 6, 0))
                .thenExecute(() -> only(players, 0, problems))
                .thenExecute(() -> change(players, PLAYERS - 1, 3, 1))
                .thenExecute(() -> only(players, PLAYERS - 1, problems))
                .thenExecute(() -> SyncPlayer.remove(level, players))
                .thenExecute(() -> TestFixtures.check(helper, problems.isEmpty(), String.join("; ", problems)))
                .thenSucceed();
    }

    /** Clears what everyone has been sent, then writes one player's thirst. */
    private static void change(List<SyncPlayer> players, int index, int thirst, int quenched) {
        players.forEach(SyncPlayer::clear);
        SyncPlayer owner = players.get(index);
        ThirstManager.set(owner, ThirstManager.get(owner).withLevels(thirst, quenched));
    }

    /**
     * Collects a problem unless the owner was sent a payload and nobody else was sent anything. The
     * failure names the payloads that did arrive, because an unexpected one is a different bug from a
     * missing one.
     */
    private static void only(List<SyncPlayer> players, int owner, List<String> problems) {
        for (int index = 0; index < players.size(); index++) {
            SyncPlayer player = players.get(index);
            int payloads = player.payloads().size();
            if (index == owner && payloads == 0) {
                problems.add(player.who() + " owns the change and was sent nothing");
            } else if (index != owner && payloads > 0) {
                problems.add(player.who() + " was sent " + player.payloadIds()
                        + " for a change to " + players.get(owner).who() + "'s thirst");
            }
        }
    }
}
