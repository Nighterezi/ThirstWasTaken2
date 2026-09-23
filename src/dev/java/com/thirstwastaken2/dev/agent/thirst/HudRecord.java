package com.thirstwastaken2.dev.agent.thirst;

/**
 * Where the thirst bar was last drawn, recorded as it was drawn.
 *
 * <p>This is the difference between reading a screenshot and reading a number. The HUD works out the
 * bar's right edge from the screen width, the configured offset and the row the loader handed it; that
 * last part is the whole of "the bar sits above the hunger bar, and the air bubbles sit above the bar",
 * and it cannot be recomputed here without recomputing the thing under test. So it is taken from the
 * draw call itself, by a mixin in this source set — the mod is not changed for the agent's sake.
 *
 * <p>The config screen's preview draws the same bar through the same method. It is recorded separately
 * rather than thrown away, because "the preview looks like the HUD" is then a comparison of two
 * rectangles instead of two screenshots.
 *
 * <p>Written from the render thread and read from the client tick, so every field is volatile and each
 * reading is one immutable {@link Bar}.
 */
public final class HudRecord {
    /**
     * The icon geometry, copied from {@code ThirstHud}, where it is private. Ten droplets, each nine
     * pixels wide, overlapping by one because the frames on the sheet share their transparent edge
     * column — so the bar is 81 pixels wide, not 90. A change to those constants has to be made here
     * too, and the {@code hud.geometry} answer says which ones were used.
     */
    public static final int ICON_SIZE = 9;
    public static final int ICON_STRIDE = 8;
    public static final int ICONS = 10;
    public static final int BAR_WIDTH = 81;

    /** One recorded draw of the bar. */
    public record Bar(int right, int top, int thirst, int quenched, float exhaustion, String overlay,
                      boolean exhaustionStrip, boolean shake, boolean parched, boolean upsetStomach,
                      long at) {
        /** The left edge: the ten droplets end at {@code right}, and the leftmost starts here. */
        public int left() {
            return right - BAR_WIDTH;
        }

        /** How long ago this was drawn. The HUD redraws every frame, so anything old means it stopped. */
        public long ageMillis() {
            return System.currentTimeMillis() - at;
        }
    }

    /** Bounds of one vanilla HUD row, built from the sprite draw calls in its latest frame. */
    public record SpriteRow(int left, int top, int right, int bottom, int lastX, int sprites, long at) {
        public int width() {
            return right - left;
        }

        public int height() {
            return bottom - top;
        }

        public long ageMillis() {
            return System.currentTimeMillis() - at;
        }
    }

    private static volatile Bar hud;
    private static volatile Bar preview;
    private static volatile SpriteRow food;
    private static volatile SpriteRow air;
    /** Set while {@code ThirstHud.render} is running, so a preview draw is not mistaken for the HUD. */
    private static volatile boolean inHud;
    /** How many times the HUD row has been asked to draw, whether or not it decided to. */
    private static volatile long rows;
    private static volatile boolean installed;

    private HudRecord() { }

    /** The last HUD draw, or null when the bar has never been drawn in this session. */
    public static Bar hud() {
        return hud;
    }

    /** The last config-screen preview draw, or null when the screen has not been opened. */
    public static Bar preview() {
        return preview;
    }

    public static long rows() {
        return rows;
    }

    /** Whether the recording mixin applied. False means every rectangle here is missing, not zero. */
    public static boolean installed() {
        return installed;
    }

    public static SpriteRow food() {
        return food;
    }

    public static SpriteRow air() {
        return air;
    }

    /** Called by the mixin as {@code ThirstHud.render} starts. */
    public static void beginHud() {
        installed = true;
        rows++;
        inHud = true;
    }

    /** Called by the mixin as {@code ThirstHud.render} returns, however it returned. */
    public static void endHud() {
        inHud = false;
    }

    /** Called by the mixin at the head of {@code ThirstHud.drawBar}. */
    public static void bar(int right, int top, int thirst, int quenched, float exhaustion, String overlay,
                          boolean exhaustionStrip, boolean shake, boolean parched, boolean upsetStomach) {
        installed = true;
        Bar drawn = new Bar(right, top, thirst, quenched, exhaustion, overlay, exhaustionStrip, shake, parched,
                upsetStomach, System.currentTimeMillis());
        if (inHud) hud = drawn;
        else preview = drawn;
    }

    /** A sprite draw at the screen position {@code at}, the drawn position put through the pose. */
    public static void sprite(String id, org.joml.Vector2fc at, int width, int height) {
        sprite(id, Math.round(at.x()), Math.round(at.y()), width, height);
    }

    /** As {@link #sprite(String, org.joml.Vector2fc, int, int)}, for 1.21.1's three-dimensional pose. */
    public static void sprite(String id, org.joml.Vector3fc at, int width, int height) {
        sprite(id, Math.round(at.x()), Math.round(at.y()), width, height);
    }

    /** Called for atlas sprite draws; only vanilla food backgrounds and air bubbles are retained. */
    public static synchronized void sprite(String id, int x, int y, int width, int height) {
        if (id.contains("hud/food_empty")) {
            food = extend(food, x, y, width, height);
        } else if (id.contains("hud/air")) {
            air = extend(air, x, y, width, height);
        }
    }

    /** A row starts at its rightmost icon and proceeds left; a larger x therefore starts a new frame. */
    private static SpriteRow extend(SpriteRow row, int x, int y, int width, int height) {
        long now = System.currentTimeMillis();
        if (row == null || x > row.lastX() || now - row.at() > 250L) {
            return new SpriteRow(x, y, x + width, y + height, x, 1, now);
        }
        return new SpriteRow(Math.min(row.left(), x), Math.min(row.top(), y),
                Math.max(row.right(), x + width), Math.max(row.bottom(), y + height),
                x, row.sprites() + 1, now);
    }
}
