package com.thirstwastaken.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.thirstwastaken.data.ThirstManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class ThirstCommands {
    private ThirstCommands() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("thirst")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("query")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    var player = EntityArgument.getPlayer(context, "player");
                                    var data = ThirstManager.get(player);
                                    context.getSource().sendSuccess(() -> Component.translatable(
                                            "command.thirstwastaken.query", data.thirst(), data.quenched()), false);
                                    return data.thirst();
                                })))
                .then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("thirst", IntegerArgumentType.integer(0, 20))
                                        .then(Commands.argument("quenched", IntegerArgumentType.integer(0, 20))
                                                .executes(context -> {
                                                    int thirst = IntegerArgumentType.getInteger(context, "thirst");
                                                    int quenched = IntegerArgumentType.getInteger(context, "quenched");
                                                    var players = EntityArgument.getPlayers(context, "players");
                                                    players.forEach(player -> ThirstManager.set(player, ThirstManager.get(player).withLevels(thirst, quenched)));
                                                    context.getSource().sendSuccess(() -> Component.translatable("command.thirstwastaken.set", thirst, quenched), true);
                                                    return players.size();
                                                })))))
                .then(Commands.literal("enable")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            var players = EntityArgument.getPlayers(context, "players");
                                            players.forEach(player -> ThirstManager.set(player, ThirstManager.get(player).withEnabled(enabled)));
                                            String key = enabled
                                                    ? "command.thirstwastaken.enable"
                                                    : "command.thirstwastaken.disable";
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable(key, players.size()), true);
                                            return players.size();
                                        })))));
    }
}
