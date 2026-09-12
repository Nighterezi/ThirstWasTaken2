# P0 spike: the 1.21.1 node

What a `1.21.1` build node actually costs, measured rather than estimated. Run on 2026-09-12 against
`main` at 85436d7. This is the record the [platform plan](PLATFORM-PLAN.md) gates P1 and P2 on;
delete it when P3 lands.

The node itself is **not in the tree**. It was stood up, built, measured and reverted, because a
version in `stonecutter.properties.toml` is a version CI builds, and this one does not compile yet.
Everything needed to stand it up again is below.

## How it was measured

1. `versions("1.21.1")` in `settings.gradle.kts`, a `["1.21.1"]` table in
   `stonecutter.properties.toml`, then `:1.21.1:build`.
2. The compiler was run until it stopped finding new causes. It never got past `main`, so `client`,
   `gametest` and `dev` never reached javac.
3. For those three, every `net.minecraft` / `net.fabricmc` / `com.mojang` import in the **generated**
   Stonecutter sources under `versions/1.21.1/build/generated/stonecutter/` was checked against the
   38,675 classes on the node's own compile classpath.
4. Resource formats were read out of the 1.21.1 Minecraft jar itself, from vanilla's own recipes,
   advancements and tags, rather than from memory.

So `main` is compiler-verified; `client`, `gametest` and `dev` are import-verified. A file whose
imports all resolve can still fail on a moved method, and one did in `main`
(`Commands.LEVEL_GAMEMASTERS`), so treat the counts below as a floor.

## The three numbers

**Files broken: 23 of 58 Java files, and 31 of 57 JSON resource files.**

**Seam shaped against structural**, counting each Java file once at its hardest break:

| Kind | Files | What it costs |
|---|---|---|
| Pure rename | 5 | Nothing. Two lines in `stonecutter.gradle.kts`, no file edited |
| Seam shaped | 2 | Branches inside `platform/`; callers never learn |
| Mixin fork | 1 | Permitted by [AGENTS.md](../../AGENTS.md); the body still delegates |
| Gametest harness | 11 | Mechanical and identical in all eleven; no test logic moves |
| Structural | 4 | Real forks: two subsystems that do not exist on 1.21.1 |

**Overlap with P1 and P2: total, in both directions.** Details below. The plan's unverified claim
holds, so the P1, P2, P3 ordering stands and P3 does not get to start early.

## Java, file by file

Pure rename, absorbed by `replacements`, nothing edited:

- `ThirstAdvancements`, `ThirstApi`, `ThirstWasTaken2`, `WaterPurity` for `Identifier`
- `ThirstConfigScreen` for `net.minecraft.util.Util`

Seam shaped, the fix lands in `platform/`:

- `Vanilla` for `FontDescription` (1.21.9 and later; before it, `Style#withFont` took the id
  directly) and `EnvironmentAttributes` (1.21.9 and later; before it, `DimensionType#ultraWarm`).
  `waterEvaporates` and `dropletFont` are already the right seams, but both imports sit outside the
  branch, so they break the file rather than the method.
- `ThirstCommands` for `Commands.LEVEL_GAMEMASTERS.check(source.permissions())` against
  `source.hasPermission(2)`. This is a **missing seam**: per AGENTS.md, `command/` should not need to
  know. It wants a `Vanilla.isGameMaster(CommandSourceStack)`.

Mixin fork, mandatory rather than optional:

- `ItemStackMixin` for `TooltipDisplay`, which is 1.21.5 and later, and because the target
  `addDetailsToTooltip` is `appendHoverText` on 1.21.1. `thirstwastaken2.mixins.json` sets
  `"required": true`, so leaving it is a startup crash, not a missing tooltip.

Structural, the two subsystems that genuinely do not exist:

- `ThirstItems` and `WaterskinItem`, because `Consumable` and `Consumables` are the 1.21.2 and later
  component system. On 1.21.1 drinking is `Item.Properties#food` plus `UseAnim`. This is the plan's
  drink item fork.
- `ThirstHud` and `ThirstWasTaken2Client`, because `HudElementRegistry`, `VanillaHudElements` and
  `HudStatusBarHeightRegistry` are 1.21.6 and later, and `RenderPipelines` is 1.21.5 and later. On
  1.21.1 it is `HudRenderCallback` and immediate mode blits. This is the plan's HUD fork.

`src/dev` breaks on nothing. `TestFixtures` breaks on nothing.

## The gametests can run on 1.21.1

This was the open question that decides whether the matrix stays testable, and the answer is yes.

Every import in all twelve test classes resolves on 1.21.1 except one,
`net.fabricmc.fabric.api.gametest.v1.GameTest`. Fabric API 0.116.17+1.21.1 ships only
`net.fabricmc.fabric.api.gametest.v1.FabricGameTest`, the older marker interface, and the annotation
is vanilla's `net.minecraft.gametest.framework.GameTest`. The whole vanilla framework, including
`GameTestHelper`, the assertion exceptions and `GameTestServer`, is present and unchanged.

So the fork is the harness and nothing else:

- swap the import in 11 classes,
- `implements FabricGameTest` on 11 class headers,
- `@GameTest` becomes `@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)` at roughly 30 sites,
- one `fabric-gametest` entrypoint per class in the gametest mod's manifest.

No assertion, no fixture and no test body changes. **The exit ramp that says a node which cannot run
gametests does not ship is not triggered.**

## Resources fork harder than the code does

This is the part that was not in the plan, and it is the reason P1 has to come first.

Directory names are already singular on 1.21.1, so `recipe/`, `advancement/`, `damage_type/` and
`tags/damage_type/` stay where they are. The contents fork anyway:

| Files | Break |
|---|---|
| 22 recipes | An ingredient is `{"item": "minecraft:potato"}` on 1.21.1 and a bare `"minecraft:potato"` from 1.21.2. Every recipe the mod ships uses the bare form |
| 7 of those 22 | `minecraft:custom_model_data` is a plain `int` on 1.21.1, not `{"floats": [...]}` |
| 6 item definitions | `assets/<ns>/items/` does not exist before 1.21.4. The classpath still carries `ItemProperties` and `ItemPropertyFunction`, so 1.21.1 is the `overrides` era |
| 2 item models | `waterskin` and `terracotta_water_bowl` need their `range_dispatch` rewritten as `overrides` plus a client side `ItemProperties.register` |
| `advancement/root.json` | `background` is a texture path on 1.21.1, `minecraft:textures/gui/advancements/backgrounds/stone.png`, not a block id |

Unchanged: the damage type, both tag files, the font, all nine lang files, every texture, both
manifests, 10 of 12 item models and 13 of 14 advancements. `"fabric:type"` is the custom ingredient
key on 1.21.1 as well, so Fabric's components ingredient survives; only the `base` inside it changes
shape.

## Overlap

**With P1, total.** All 31 forking resource files are files P1 moves to datagen. Hand forking 22
recipes and six item definitions against a second format is precisely the work a generator keyed by
Minecraft version does for free. Doing P3 first means writing that fork by hand and then throwing it
away.

**With P2, total, and on the smallest possible set.** The four structural Java forks are item
registration and HUD registration. Those are also the two surfaces a loader seam has to abstract:
NeoForge registers items through a deferred register and draws HUD elements through its own event.
P2 and P3 fork the same four files, so building `platform/Loader` while there is still one loader
puts the seam in before two axes have to meet in it.

## Answers to the plan's open questions

**Dependency versions.** Resolved and built:

```toml
["1.21.1"]
mod.mc_compat = ">=1.21 <=1.21.1"
mod.mc_releases = ["1.21", "1.21.1"]

deps.fabric_api = "0.116.17+1.21.1"
deps.modmenu = "11.0.4"
deps.appleskin = "b5ZiCjAr"      # 3.0.6+mc1.21, Fabric
deps.cloth_config = "15.0.140+fabric"
```

Fabric Loader 0.19.3, the one global value, resolves on 1.21.1. No second loader version.

**`loomx.loom_version` does not need a per-version value.** Loom 1.17.20 configured the node,
resolved 1.21.1 through `loom.mappings.1_21_1.layered`, remapped Fabric API and reached javac. The
plan expected this to be a problem. It is not one.

**The rename threshold.** Two renames, not one, and neither needs a real Minecraft release number;
writing them against the nodes that exist is enough:

```kotlin
string(current.parsed < "1.21.11") {
    replace("Identifier", "ResourceLocation")
    replace("net.minecraft.util.Util", "net.minecraft.Util")
}
```

The first rule alone took the build from 25 errors in 8 files to 8 errors in 5.

**Whether the gametest API works.** Yes, see above.

## Revised estimates

The plan's day counts were unvalidated. These are still estimates, but they are anchored to a real
build now.

| Phase | Plan | Now | Why |
|---|---|---|---|
| P1 | 2 to 3 days | **3 to 4 days** | The generators have to carry two output formats, not one, or P3 pays it back twice |
| P2 | 2 to 3 days | 2 to 3 days | Unchanged; the four files it touches are known now |
| P3 | 5 to 8 days | **4 to 6 days** | Two renames and one Loom version turned out free, and the gametests need no rewrite |

P3 shrinks only if P1 lands first. Taken alone it is still 8 days or more, because the resource fork
does not go away, it just moves into 31 hand edited files.
