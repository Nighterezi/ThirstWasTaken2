# ThirstWasTaken2

A thirst mod for **Minecraft 26.2, 26.1.x, 1.21.11 and 1.21.1** on **Fabric** and **NeoForge**: a
thirst bar, water purity and drinking. It began as a port of
[Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) (Forge, 1.19.2) and has diverged, so
upstream is a reference, not a spec. Its source is expected at `../Thirst-Mod` when comparing.

- Mod id and resource namespace: `thirstwastaken2`. Java package: `com.thirstwastaken2`.
- Published on [Modrinth](https://modrinth.com/mod/thirst-was-taken-2) and
  [CurseForge](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken-2).
- Every area below has its own `AGENTS.md`; read it before changing that area (see
  [Where to look](#where-to-look)).

## Build and run

One source tree, one jar per node. Nodes are the Gradle subprojects in `settings.gradle.kts`:
`26.2.x`, `26.1.x`, `1.21.11`, `1.21.1` on Fabric, and the same four with `-neoforge`
(`build.neoforge.gradle.kts`). The Fabric `1.21.1` jar also covers 1.21; the NeoForge one does not.

| Command | What it does |
|---|---|
| `./gradlew buildAndCollect` | Build every node, jars in `build/libs/` |
| `./gradlew ":<node>:build"` | Build one node |
| `./gradlew ":<node>:runServer"` | Fastest smoke test: applies every mixin, loads registries, idles. Clean run prints `ThirstWasTaken2 initialized for Minecraft <version>` |
| `./gradlew ":<node>:runClient"` | Dev client |
| `./gradlew ":<node>:runGametest"` | Automated in-game tests, headless, seconds. **The check that proves behaviour.** CI runs it on every node. See [src/gametest/java/AGENTS.md](src/gametest/java/AGENTS.md) |
| `./gradlew ":<node>:runDatagen"` | Regenerate recipes, advancements, tags, damage type and models into `src/main/generated/<mc version>/` (Fabric nodes only) |
| `./gradlew ":<node>:checkDatagen"` | Fails if generated output differs from what is committed. CI runs it |
| `./gradlew ":<node>:runBenchmark"` | Server cost in time and memory. See [benchmark/AGENTS.md](src/dev/java/com/thirstwastaken2/dev/benchmark/AGENTS.md) |
| `./gradlew ":<node>:runServer" -Pagent=<file>.jsonl` | Script a running game and read numbers back. See [agent/AGENTS.md](src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md) |
| `python tools/release/publish.py --dry-run` | Release to Modrinth, then `publish_curseforge.py --no-build`. Checklist and flags in the scripts' docstrings |

- **Never hand-edit `src/main/generated/`.** Change the generator in `src/datagen`. NeoForge nodes
  reuse the Fabric output and translate its keys; `checkNeoForgeResources` fails if one survives.
- Each node runs in its own `run/<node>/` (worlds are not portable across versions). A new one needs
  its own `eula.txt`.
- Unqualified `./gradlew build` acts on the **active** node (`26.2.x`). `./gradlew "Set active project
  to <node>"` rewrites the versioned comments in `src/` for the IDE. **Run
  `./gradlew "Reset active project"` before committing.**
- The first run needs network for `maven.modrinth`; after that `--offline` works.

## Stack and constraints

- **Java**: 26.1+ runs on Java 25, 1.21.x on Java 21. Do not use a language feature newer than Java 21.
- **Multi-version via [Stonecutter](https://stonecutter.kikugie.dev)**. Per-node values (dependency
  versions, compat ranges) live only in `stonecutter.properties.toml`; there is no version catalog.
  `stonecutter.gradle.kts` is the controller, `build.gradle.kts` is the Fabric script (Loom),
  `build.neoforge.gradle.kts` the NeoForge one (ModDevGradle), `gradle/shared.gradle.kts` what both share.
- **Split source sets.** Anything touching `net.minecraft.client` goes in `src/client/java`, never
  `src/main/java`. NeoForge compiles both together, so only the Fabric nodes catch a mistake.
- **Common code never names a mod loader.** `src/main/java` and `src/client/java` call
  `platform/Loader` and `client/platform/ClientLoader`, one copy per loader in `src/*/<loader>`.
  `checkLoaderSeam` enforces it. See [platform/AGENTS.md](src/main/java/com/thirstwastaken2/platform/AGENTS.md).
- **Optional integrations are soft.** No hard dependency, ever. Gate on `Loader.isModLoaded` (plus a
  marker-class probe when extending a foreign class) and keep integration classes off the load path.
- **Mixins**: in `com.thirstwastaken2.mixin`, package-private, `abstract`, every injected member
  prefixed `thirst$`, listed in `thirstwastaken2.mixins.json` or they silently do nothing. Client,
  Fabric-client, dev, Create, Create Fly and Sophisticated mixins have their own configs next to their
  sources. A new config goes in both loader manifests.
- **Player state** is the immutable record `ThirstData`. Derive a new one and write through
  `ThirstManager.set` only when it changed; every write is a sync packet.
- **Config** is the Gson POJO `ThirstConfig`. A new field: add it, clamp it in `sanitize()`, and if
  user-facing add a widget and reset line in `client/config/ConfigCategory` plus lang keys (`en_us`
  and `vi_vn` mandatory).
- **Per-item lookups are cached** by `Item` identity (`ThirstApi.CACHE`, `WaterPurity.INFO`). No
  string building or regex on a per-call path; tooltips call these every frame.
- **`ThirstWasTaken2.DEV`** is true under Loom run tasks, false in the published jar. Dev-only tooling
  lives in the `dev` source set, which `main` and `client` never reference.
- Intentional divergence from upstream gets a comment at the divergence.

## Supporting several Minecraft versions

Version differences live in two places only:

- **`platform/`**: `Vanilla` (common) and `ClientVanilla` (client) wrap every vanilla call whose shape
  changed, with one signature on every version.
- **`mixin/`**: an `@Inject` signature tracks its target, so a mixin may fork. Its body stays one line.

Everything else must compile unchanged on every version; `checkVersionSeam` fails CI otherwise. A
branch is a Stonecutter comment, the disabled side commented out:

```java
//? if >=26.2 {
return minecraft.gui.hud.isHidden();
//?} else {
/*return minecraft.options.hideGui;
*///?}
```

A pure rename needs no branch: add it to `replacements` in `stonecutter.gradle.kts`.

- **Replacements do not chain.** Each applies to the original text. When two differences meet, pick
  the result in Kotlin first (see `critereon` in `stonecutter.gradle.kts`).
- **No block comments inside a `//?` block.** The disabled branch is itself one `/* */`. Put Javadoc
  outside or use line comments.

Every difference between versions is listed in [docs/dev/VERSION-DIFFERENCES.md](docs/dev/VERSION-DIFFERENCES.md).

### Version policy

- At most four Minecraft versions; adding one retires one. A NeoForge node on an existing version
  does not count. When 26.3 arrives, drop 26.1.x.
- 1.21.1 is kept for modpacks. If a feature needs a core-code fork for it, retire 1.21.1 instead.

### Adding a Minecraft version

1. Add `<version>` and `<version>-neoforge` in `settings.gradle.kts`, and three tables in
   `stonecutter.properties.toml`: `["<version>"]` (shared), `[fabric."<version>"]`,
   `[neoforge."<version>"]`. A key lives in exactly one table a node reads. AppleSkin is pinned by
   Modrinth version id, since its Fabric and NeoForge uploads share a version number.
2. CI needs nothing: `.github/workflows/build.yml` makes one job per loader table.
3. `./gradlew ":<version>:build"` and fix errors by extending `platform/`, not by branching at the call site.
4. Smoke-test with `runServer`.

### Dependency updates

Dependabot covers Gradle plugins, the wrapper, Actions and `docs/`, but not
`stonecutter.properties.toml`. `.github/scripts/update_mc_deps.py` does that daily through
`.github/workflows/update-mc-deps.yml`, into the `automation/minecraft-deps` PR. Test with `--dry-run`.
A new dependency must be added to `MODRINTH_DEPS` in the script.

## Architecture

`ThirstWasTaken2.initialize` is the one loader-independent entry point, called by
`ThirstWasTaken2Fabric` and `ThirstWasTaken2NeoForge`. It loads config, registers player data, blocks,
components, items and loot, then hooks server tick, block/item use, commands and tag reload through
`Loader`. Init order, the drain chain and the invariants are in
[src/main/java/com/thirstwastaken2/AGENTS.md](src/main/java/com/thirstwastaken2/AGENTS.md).

Water is `WaterQuality`, sealed: `Fresh(grade 0-3)` or `Salt`. It is sampled from the world only when
water is collected, drunk or looked at with Jade, never on a tick or tooltip path. See
[purity/AGENTS.md](src/main/java/com/thirstwastaken2/purity/AGENTS.md).

### Source roots

| Path | Compiled |
|---|---|
| `src/main/java`, `src/client/java` | Everywhere. Loader-independent |
| `src/main/fabric`, `src/client/fabric` | Fabric nodes |
| `src/main/neoforge`, `src/client/neoforge` | NeoForge nodes (client compiled into main) |
| `src/main/neoforge-fluidhandler` / `neoforge-transfer` | NeoForge 1.21.1 / 1.21.11+, the fluid container API |
| `src/main/createfly`, `src/client/createfly` | Where `deps.create_fly` is set (Fabric 26.1.x, 26.2.x) |
| `src/main/create` | Where `deps.create` is set (`1.21.1-neoforge`) |
| `src/main/sophisticated`, `src/client/sophisticated` | Where `deps.sophisticated_core` is set (every NeoForge node) |
| `src/main/sophisticated-fluidhandler` / `sophisticated-transfer` | The same nodes, 1.21.1 / 1.21.11+: Sophisticated's tank and pump code |
| `src/main/resources` | Hand-written assets and lang, all nodes |
| `src/main/generated/<mc version>` | Datagen output, never hand-edited |
| `src/datagen`, `src/gametest`, `src/dev` | Separate mods, never packaged |
| `tools/agent`, `tools/benchmark`, `tools/release` | Scripts for the agent client, benchmark sets, publishing |

### Optional integrations

| Mod | Where |
|---|---|
| AppleSkin, Jade, Mod Menu, Farmer's Delight, loot | [compat/AGENTS.md](src/main/java/com/thirstwastaken2/compat/AGENTS.md) |
| Drinks from other mods | `c:drinks` tag and registry ids in `ThirstConfig`, no class references |
| Create Fly (Fabric) | [src/main/createfly/AGENTS.md](src/main/createfly/AGENTS.md) |
| Create (NeoForge) | [src/main/create/AGENTS.md](src/main/create/AGENTS.md) |
| Sophisticated Backpacks and Storage | [src/main/sophisticated/AGENTS.md](src/main/sophisticated/AGENTS.md) |

## Where to look

| Task / Area | Location |
|---|---|
| Common code: init order, state invariants, caching, tooltip lines | [src/main/java/com/thirstwastaken2/AGENTS.md](src/main/java/com/thirstwastaken2/AGENTS.md) |
| Vanilla hooks and fragile injections | [mixin/AGENTS.md](src/main/java/com/thirstwastaken2/mixin/AGENTS.md) |
| Water purity, sampling, cauldrons, fluid containers | [purity/AGENTS.md](src/main/java/com/thirstwastaken2/purity/AGENTS.md) |
| Version and loader differences | [platform/AGENTS.md](src/main/java/com/thirstwastaken2/platform/AGENTS.md) |
| HUD and config screen | [src/client/java/com/thirstwastaken2/client/AGENTS.md](src/client/java/com/thirstwastaken2/client/AGENTS.md) |
| Manifests, textures, fonts, lang | [src/main/resources/AGENTS.md](src/main/resources/AGENTS.md) |
| Datagen providers | [src/datagen/java/AGENTS.md](src/datagen/java/AGENTS.md) |
| Automated in-game tests | [src/gametest/java/AGENTS.md](src/gametest/java/AGENTS.md) |
| Dev-only tooling (agent client, benchmark) | [src/dev/java/AGENTS.md](src/dev/java/AGENTS.md) |
| Benchmark baseline per node | [docs/dev/BENCHMARK-BASELINE.md](docs/dev/BENCHMARK-BASELINE.md) |
| Manual checks before a release | [docs/dev/MANUAL-TESTING.md](docs/dev/MANUAL-TESTING.md) |
| Purification balance | [docs/dev/WATER-PURIFICATION-BALANCE.md](docs/dev/WATER-PURIFICATION-BALANCE.md) |
| Sophisticated upgrades still to do | [docs/dev/SOPHISTICATED-INTEGRATION.md](docs/dev/SOPHISTICATED-INTEGRATION.md) |
| Releasing | [tools/release/publish.py](tools/release/publish.py) and [publish_curseforge.py](tools/release/publish_curseforge.py) docstrings |
| Documentation site, CHANGELOG, Modrinth and CurseForge pages | [docs/AGENTS.md](docs/AGENTS.md) and the `write-docs` skill |
