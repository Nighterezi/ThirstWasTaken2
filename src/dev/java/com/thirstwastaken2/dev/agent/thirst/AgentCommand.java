package com.thirstwastaken2.dev.agent.thirst;

import com.mojang.brigadier.CommandDispatcher;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.agent.core.AgentDispatcher;
import com.thirstwastaken2.dev.platform.DevLoader;
import com.thirstwastaken2.platform.Vanilla;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /thirst agent probe|queue}, the one part of the agent a person types.
 *
 * <p>It answers the same thing the queue's {@code probe} does, and exists for the moment before an
 * agent is wired up at all: it says whether this process has a queue, where it is, and what it can
 * answer. A run where {@code /thirst agent probe} works is a run whose {@code in.jsonl} will be read.
 */
public final class AgentCommand {
    private AgentCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Merges into the mod's own /thirst node, the same way /thirst benchmark does: whichever
        // registration runs first decides the root requirement, so it is repeated here.
        dispatcher.register(Commands.literal("thirst")
                .requires(Vanilla::isGameMaster)
                .then(Commands.literal("agent")
                        .requires(Vanilla::isOwner)
                        .executes(context -> probe(context.getSource()))
                        .then(Commands.literal("probe").executes(context -> probe(context.getSource())))
                        .then(Commands.literal("queue").executes(context -> queue(context.getSource())))));
    }

    private static int probe(CommandSourceStack source) {
        AgentDispatcher agent = ThirstAgent.dispatcher();
        if (agent == null) {
            source.sendFailure(Component.literal("No agent queue in this process. "
                    + "It is only installed under a development run, where ThirstWasTaken2.DEV is set."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("ThirstWasTaken2 agent: side " + ThirstAgent.side()
                + ", " + DevLoader.LOADER + ", Minecraft " + ThirstWasTaken2.MINECRAFT
                + ", " + agent.commands().size() + " commands, " + agent.pending() + " waiting"), false);
        source.sendSuccess(() -> Component.literal("Queue: "
                + agent.queue().directory().toAbsolutePath()), false);
        return agent.commands().size();
    }

    private static int queue(CommandSourceStack source) {
        AgentDispatcher agent = ThirstAgent.dispatcher();
        if (agent == null) {
            source.sendFailure(Component.literal("No agent queue in this process"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(agent.queue().directory().toAbsolutePath().toString()), false);
        return 1;
    }
}
