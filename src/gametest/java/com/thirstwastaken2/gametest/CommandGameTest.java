package com.thirstwastaken2.gametest;

import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /thirst}, run through the server's real command dispatcher.
 *
 * <p>Every mock player shares one name, and a UUID counts as an entity selector that {@code /thirst}
 * rightly refuses, so commands target {@code @s}: the console's operator permission, run as the
 * player. The player's own source has no permission, which is what the permission test uses.
 */
public final class CommandGameTest {
    @GameTest
    public void setChangesTheLevelsOfThePlayer(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        run(helper, operator(helper, player), "thirst set @s 7 3");

        levels(helper, player, 7, 3, "/thirst set 7 3");
        helper.succeed();
    }

    @GameTest
    public void setKeepsQuenchedBelowThirst(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        run(helper, operator(helper, player), "thirst set @s 4 9");

        levels(helper, player, 4, 4, "/thirst set 4 9");
        helper.succeed();
    }

    @GameTest
    public void valuesOffTheBarAreRejected(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);
        ThirstData before = ThirstManager.get(player);

        run(helper, operator(helper, player), "thirst set @s 21 0");
        TestFixtures.check(helper, ThirstManager.get(player).equals(before),
                "/thirst set 21 should be rejected by the argument's range, got " + ThirstManager.get(player));

        run(helper, operator(helper, player), "thirst set @s 20 0");
        levels(helper, player, 20, 0, "/thirst set 20 0, the top of the range");
        helper.succeed();
    }

    @GameTest
    public void enableTurnsThirstOffAndOn(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);

        run(helper, operator(helper, player), "thirst enable @s false");
        TestFixtures.check(helper, !ThirstManager.get(player).enabled(), "/thirst enable false should turn thirst off");

        run(helper, operator(helper, player), "thirst enable @s true");
        TestFixtures.check(helper, ThirstManager.get(player).enabled(), "/thirst enable true should turn it back on");
        helper.succeed();
    }

    @GameTest
    public void playersWithoutPermissionCannotUseIt(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.mockPlayer(helper);
        ThirstData before = ThirstManager.get(player);

        run(helper, player.createCommandSourceStack(), "thirst set @s 1 0");
        TestFixtures.check(helper, ThirstManager.get(player).equals(before),
                "a player without operator permission should not be able to set thirst, got " + ThirstManager.get(player));

        run(helper, operator(helper, player), "thirst set @s 1 0");
        levels(helper, player, 1, 0, "the same command with operator permission");
        helper.succeed();
    }

    /** The console's permission, with {@code @s} meaning the player. */
    private static CommandSourceStack operator(GameTestHelper helper, ServerPlayer player) {
        return helper.getLevel().getServer().createCommandSourceStack().withEntity(player).withSuppressedOutput();
    }

    private static void run(GameTestHelper helper, CommandSourceStack source, String command) {
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, command);
    }

    private static void levels(GameTestHelper helper, ServerPlayer player, int thirst, int quenched, String what) {
        ThirstData data = ThirstManager.get(player);
        TestFixtures.check(helper, data.thirst() == thirst && data.quenched() == quenched,
                what + " should leave " + thirst + "/" + quenched + ", got " + data);
    }
}
