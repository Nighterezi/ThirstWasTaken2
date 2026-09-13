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
| `config/ThirstConfigScreen` | the vanilla-styled options screen |
| `platform/ClientVanilla` | client vanilla calls whose shape differs between Minecraft versions |
| `platform/StatusBarRenderer` | the shape `ClientLoader` draws a HUD row through |

Loader code for the client lives in `src/client/fabric/java`, never here, and `checkLoaderSeam` fails
the build on a loader import in this directory:

| File | Owns |
|---|---|
| `client/fabric/ThirstWasTaken2FabricClient` | the Fabric `client` entrypoint |
| `client/platform/ClientLoader` | HUD layer and status bar height registration, per loader |
| `client/compat/ModMenuIntegration` | the `modmenu` entrypoint; Mod Menu is a Fabric-only mod |
| `fabric/mixin/GuiMixin` | 1.21.1 only: the status bar registry Fabric API gained in 1.21.6 |

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
- The quarter and three-quarter frames come from `drainedFraction`, which spends the synced
  `exhaustion` (0..4) against the next point — and only once quenched is empty, so a quenched player
  never shows a partially drained droplet. There is no setting for this.
- The server syncs exhaustion in quarter-point steps (`ThirstManager.SYNC_STEP`), not every tick. The
  partial frames change at 0 and 2, both on a step, and the AppleSkin strip moves about 5 px a step.
  Do not add a client feature that needs finer exhaustion without revisiting that step.
- When AppleSkin is loaded and its exhaustion-underlay option is enabled, `AppleSkinIntegration`
  exposes that setting and `ThirstHud` draws the synced exhaustion as the `v = 18` dither strip of
  `appleskin_icons.png`, blitted with the 256x256 texture size.
- The quenched outline is AppleSkin-only, like the strip: `compat/AppleSkin.quenchedOverlay()` (common
  code) returns `OFF` without AppleSkin, and otherwise the player's `appleskinQuenchedOverlay`. It comes
  from `quenched_overlay.png`, 36x27, at `u = 0/9/18/27` by quarter and `v = ordinal * 9`, so
  `QuenchedOverlay`'s order is the sheet's row order. Every sheet here has its own texture size — do
  not copy blit arguments between them.
- `tools/generate_quenched_overlay.py` draws `quenched_overlay.png` and the matching tooltip glyphs.
  Edit the palettes there and rerun it rather than touching the PNGs.
- The bar shakes when quenched hits zero, mirroring vanilla hunger (`shakePeriod = thirst * 3 + 1`).

`ThirstTooltip` (common) uses the same two-units-per-droplet rule with its own bitmap font. If the
fill thresholds change here, change them there too.

## Config screen

`OptionInstance` widgets write **straight into the live `ThirstConfig` instance**; `onClose` calls
`ThirstConfig.commit()`, which re-sanitises, bumps the generation and saves. So a widget's range must
not be wider than the clamp in `ThirstConfig.sanitize()`, or the value silently snaps back.

Adding a setting means: field in `ThirstConfig`, clamp in `sanitize()`, a widget here, and
`thirstwastaken2.config.<key>` plus `thirstwastaken2.config.<key>.tooltip` in `en_us.json` and
`vi_vn.json` (the other seven locales are best-effort). `translationKey()` builds the key from the
snake_case string passed to `toggle`/`slider`, so that string is the lang key — keep it matching the
Java field name.

Enums use `cycle`, an `OptionInstance.Enum` labelled by `<key>.<value in lower case>`, so each value
needs its own lang key as well. The AppleSkin section is always shown; without AppleSkin it adds a
note saying the settings do nothing yet.

Doubles are edited as integer percentages (`percentSlider`, `PERCENT = 100`) because the vanilla
slider is integer-only. Only scalars are exposed; maps and keyword patterns stay in the JSON, which
the footer button opens with `Util.getPlatform().openPath`.

Not every version of `OptionsList` takes a plain widget, so the footer button goes through
`ClientVanilla.addFullWidthRow`, and 1.21.1 has no section headings, so those go through
`ClientVanilla.addHeader`, which stands a centred text row in for one.

Mod Menu is `clientCompileOnly`. `ModMenuIntegration` (in `src/client/fabric/java`) is only ever
class-loaded when Mod Menu itself resolves the entrypoint, so nothing else may reference it.
