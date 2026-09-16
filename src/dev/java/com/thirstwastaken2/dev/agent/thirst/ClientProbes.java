package com.thirstwastaken2.dev.agent.thirst;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.thirstwastaken2.client.ThirstHud;
import com.thirstwastaken2.client.config.ThirstConfigScreen;
import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.dev.agent.core.AgentDispatcher;
import com.thirstwastaken2.dev.agent.core.AgentException;
import com.thirstwastaken2.dev.agent.core.AgentReply;
import com.thirstwastaken2.dev.agent.core.AgentRequest;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the client knows, as numbers: the value it holds, whether it is sprinting, where the mod drew
 * the bar, and what colour a pixel of the framebuffer is.
 *
 * <p>Every handler here runs on the client thread, from the client tick. A probe that answers about a
 * frame — a capture or a sample — is deferred rather than answered on the spot, because the framebuffer
 * is read back from the GPU asynchronously from 1.21.11 on and the answer would otherwise describe the
 * previous frame or no frame at all.
 *
 * <p>Nothing in here is an assertion about a picture. A capture is written so a person can look at it
 * afterwards; the answer an agent acts on is always a number or a string.
 */
final class ClientProbes {
    /** Ticks between two looks at a capture that has not finished, and how many looks to take. */
    private static final int CAPTURE_POLL_TICKS = 2;
    private static final int CAPTURE_ATTEMPTS = 30;

    private ClientProbes() { }

    static void register(AgentDispatcher dispatcher) {
        dispatcher.register("client.info", (request, reply) -> {
            Minecraft minecraft = client();
            JsonObject result = new JsonObject();
            result.addProperty("frameWidth", minecraft.getWindow().getWidth());
            result.addProperty("frameHeight", minecraft.getWindow().getHeight());
            result.addProperty("guiWidth", minecraft.getWindow().getGuiScaledWidth());
            result.addProperty("guiHeight", minecraft.getWindow().getGuiScaledHeight());
            result.addProperty("guiScale", AgentClientVanilla.guiScale(minecraft));
            result.addProperty("fps", minecraft.getFps());
            result.addProperty("screen", screenName(minecraft));
            result.addProperty("inWorld", minecraft.level != null);
            result.addProperty("singleplayer", minecraft.hasSingleplayerServer());
            ServerData current = minecraft.getCurrentServer();
            result.addProperty("server", current == null ? null : current.ip);
            LocalPlayer player = minecraft.player;
            result.addProperty("player", player == null ? null : player.getScoreboardName());
            JsonArray keys = new JsonArray();
            Keys.names(minecraft).forEach(keys::add);
            result.add("keys", keys);
            reply.ok(result);
        });

        /*
         * The client's own answer to "what thirst does this client hold": read straight out of the
         * synced state this client was given, never off a screenshot. Everything beside it is the state
         * that decides whether the bar is drawn at all.
         */
        dispatcher.register("client.state", (request, reply) -> {
            Minecraft minecraft = client();
            LocalPlayer player = player(minecraft);
            JsonObject result = ServerProbes.state(ThirstManager.get(player));
            result.addProperty("name", player.getScoreboardName());
            result.addProperty("uuid", player.getUUID().toString());
            result.addProperty("sprinting", player.isSprinting());
            result.addProperty("sneaking", player.isShiftKeyDown());
            result.addProperty("onGround", player.onGround());
            result.addProperty("health", ServerProbes.round(player.getHealth()));
            result.addProperty("food", player.getFoodData().getFoodLevel());
            result.addProperty("creative", player.isCreative());
            result.addProperty("dimension", player.level().dimension().identifier().toString());
            result.addProperty("x", ServerProbes.round(player.getX()));
            result.addProperty("y", ServerProbes.round(player.getY()));
            result.addProperty("z", ServerProbes.round(player.getZ()));
            result.addProperty("ridingLiving", player.getVehicle() instanceof LivingEntity);
            result.addProperty("hudHidden", ClientVanilla.isHudHidden(minecraft));
            result.addProperty("barShouldRender", ThirstHud.shouldRender(player));
            result.addProperty("screen", screenName(minecraft));
            reply.ok(result);
        });

        /*
         * Where the bar was drawn, taken from the draw call by the recording mixin. `drawnMsAgo` is how
         * "the bar is hidden" is told from "the bar is empty": a hidden bar simply stops being drawn,
         * and the HUD redraws every frame, so anything over a few frames old means it is gone.
         */
        dispatcher.register("client.hud", (request, reply) -> {
            Minecraft minecraft = client();
            JsonObject result = new JsonObject();
            result.addProperty("recording", HudRecord.installed());
            result.addProperty("rowsDrawn", HudRecord.rows());
            result.addProperty("guiWidth", minecraft.getWindow().getGuiScaledWidth());
            result.addProperty("guiHeight", minecraft.getWindow().getGuiScaledHeight());
            result.addProperty("guiScale", AgentClientVanilla.guiScale(minecraft));
            result.add("bar", bar(HudRecord.hud()));
            result.add("preview", bar(HudRecord.preview()));
            JsonObject geometry = new JsonObject();
            geometry.addProperty("iconSize", HudRecord.ICON_SIZE);
            geometry.addProperty("iconStride", HudRecord.ICON_STRIDE);
            geometry.addProperty("icons", HudRecord.ICONS);
            geometry.addProperty("barWidth", HudRecord.BAR_WIDTH);
            result.add("geometry", geometry);
            if (!HudRecord.installed()) {
                result.addProperty("note", "the recording mixin has not run; "
                        + "either the bar has never been drawn, or ThirstHud has been renamed");
            }
            reply.ok(result);
        });

        /* Writes the framebuffer to a PNG beside the queue. Evidence for a person, never an assertion. */
        dispatcher.register("client.capture", (request, reply) -> {
            Minecraft minecraft = client();
            Frames.Capture capture = Frames.capture(minecraft, dispatcher.queue().directory(),
                    request.string("name", null));
            whenCaptured(dispatcher, reply, capture, () -> reply.ok(frame(minecraft, capture)));
        });

        /*
         * The colour of the framebuffer at named points. Points are in GUI pixels by default, which is
         * what client.hud answers in, so a check reads "the pixel at the middle of the third droplet"
         * rather than a physical coordinate that changes with the window.
         */
        dispatcher.register("client.pixels", (request, reply) -> {
            Minecraft minecraft = client();
            List<int[]> points = points(request);
            boolean gui = request.choice("space", "gui", "gui", "frame").equals("gui");
            boolean fresh = request.flag("capture", Frames.lastFile() == null);
            if (!fresh) {
                reply.ok(samples(minecraft, Frames.lastFile(), points, gui));
                return;
            }
            Frames.Capture capture = Frames.capture(minecraft, dispatcher.queue().directory(),
                    request.string("name", null));
            whenCaptured(dispatcher, reply, capture,
                    () -> reply.ok(samples(minecraft, capture.file(), points, gui)));
        });

        /* Commands go through the player's own connection, so the server sees them exactly as typed. */
        dispatcher.register("client.command", (request, reply) -> {
            String command = request.string("command").strip();
            if (command.startsWith("/")) command = command.substring(1);
            player(client()).connection.sendCommand(command);
            JsonObject result = new JsonObject();
            result.addProperty("command", command);
            reply.ok(result);
        });

        dispatcher.register("client.chat", (request, reply) -> {
            player(client()).connection.sendChat(request.string("message"));
            reply.ok();
        });

        /*
         * Holds keys down for a number of ticks and answers with the state afterwards. This is the sprint
         * gate: hold forward and sprint at 7 thirst and at 6, and read LocalPlayer.isSprinting() rather
         * than walking a measured track and comparing distances.
         */
        dispatcher.register("client.hold", (request, reply) -> {
            Minecraft minecraft = client();
            List<String> names = request.strings("keys");
            if (names.isEmpty()) throw new AgentException("client.hold: 'keys' needs at least one key");
            int ticks = request.integer("ticks", 1, 20 * 60);
            Map<String, KeyMapping> held = new LinkedHashMap<>();
            for (String name : names) held.put(name, Keys.of(minecraft, name));
            JsonObject before = movement(minecraft);
            held.values().forEach(key -> key.setDown(true));
            dispatcher.defer(ticks, reply, () -> {
                JsonObject after = movement(minecraft);
                held.values().forEach(key -> key.setDown(false));
                JsonObject result = new JsonObject();
                JsonArray keys = new JsonArray();
                held.keySet().forEach(keys::add);
                result.add("keys", keys);
                result.addProperty("ticks", ticks);
                result.add("before", before);
                result.add("after", after);
                reply.ok(result);
            });
        });

        /* Sets one key's state and leaves it there, for a hold that spans several requests. */
        dispatcher.register("client.key", (request, reply) -> {
            Minecraft minecraft = client();
            KeyMapping key = Keys.of(minecraft, request.string("key"));
            boolean down = request.flag("down", true);
            key.setDown(down);
            JsonObject result = new JsonObject();
            result.addProperty("key", request.string("key"));
            result.addProperty("down", key.isDown());
            reply.ok(result);
        });

        /* Screens are opened through the game, not by clicking at coordinates that move with the window. */
        dispatcher.register("client.screen", (request, reply) -> {
            Minecraft minecraft = client();
            String open = request.choice("open", "none", "none", "config");
            ClientVanilla.setScreen(minecraft, open.equals("config")
                    ? new ThirstConfigScreen(AgentClientVanilla.screen(minecraft)) : null);
            JsonObject result = new JsonObject();
            result.addProperty("screen", screenName(minecraft));
            reply.ok(result);
        });

        /*
         * The lines an item's tooltip produced, as text. Whether they read well is still a person's
         * judgement; whether the grade line is there, and in the right order, is not.
         */
        dispatcher.register("client.tooltip", (request, reply) -> {
            Minecraft minecraft = client();
            LocalPlayer player = player(minecraft);
            ItemStack stack = stack(request);
            List<Component> lines = stack.getTooltipLines(Item.TooltipContext.of(player.level()), player,
                    request.flag("advanced", false) ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
            JsonArray text = new JsonArray();
            JsonArray colours = new JsonArray();
            for (Component line : lines) {
                text.add(line.getString());
                Integer colour = line.getStyle().getColor() == null ? null
                        : line.getStyle().getColor().getValue();
                colours.add(colour == null ? null : String.format("#%06X", colour));
            }
            JsonObject result = new JsonObject();
            result.addProperty("item", stack.getItem().toString());
            result.addProperty("count", stack.getCount());
            result.add("lines", text);
            result.add("colours", colours);
            reply.ok(result);
        });

        /*
         * Leaving and rejoining is one of the checks, so it is one of the commands. The client answers
         * before it disconnects, because the queue it would answer into belongs to this process and the
         * process survives either way.
         */
        dispatcher.register("client.disconnect", (request, reply) -> {
            Minecraft minecraft = client();
            if (minecraft.level == null) throw new AgentException("client.disconnect: no world is loaded");
            reply.ok();
            minecraft.disconnect(new TitleScreen(), false);
        });

        dispatcher.register("client.connect", (request, reply) -> {
            Minecraft minecraft = client();
            String address = request.string("address", "localhost:25565");
            if (minecraft.level != null) {
                throw new AgentException("client.connect: a world is already loaded; disconnect first");
            }
            reply.ok();
            ServerData data = new ServerData("agent", address, ServerData.Type.OTHER);
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                    new TitleScreen(), minecraft,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                    data, false, null);
        });
    }

    /** Waits for a capture to reach disk, then runs {@code ready}. */
    private static void whenCaptured(AgentDispatcher dispatcher, AgentReply reply, Frames.Capture capture,
                                     Runnable ready) {
        waitForCapture(dispatcher, reply, capture, CAPTURE_ATTEMPTS, ready);
    }

    private static void waitForCapture(AgentDispatcher dispatcher, AgentReply reply, Frames.Capture capture,
                                       int attemptsLeft, Runnable ready) {
        dispatcher.defer(CAPTURE_POLL_TICKS, reply, () -> {
            String failure = capture.failure().get();
            if (failure != null) {
                reply.fail("the capture failed: " + failure);
                return;
            }
            if (capture.finished() && Files.isRegularFile(capture.file())) {
                Frames.invalidate();
                ready.run();
                return;
            }
            if (attemptsLeft <= 1) {
                reply.fail("the capture did not reach " + capture.file() + " within "
                        + CAPTURE_ATTEMPTS * CAPTURE_POLL_TICKS + " ticks");
                return;
            }
            waitForCapture(dispatcher, reply, capture, attemptsLeft - 1, ready);
        });
    }

    private static JsonObject frame(Minecraft minecraft, Frames.Capture capture) {
        BufferedImage image = Frames.image(capture.file());
        JsonObject result = new JsonObject();
        result.addProperty("file", capture.file().toAbsolutePath().toString());
        result.addProperty("width", image.getWidth());
        result.addProperty("height", image.getHeight());
        result.addProperty("guiScale", AgentClientVanilla.guiScale(minecraft));
        result.addProperty("message", capture.message().get());
        return result;
    }

    private static JsonObject samples(Minecraft minecraft, Path file, List<int[]> points, boolean gui) {
        BufferedImage image = Frames.image(file);
        double scale = AgentClientVanilla.guiScale(minecraft);
        JsonArray samples = new JsonArray();
        for (int[] point : points) {
            int frameX = gui ? (int) Math.round((point[0] + 0.5) * scale) : point[0];
            int frameY = gui ? (int) Math.round((point[1] + 0.5) * scale) : point[1];
            JsonObject sample = new JsonObject();
            sample.addProperty("x", point[0]);
            sample.addProperty("y", point[1]);
            sample.addProperty("frameX", frameX);
            sample.addProperty("frameY", frameY);
            if (frameX < 0 || frameY < 0 || frameX >= image.getWidth() || frameY >= image.getHeight()) {
                sample.addProperty("error", "outside the " + image.getWidth() + "x" + image.getHeight() + " frame");
            } else {
                int argb = image.getRGB(frameX, frameY);
                sample.addProperty("argb", Frames.hex(argb));
                sample.addProperty("alpha", (argb >>> 24) & 0xFF);
                sample.addProperty("red", (argb >> 16) & 0xFF);
                sample.addProperty("green", (argb >> 8) & 0xFF);
                sample.addProperty("blue", argb & 0xFF);
            }
            samples.add(sample);
        }
        JsonObject result = new JsonObject();
        result.addProperty("file", file.toAbsolutePath().toString());
        result.addProperty("width", image.getWidth());
        result.addProperty("height", image.getHeight());
        result.addProperty("guiScale", scale);
        result.addProperty("space", gui ? "gui" : "frame");
        result.add("points", samples);
        return result;
    }

    /**
     * The points to sample, written either as {@code [x, y]} or as <code>{"x": .., "y": ..}</code>.
     * Both spellings turn up in hand-written scripts, and neither is worth an error.
     */
    private static List<int[]> points(AgentRequest request) {
        List<int[]> points = new ArrayList<>();
        for (JsonElement element : request.list("points")) {
            if (element.isJsonArray() && element.getAsJsonArray().size() == 2) {
                JsonArray pair = element.getAsJsonArray();
                points.add(new int[] {pair.get(0).getAsInt(), pair.get(1).getAsInt()});
            } else if (element.isJsonObject() && element.getAsJsonObject().has("x")) {
                JsonObject object = element.getAsJsonObject();
                points.add(new int[] {object.get("x").getAsInt(), object.get("y").getAsInt()});
            } else {
                throw new AgentException("client.pixels: a point is [x, y] or {\"x\": .., \"y\": ..}, got "
                        + element);
            }
        }
        if (points.isEmpty()) throw new AgentException("client.pixels: 'points' needs at least one point");
        return points;
    }

    /** One recorded draw of the bar, with the droplet rectangles worked out from it. */
    private static JsonElement bar(HudRecord.Bar drawn) {
        if (drawn == null) return JsonNull.INSTANCE;
        JsonObject result = new JsonObject();
        result.addProperty("left", drawn.left());
        result.addProperty("top", drawn.top());
        result.addProperty("right", drawn.right());
        result.addProperty("bottom", drawn.top() + HudRecord.ICON_SIZE);
        result.addProperty("width", HudRecord.BAR_WIDTH);
        result.addProperty("height", HudRecord.ICON_SIZE);
        result.addProperty("thirst", drawn.thirst());
        result.addProperty("quenched", drawn.quenched());
        result.addProperty("exhaustion", ServerProbes.round(drawn.exhaustion()));
        result.addProperty("overlay", drawn.overlay());
        result.addProperty("exhaustionStrip", drawn.exhaustionStrip());
        result.addProperty("shake", drawn.shake());
        result.addProperty("drawnMsAgo", drawn.ageMillis());
        // Droplet 0 is the rightmost, the way the HUD draws them.
        JsonArray droplets = new JsonArray();
        for (int i = 0; i < HudRecord.ICONS; i++) {
            JsonObject droplet = new JsonObject();
            int x = drawn.right() - i * HudRecord.ICON_STRIDE - HudRecord.ICON_SIZE;
            droplet.addProperty("index", i);
            droplet.addProperty("left", x);
            droplet.addProperty("top", drawn.top());
            droplet.addProperty("centreX", x + HudRecord.ICON_SIZE / 2);
            droplet.addProperty("centreY", drawn.top() + HudRecord.ICON_SIZE / 2);
            droplets.add(droplet);
        }
        result.add("droplets", droplets);
        return result;
    }

    private static JsonObject movement(Minecraft minecraft) {
        LocalPlayer player = player(minecraft);
        JsonObject result = new JsonObject();
        result.addProperty("sprinting", player.isSprinting());
        result.addProperty("sneaking", player.isShiftKeyDown());
        result.addProperty("x", ServerProbes.round(player.getX()));
        result.addProperty("y", ServerProbes.round(player.getY()));
        result.addProperty("z", ServerProbes.round(player.getZ()));
        result.add("thirst", ServerProbes.state(ThirstManager.get(player)));
        return result;
    }

    private static ItemStack stack(AgentRequest request) {
        String name = request.string("item");
        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(name))
                .orElseThrow(() -> new AgentException("client.tooltip: no item called '" + name + "'"));
        return new ItemStack(item, request.integer("count", 1));
    }

    private static String screenName(Minecraft minecraft) {
        Screen screen = AgentClientVanilla.screen(minecraft);
        return screen == null ? null : screen.getClass().getName();
    }

    private static Minecraft client() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) throw new AgentException("there is no client in this process");
        return minecraft;
    }

    private static LocalPlayer player(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            throw new AgentException("no player on this client; it is at " + screenName(minecraft));
        }
        return player;
    }
}
