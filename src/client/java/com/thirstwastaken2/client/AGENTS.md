# src/client/java — client-only code

The split source set (`loom.splitEnvironmentSourceSets()`). Everything touching
`net.minecraft.client` lives here and **nowhere else**; common code in `src/main/java` may not
reference this package. The reverse is fine — this code reads `ThirstConfig`, `ThirstManager` and
`ThirstData` directly.

Nothing here is authoritative. The client renders the `ThirstData` the server synced to its owner
alone (`ThirstData.STORAGE`), and the config screen edits a config the server ignores for everything
except the HUD section.

| File | Owns |
|---|---|
| `ThirstWasTaken2Client` | `initialize`, called by the loader's client entrypoint: registers the HUD row |
| `ThirstHud` | drawing the bar |
| `HandDrinking` | hand drinking from water the crosshair misses: picks again with fluids and sends vanilla's use-on-block packet on the water |
| `config/ThirstConfigScreen` | the root options screen: preview, a button per page, Cancel and Done |
| `config/ThirstCategoryScreen` | one page as a vanilla options list, with Reset to Defaults |
| `config/ConfigCategory` | every page: its widgets and what its reset puts back |
| `config/ConfigOptions` | the widget factories (`toggle`, `slider`, `cycle`, ...) and their lang keys |
| `config/ConfigPreview` | the live thirst bar, food bar and tooltip drawn at the top of the screen |
| `platform/ClientVanilla` | client vanilla calls whose shape differs between Minecraft versions |
| `platform/StatusBarRenderer` | the shape `ClientLoader` draws a HUD row through |
| `compat/AppleSkinIntegration` | reads AppleSkin's own settings, only after `AppleSkin.isLoaded()` |
| `compat/JadeIntegration` | the `jade` entrypoint: the grade of the water under the crosshair, see `compat/AGENTS.md` |
| `mixin/MinecraftMixin` | every version and loader: calls `HandDrinking` at the start of a right click. Listed in `src/client/resources/thirstwastaken2.client.mixins.json` |
| `mixin/LocalPlayerMixin` | 1.21.1 only, on both loaders: the thirst sprint gate, on the client player's own food check. Same config |

Loader code for the client lives in `src/client/<loader>/java`, never here, and `checkLoaderSeam`
fails the build on a loader import in this directory:

| File | Owns |
|---|---|
| `client/fabric/ThirstWasTaken2FabricClient` | the Fabric `client` entrypoint |
| `client/platform/ClientLoader` | HUD layer and status bar height registration, per loader |
| `client/compat/ModMenuIntegration` | the `modmenu` entrypoint; Mod Menu is a Fabric-only mod |
| `fabric/mixin/GuiMixin` | 1.21.1 only: the status bar registry Fabric API gained in 1.21.6 |
| `client/neoforge/ThirstWasTaken2NeoForgeClient` | the NeoForge `@Mod(dist = CLIENT)` class; also registers the config screen as the mods list's `IConfigScreenFactory` |

`ClientVanilla` is the client half of `com.thirstwastaken2.platform.Vanilla` and follows the same
rules — plumbing only, one signature on every version. A Stonecutter `//?` branch anywhere else in
this source set means a seam is missing from it. The HUD draw target is the exception that needs no
branch: 26.1 renamed `GuiGraphics` to `GuiGraphicsExtractor` and left every drawing method this mod
uses untouched, so `stonecutter.gradle.kts` renames the type back for older versions instead.

## HUD

`ClientLoader.addRightStatusBar` places the bar after the food bar and reserves 10px of the right-hand
stack while `ThirstHud.shouldRender` holds, so vanilla stacks around it. On Fabric that is
`HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, …)` plus
`HudStatusBarHeightRegistry.addRight`, under the one `thirstwastaken2:thirst_bar` id. The loader
reads the stack height back and hands `ThirstHud.render` the row's `top`; the Y offset setting is
added on top of that.

Fabric API has neither registry before 1.21.6. On 1.21.1 `ClientLoader` keeps the rows itself and
`GuiMixin` (in `src/client/fabric`) draws them where vanilla is about to draw the air bubbles, 49px up
from the bottom for the one row, then translates the bubbles up by the height it used. That also
means the bar is only drawn when vanilla draws the health and food bars, as on later versions.

Every blit goes through `ClientVanilla.blit`, because 1.21.1 has no render pipelines: the tint there
is shader colour state, set before the draw and reset after it.

Sprite geometry, which is easy to break:

- `thirst_icons.png` is 41x9 — five 9px frames (empty, quarter, half, three quarter, full) on an
  **8px stride**, because neighbouring frames share their transparent edge column. Hence
  `U_EMPTY = 0` and `FILL_FRAMES = {8, 16, 24, 32}`.
- Each droplet holds two thirst points, so `FILL_THRESHOLDS` is `{0.5, 1.0, 1.5, 2.0}` against
  `level - i * 2`.
- `thirst_icons_parched.png` is the same sheet in dry sand, drawn instead while the player has
  Parched, the way vanilla swaps to `food_*_hunger` for Hunger. Same size and frames, so every blit
  argument is shared. `tools/generate_parched_icons.py` draws it from `thirst_icons.png`; rerun it
  after touching the droplets rather than editing the parched sheet. The config preview always
  draws the plain sheet.
- `thirst_icons_upset_stomach.png` is the same sheet in a venom green, drawn while the player has
  Upset Stomach. It wins over Parched when both are on. `tools/generate_upset_stomach_bar.py` draws
  it from `thirst_icons.png` by swapping its nine blues for a hand-drawn ramp; rerun it rather than editing the sheet.
- The quarter and three-quarter frames come from `drainedFraction`, which spends the synced
  `exhaustion` (0..4) against the next point — and only once quenched is empty, so a quenched player
  never shows a partially drained droplet. There is no setting for this.
- The server syncs exhaustion in quarter-point steps (`ThirstManager.SYNC_STEP`), not every tick. The
  partial frames change at 0 and 2, both on a step, and the AppleSkin strip moves about 5 px a step.
  Do not add a client feature that needs finer exhaustion without revisiting that step.
- When AppleSkin is loaded, its exhaustion-underlay option is enabled and the quenched outline is not
  `OFF`, `AppleSkinIntegration` says so and `ThirstHud` draws the synced exhaustion as the `v = 18` dither strip of
  `appleskin_icons.png`, blitted with the 256x256 texture size.
- The quenched outline is AppleSkin-only, like the strip: `compat/AppleSkin.quenchedOverlay()` (common
  code) returns `OFF` without AppleSkin, and otherwise the player's `appleskinQuenchedOverlay`. It comes
  from `quenched_overlay.png`, 36x45, at `u = 0/9/18/27` by quarter and `v = ordinal * 9`, so
  `QuenchedOverlay`'s order is the sheet's row order. Every sheet here has its own texture size — do
  not copy blit arguments between them.
- `tools/generate_quenched_overlay.py` draws `quenched_overlay.png` and the matching tooltip glyphs.
  Edit the palettes there and rerun it rather than touching the PNGs.
- The bar shakes when quenched hits zero, mirroring vanilla hunger (`shakePeriod = thirst * 3 + 1`).
- `shouldRender` deliberately does not ask whether the player is alive. Vanilla draws the food bar
  for a dead player, so the hunger bar stays on screen behind the death screen, and a thirst row
  that hid itself there would leave a gap above it. `tools/agent/hud-death-screen.jsonl` is the
  check.

`ThirstTooltip` (common) uses the same two-units-per-droplet rule with its own bitmap font. If the
fill thresholds change here, change them there too.

## Config screen

`ThirstConfigScreen` is the page Mod Menu opens: `ConfigPreview` on top, a button per
`ConfigCategory`, and Cancel and Done. Each button opens a `ThirstCategoryScreen`, a vanilla
`OptionsSubScreen` whose list `ConfigCategory.addOptions` fills; the HUD page also puts the preview
under its title, in a taller header.

`OptionInstance` widgets write **straight into the live `ThirstConfig` instance**, so the HUD, the
tooltips and the preview follow every change at once. Leaving a page saves nothing. Done or Escape on
the root screen calls `ThirstConfig.commit()`, which re-sanitises, bumps the generation and saves;
Cancel calls `ThirstConfig.restore` with the snapshot the root screen took when it was constructed.
So a widget's range must not be wider than the clamp in `ThirstConfig.sanitize()`, or the value
silently snaps back.

A vanilla screen's `init()` runs once; coming back from a page only repositions it. That is why the
root screen can keep its layout in a final field. Reset to Defaults copies the page's fields from a
`new ThirstConfig()` through `ConfigCategory.reset` and opens a fresh page, because widgets keep the
value they were built with.

Adding a setting means: field in `ThirstConfig`, clamp in `sanitize()`, a widget and a reset line in
its `ConfigCategory`, and `thirstwastaken2.config.<key>` plus `thirstwastaken2.config.<key>.tooltip`
in `en_us.json` and `vi_vn.json` (the other seven locales are best-effort). `ConfigOptions` builds the
key from the snake_case string passed to `toggle`/`slider`, so that string is the lang key — keep it
matching the Java field name. A new page also needs `category.<key>` and `category.<key>.tooltip`.

Enums use `cycle`, an `OptionInstance.Enum` labelled by `<key>.<value in lower case>`, so each value
needs its own lang key as well. The cycle button writes the option's name in front of the value
itself, so the label function returns the value alone; returning `caption: value` there prints the
name twice. The AppleSkin section is always shown; without AppleSkin it adds a note saying the
settings do nothing yet.

Doubles are edited as integer percentages (`percentSlider`, `PERCENT = 100`) because the vanilla
slider is integer-only. Only scalars are exposed; maps and keyword patterns stay in the JSON, which
the Item Values page opens with `Util.getPlatform().openPath`.

Not every version of `OptionsList` takes a plain widget, so that button goes through
`ClientVanilla.addFullWidthRow`, and 1.21.1 has no section headings, so those go through
`ClientVanilla.addHeader`, which stands a centred text row in for one. The preview draws through
`ClientVanilla.canvas`, `text` and `blitSprite`, because 26.1 renamed the widget draw method and the
text call, and 1.21.11 added the render pipeline to sprite draws. It never builds an `ItemStack`: Mod
Menu opens the screen from the title screen, where 26.1 and later have not bound item components yet
and constructing a stack crashes the game. 26.2 moved
`setScreen` onto `Minecraft.gui`, hence `ClientVanilla.setScreen`.

Mod Menu is `clientCompileOnly`. `ModMenuIntegration` (in `src/client/fabric/java`) is only ever
class-loaded when Mod Menu itself resolves the entrypoint, so nothing else may reference it.
