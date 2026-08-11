# src/client/java — client-only code

The split source set (`loom.splitEnvironmentSourceSets()`). Everything touching
`net.minecraft.client` lives here and **nowhere else**; common code in `src/main/java` may not
reference this package. The reverse is fine — this code reads `ThirstConfig`, `ThirstManager` and
`ThirstData` directly.

Nothing here is authoritative. The client renders the `ThirstData` attachment the server synced to it
(`AttachmentSyncPredicate.targetOnly()`), and the config screen edits a config the server ignores for
everything except the HUD section.

| File | Owns |
|---|---|
| `ThirstWasTaken2Client` | the `client` entrypoint: HUD element + status-bar height registration |
| `ThirstHud` | drawing the bar |
| `config/ThirstConfigScreen` | the vanilla-styled options screen |
| `compat/ModMenuIntegration` | the `modmenu` entrypoint |

## HUD

`HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, …)` places the bar, and
`HudStatusBarHeightRegistry.addRight` reserves 10px so vanilla stacks around it — `ThirstHud.render`
then reads that height back to find its own Y. Both use the same `thirstwastaken2:thirst_bar` id;
changing it means changing all three sites.

Sprite geometry, which is easy to break:

- `thirst_icons.png` is 41x9 — five 9px frames (empty, quarter, half, three quarter, full) on an
  **8px stride**, because neighbouring frames share their transparent edge column. Hence
  `U_EMPTY = 0` and `FILL_FRAMES = {8, 16, 24, 32}`.
- Each droplet holds two thirst points, so `FILL_THRESHOLDS` is `{0.5, 1.0, 1.5, 2.0}` against
  `level - i * 2`.
- The quarter and three-quarter frames come from `drainedFraction`, which spends the synced
  `exhaustion` (0..4) against the next point — and only once quenched is empty, so a quenched player
  never shows a partially drained droplet. There is no setting for this.
- When AppleSkin is loaded and its exhaustion-underlay option is enabled, `AppleSkinIntegration`
  exposes that setting and `ThirstHud` draws the synced exhaustion as the `v = 18` dither strip.
- The quenched outline comes from `appleskin_icons.png` row `v = 0`, at `u = 0/9/18/27` by quarter,
  blitted with the 256x256 texture size. That sheet is 256x256, unlike `thirst_icons.png` — do not
  copy blit arguments between the two.
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

Doubles are edited as integer percentages (`percentSlider`, `PERCENT = 100`) because the vanilla
slider is integer-only. Only scalars are exposed; maps and keyword patterns stay in the JSON, which
the footer button opens with `Util.getPlatform().openPath`.

Mod Menu is `clientCompileOnly`. `ModMenuIntegration` is only ever class-loaded when Mod Menu itself
resolves the entrypoint, so nothing else may reference it.
