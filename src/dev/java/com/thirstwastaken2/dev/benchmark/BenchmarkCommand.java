package com.thirstwastaken2.dev.benchmark;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /thirst benchmark [quick|standard|stress|players <count> [ticks]|status|cancel]}. */
public final class BenchmarkCommand {
    private BenchmarkCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Merges into the mod's own /thirst node. Whichever registration runs first decides the root
        // requirement, so it is repeated here; benchmark itself is stricter.
        dispatcher.register(Commands.literal("thirst")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("benchmark")
                        .requires(source -> Commands.LEVEL_OWNERS.check(source.permissions()))
                        .executes(context -> start(context.getSource(), BenchmarkProfile.STANDARD))
                        .then(profile(BenchmarkProfile.QUICK))
                        .then(profile(BenchmarkProfile.STANDARD))
                        .then(profile(BenchmarkProfile.STRESS))
                        .then(Commands.literal("players")
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, BenchmarkProfile.MAX_PLAYERS))
                                        .executes(context -> start(context.getSource(), BenchmarkProfile.players(
                                                IntegerArgumentType.getInteger(context, "count"),
                                                BenchmarkProfile.DEFAULT_TICKS)))
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer(
                                                        BenchmarkProfile.MIN_TICKS, BenchmarkProfile.MAX_TICKS))
                                                .executes(context -> start(context.getSource(), BenchmarkProfile.players(
                                                        IntegerArgumentType.getInteger(context, "count"),
                                                        IntegerArgumentType.getInteger(context, "ticks")))))))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("cancel").executes(context -> cancel(context.getSource())))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profile(BenchmarkProfile profile) {
        return Commands.literal(profile.name()).executes(context -> start(context.getSource(), profile));
    }

    private static int start(CommandSourceStack source, BenchmarkProfile profile) {
        if (!BenchmarkRunner.start(source, profile)) {
            source.sendFailure(Component.literal("A thirst benchmark is already running: " + BenchmarkRunner.status()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Thirst benchmark '" + profile.name() + "' started: "
                + profile.describe() + ". Check progress with /thirst benchmark status, stop with /thirst benchmark cancel."),
                false);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        boolean running = BenchmarkRunner.isRunning();
        source.sendSuccess(() -> Component.literal(running ? BenchmarkRunner.status() : "No thirst benchmark is running"), false);
        return running ? 1 : 0;
    }

    private static int cancel(CommandSourceStack source) {
        if (!BenchmarkRunner.cancel()) {
            source.sendFailure(Component.literal("No thirst benchmark is running"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "Cancelling the thirst benchmark; the partial report is written on the next tick"), false);
        return 1;
    }
}
