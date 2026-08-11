# CLAUDE.md

Guidance for working in this repository.

## What this is

A Fabric fork of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) (originally Forge,
Minecraft 1.19.2) on **Minecraft 26.2 / Fabric Loader 0.19.3 / Java 25**. It started as a port and
has since diverged, so upstream is a reference, not a spec.

- Mod id and resource namespace: `thirstwastaken2`
- Java package: `com.thirstwastaken2`
- Upstream reference source is expected at `../Thirst-Mod` when comparing behaviour.
- The Create Fly integration is **broken and documented as unsupported**. The code is still present
  and still gated behind `CreateFlyIntegration.isAvailable()`; the docs say it is off. Re-enabling it
  means fixing the integration and reverting those doc notes together.

See [CODEMAP.md](CODEMAP.md) for the file-by-file architecture and
[FORK-STATUS.md](FORK-STATUS.md) for what the fork carries, adds and still lacks.

## Build and run

```bash
./gradlew build
```

```bash
./gradlew runServer
```

```bash
./gradlew runClient
```

`runServer` is the fastest smoke test: it applies every mixin, loads the datapack registries and the
conditional Create recipe, then idles. A clean run prints
`ThirstWasTaken2 initialized for Minecraft 26.2` and no exceptions.

Gradle needs network access on the first run for `maven.modrinth` artifacts (Create Fly, Mod Menu).
Once cached, `--offline` works — except that `clientCompileOnly` on Mod Menu must already be cached.

## Conventions

- **Source sets are split** (`loom.splitEnvironmentSourceSets()`). Anything that touches
  `net.minecraft.client` belongs in `src/client/java`, never in `src/main/java`.
- **Mixins live in `com.thirstwastaken2.mixin`**, are package-private, `abstract`, and prefix every
  injected member with `thirst$`. New mixins must be listed in `thirstwastaken2.mixins.json`.
- **Config is a plain POJO** serialized by Gson (`ThirstConfig`). Adding a field means: add it to the
  POJO, clamp it in `sanitize()`, and — if it is user-facing — add a widget in `ThirstConfigScreen`
  plus `en_us`/`vi_vn` keys.
- **Per-item lookups are cached** keyed by `Item` identity (`ThirstApi.CACHE`, `WaterPurity.INFO`).
  Never do registry-name string building or regex compilation on a per-call path; the tooltip
  renderer calls into both once per frame.
- **Optional mod integrations are soft**. Never add a hard dependency: gate on
  `FabricLoader.isModLoaded` plus, for Create Fly, a marker-class probe
  (`CreateFlyIntegration.isAvailable()`), and keep integration classes out of the load path
  otherwise.
- Player thirst state is an **immutable record** (`ThirstData`) stored as a Fabric attachment. Mutate
  by deriving a new record and calling `ThirstManager.set`; only write when the value actually
  changed, because every write costs a sync packet.

## Porting rules of thumb

- Vanilla APIs the original relied on that no longer exist in 26.2:
  - `DimensionType#ultraWarm` → `EnvironmentAttributes.WATER_EVAPORATES`
  - `Biome#getDownfall` → no public equivalent; the port approximates with
    `Biome#hasPrecipitation()` (see `ThirstManager.climateModifier`)
  - Item NBT (`"Purity"` tag) → the `thirstwastaken2:water_purity` data component
  - Forge global loot modifiers → `LootTableEvents.MODIFY`
  - Forge GUI overlays → Fabric `HudElementRegistry`
- When behaviour differs from the original mod on purpose, say so in a comment at the divergence.

## Things that are deliberately not 1:1 with upstream

- Structure-chest water uses one Fabric loot pool per table instead of the original's
  Farmer's-Respite / Brewin'-and-Chewin' loot variants.
- The quenched overlay is drawn by this mod directly, always on, with no setting. The AppleSkin
  exhaustion underlay was removed: it belongs to an AppleSkin integration that does not exist yet, so
  shipping a half version of it was worse than not shipping it. `appleskin_icons.png` still supplies
  the quenched frames, and its v=18 dither row is now unused.
- Water purity at altitude follows the documented rule (mountains *or* caves are cleaner); upstream
  had a contradictory extra clause that made the mountain case dead code.
