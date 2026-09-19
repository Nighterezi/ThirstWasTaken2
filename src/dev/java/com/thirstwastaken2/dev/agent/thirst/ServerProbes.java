package com.thirstwastaken2.dev.agent.thirst;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.dev.agent.core.AgentDispatcher;
import com.thirstwastaken2.dev.agent.core.AgentException;
import com.thirstwastaken2.dev.agent.core.AgentRequest;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * What the server knows, as numbers. These answer wherever a server is running — a dedicated one, or
 * a client's integrated one — and say so plainly when there is none.
 *
 * <p>Nothing here parses chat. {@code server.command} does run a command, but it collects the feedback
 * the command produced into a list of its own rather than letting it go to the log for an agent to
 * scrape back out.
 */
final class ServerProbes {
    private ServerProbes() { }

    static void register(AgentDispatcher dispatcher) {
        dispatcher.register("server.info", (request, reply) -> {
            MinecraftServer server = server();
            JsonObject result = new JsonObject();
            result.addProperty("dedicated", server.isDedicatedServer());
            result.addProperty("tickCount", server.getTickCount());
            result.addProperty("players", server.getPlayerCount());
            result.addProperty("difficulty", server.getWorldData().getDifficulty().name());
            result.addProperty("levels", server.levelKeys().size());
            reply.ok(result);
        });

        /* Every online player's thirst, in one answer: what "each player sees only their own bar" is
         * checked against, from the side that decides it. */
        dispatcher.register("server.players", (request, reply) -> {
            JsonArray players = new JsonArray();
            for (ServerPlayer player : server().getPlayerList().getPlayers()) players.add(describe(player));
            JsonObject result = new JsonObject();
            result.add("players", players);
            result.addProperty("count", players.size());
            reply.ok(result);
        });

        dispatcher.register("server.thirst.get", (request, reply) ->
                reply.ok(describe(player(request, request.string("player")))));

        /*
         * Sets the state directly rather than through /thirst set, so a check of the command itself is
         * not also the way the fixture is built. Absent fields keep what the player has.
         */
        dispatcher.register("server.thirst.set", (request, reply) -> {
            ServerPlayer player = player(request, request.string("player"));
            ThirstData before = ThirstManager.get(player);
            int thirst = request.integer("thirst", 0, ThirstData.MAX);
            int quenched = request.has("quenched")
                    ? request.integer("quenched", 0, ThirstData.MAX) : before.quenched();
            ThirstData after = before.withLevels(thirst, quenched)
                    .withEnabled(request.flag("enabled", before.enabled()));
            float exhaustion = request.decimal("exhaustion", after.exhaustion());
            after = new ThirstData(after.thirst(), after.quenched(), exhaustion, after.enabled());
            ThirstManager.set(player, after);
            JsonObject result = new JsonObject();
            result.add("before", state(before));
            result.add("after", describe(player));
            reply.ok(result);
        });

        /*
         * Runs a command and hands back what it returned and what it said. With `as`, it runs from the
         * console's own source moved to that player: `@s` resolves to them, and the permission level is
         * still the console's, so a check never fails because a development player is not an operator.
         *
         * A client polls its queue on the client thread, and the integrated server answers a block
         * entity lookup from any other thread with null, so `data get block` would report every block
         * entity missing. The command is therefore handed to the server's own thread and waited for.
         */
        dispatcher.register("server.command", (request, reply) -> {
            MinecraftServer server = server();
            String command = request.string("command").strip();
            if (command.startsWith("/")) command = command.substring(1);
            Feedback feedback = new Feedback();
            CommandSourceStack console = server.createCommandSourceStack().withSource(feedback);
            ServerPlayer as = request.has("as") ? player(request, request.string("as")) : null;
            String line = command;
            Runnable run = () -> {
                CommandSourceStack source = console;
                if (as != null) {
                    source = source.withEntity(as).withPosition(as.position());
                    if (as.level() instanceof ServerLevel level) source = source.withLevel(level);
                }
                server.getCommands().performPrefixedCommand(source, line);
            };
            if (server.isSameThread()) {
                run.run();
            } else {
                try {
                    server.submit(run).get(30, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    throw new AgentException("server.command: the server did not run '" + command
                            + "' within 30 seconds; is the world paused?");
                } catch (InterruptedException | ExecutionException e) {
                    throw new AgentException("server.command: '" + command + "' failed: " + e);
                }
            }
            JsonObject result = new JsonObject();
            result.addProperty("command", command);
            JsonArray messages = new JsonArray();
            feedback.messages.forEach(messages::add);
            result.add("messages", messages);
            reply.ok(result);
        });
    }

    /** The whole of a player's thirst plus the server-side state a check might read beside it. */
    static JsonObject describe(ServerPlayer player) {
        JsonObject result = state(ThirstManager.get(player));
        result.addProperty("name", player.getScoreboardName());
        result.addProperty("uuid", player.getUUID().toString());
        result.addProperty("dimension", player.level().dimension().identifier().toString());
        result.addProperty("x", round(player.getX()));
        result.addProperty("y", round(player.getY()));
        result.addProperty("z", round(player.getZ()));
        result.addProperty("health", round(player.getHealth()));
        result.addProperty("food", player.getFoodData().getFoodLevel());
        result.addProperty("sprinting", player.isSprinting());
        result.addProperty("creative", player.isCreative());
        result.addProperty("alive", player.isAlive());
        return result;
    }

    static JsonObject state(ThirstData data) {
        JsonObject result = new JsonObject();
        result.addProperty("thirst", data.thirst());
        result.addProperty("quenched", data.quenched());
        result.addProperty("exhaustion", round(data.exhaustion()));
        result.addProperty("enabled", data.enabled());
        return result;
    }

    static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static MinecraftServer server() {
        MinecraftServer server = ThirstAgent.server();
        if (server == null) {
            throw new AgentException("no server is running in this process; "
                    + "on a client, load a world or use client.command to reach the one it joined");
        }
        return server;
    }

    /** The one online player with that name, however it is spelled. */
    private static ServerPlayer player(AgentRequest request, String name) {
        for (ServerPlayer player : server().getPlayerList().getPlayers()) {
            if (player.getScoreboardName().equalsIgnoreCase(name)) return player;
        }
        List<String> online = new ArrayList<>();
        for (ServerPlayer player : server().getPlayerList().getPlayers()) {
            online.add(player.getScoreboardName());
        }
        throw new AgentException(request.command() + ": no player called '" + name + "' is online; "
                + (online.isEmpty() ? "nobody is" : "online: " + String.join(", ", online)));
    }

    /** Collects what a command says instead of letting it go to the log. */
    private static final class Feedback implements CommandSource {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void sendSystemMessage(Component message) {
            messages.add(message.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    }
}
