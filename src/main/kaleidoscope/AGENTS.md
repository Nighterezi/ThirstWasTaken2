# src/main/kaleidoscope — water quality in Kaleidoscope Cookery's stockpot and teapot

[Kaleidoscope Cookery](https://modrinth.com/mod/kaleidoscope-cookery) (mod id `kaleidoscope_cookery`)
keeps water in two blocks, the stockpot and the teapot, and both keep only a fluid id, so the grade a
bucket had is lost on the way in. This directory is where that gets fixed. The plan, the order of work
and what is still to do are in
[docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md](../../../docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md).

**Today it is the gate and nothing behind it**: the dependency, the source set, the mixin plugin and an
empty mixin config. The stockpot and teapot mixins are items 3 and 4 of the plan.

The drink and soup values are not here. They are ids in `ThirstConfig`, common code that names no class
of the mod, so they reach every node, including those that do not compile this directory and a player
on the unsupported official Fabric build.

## Which build

| Node | Mod | Modrinth project |
|---|---|---|
| `1.21.1-neoforge` | the official mod | `kaleidoscope-cookery` |
| `1.21.1`, `1.21.11`, `26.1.x`, `26.2.x`, `26.3.x` | Refabricated, the Fabric port | `kaleidoscope-cookery-refabricated` |

The official mod has no NeoForge build past 1.21.1, so the other NeoForge nodes do not set the key and
do not compile this directory. The official Fabric build stopped at 1.0.1, before the teapot, and is not
supported. Refabricated has the same mod id and the same package, so one directory serves both, and
since it names no loader and no fluid API, **both loaders compile it**, as with
[src/main/supplementaries](../supplementaries/AGENTS.md). Each key is pinned by Modrinth version id; the
Fabric uploads of different Minecraft versions share one version number. `1.21.11` is frozen upstream at
1.3.0.9, and `update_mc_deps.py` leaves it alone.

On `1.21.1` and `1.21.11` Refabricated requires Forge Config API Port, `deps.forge_config_api_port`, on
the `runClient` classpath only. Its Night Config is nested in its jar, which Loom does not unpack into a
run, so `nestedMods` in `build.gradle.kts` takes it out, as it does Moonlight's CodecUI. From 26.1 on it
is optional and no table names it.

```
kaleidoscope/java/com/thirstwastaken2/kaleidoscope/
  KaleidoscopePresence       the gate: a classpath probe for the teapot, then one per mixin target
  KaleidoscopeMixinPlugin    applies each mixin only where the gate allows it
kaleidoscope/resources/
  thirstwastaken2.kaleidoscope.mixins.json
```

## How it stays optional

The same three layers as Supplementaries.

1. **Build.** Only when `deps.kaleidoscope_cookery` is set, both loader scripts add this directory and
   append the mixin config to the built manifest (on NeoForge with `kaleidoscope_cookery` as an optional
   dependency), from [its row in the integration table](../../../build-logic/src/main/kotlin/com/thirstwastaken2/buildlogic/Integrations.kt). Both loaders compile it, so `checkLoaderSeam` keeps it free of either loader's
   API. Fabric needs it remapped, since it is mixed into, so it
   is `modCompileOnly`; NeoForge takes it as `compileOnly`. Both put it on `runClient` only, so
   `runServer` and the gametests run without it, and `-PwithoutOptional=kaleidoscope_cookery` (or
   `kaleidoscope-cookery`, `kaleidoscope-cookery-refabricated`) leaves it out of `runClient` as well.
2. **Runtime gate.** `KaleidoscopePresence` answers every question with a resource lookup, which never
   loads a class, and names no class of the mod's, no Minecraft class and no loader. The teapot's class
   is the version check: the official Fabric 1.0.1 has the same mod id and package but no teapot, so
   without it the whole integration is off and one warning says to install Refabricated. Past that,
   each target is probed by name, and a missing one logs once and is skipped.
3. **Mixin plugin.** `KaleidoscopeMixinPlugin.shouldApplyMixin` asks the gate for the mixin's own
   target, so a class renamed upstream only takes its own mixins down. `onLoad` asks too, so the warning
   about an unsupported build is logged at startup whatever the config lists.

Mixins here match their targets **by name alone, with no descriptor**, as in Supplementaries, so it
does not matter that the Fabric jar uses intermediary names for Minecraft types.

## Checking it

- `checkOptionalSeam` finds the plugin and the gate as classes loaded without the mod, and passes.
- `runGametest` passes unchanged on every node: the mod is never on its classpath.
- `./gradlew ":<node>:runClient" -Pagent=tools/agent/boot.jsonl`, with the mod and with
  `-PwithoutOptional=kaleidoscope_cookery`, comes up and stays up.
