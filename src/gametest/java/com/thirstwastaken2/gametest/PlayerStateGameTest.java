package com.thirstwastaken2.gametest;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ThirstManager;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

/**
 * What thirst does to the player itself, which is everything {@code PlayerMixin} hooks.
 *
 * <p>Both hooks only take effect on top of a vanilla decision, so each test sets up the state where
 * vanilla would say yes before checking that the mod says no.
 */
public final class PlayerStateGameTest {
    /** Vanilla gates sprinting on food above 6, and the mod applies the same cut-off to thirst. */
    private static final int SPRINT_CUTOFF = 6;
    private static final float EXHAUSTION = 4.0F;

    @GameTest
    public void sprintingIsBlockedWhenThirsty(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);

        setThirst(player, SPRINT_CUTOFF + 1);
        TestFixtures.check(helper, player.canSprint(),
                "thirst " + (SPRINT_CUTOFF + 1) + " is above the cut-off and should allow sprinting");

        setThirst(player, SPRINT_CUTOFF);
        TestFixtures.check(helper, !player.canSprint(),
                "thirst " + SPRINT_CUTOFF + " should block sprinting");
        helper.succeed();
    }

    @GameTest
    public void sprintingIsAllowedWhenTheSettingIsOff(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        setThirst(player, SPRINT_CUTOFF);

        ThirstConfig config = ThirstConfig.get();
        boolean original = config.preventSprintingWhenThirsty;
        try {
            config.preventSprintingWhenThirsty = false;
            TestFixtures.check(helper, player.canSprint(),
                    "prevent_sprinting_when_thirsty is off, so thirst should not block sprinting");
        } finally {
            config.preventSprintingWhenThirsty = original;
        }
        helper.succeed();
    }

    @GameTest
    public void disabledThirstNeverBlocksSprinting(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);

        setThirst(player, SPRINT_CUTOFF);
        ThirstManager.set(player, ThirstManager.get(player).withEnabled(false));

        TestFixtures.check(helper, player.canSprint(),
                "a player with thirst disabled should sprint regardless of the value");
        helper.succeed();
    }

    @GameTest
    public void hungerExhaustionMirrorsIntoThirst(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        float before = ThirstManager.get(player).exhaustion();

        player.causeFoodExhaustion(EXHAUSTION);

        float after = ThirstManager.get(player).exhaustion();
        TestFixtures.check(helper, after > before,
                "spending hunger should also spend thirst, exhaustion stayed at " + after);
        helper.succeed();
    }

    @GameTest
    public void ridingDoesNotDehydrate(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        Entity mount = helper.spawn(TestFixtures.mountType(), new BlockPos(2, 2, 2));
        player.startRiding(mount, true, true);
        TestFixtures.check(helper, player.isPassenger(), "the player should be riding the mount");

        float before = ThirstManager.get(player).exhaustion();
        player.causeFoodExhaustion(EXHAUSTION);

        TestFixtures.check(helper, ThirstManager.get(player).exhaustion() == before,
                "riding should not dehydrate, exhaustion moved to "
                        + ThirstManager.get(player).exhaustion());
        helper.succeed();
    }

    /** Survival with a full hunger bar, so vanilla allows sprinting and charges exhaustion. */
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        return player;
    }

    private static void setThirst(ServerPlayer player, int thirst) {
        ThirstManager.set(player, ThirstManager.get(player).withLevels(thirst, 0));
    }
}
