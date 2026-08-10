# CLAUDE.md

Guidance for working in this repository.

## What this is

A Fabric port of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) (originally Forge,
Minecraft 1.19.2) to **Minecraft 26.2 / Fabric Loader 0.19.3 / Java 25**.

- Mod id and resource namespace: `thirstwastaken`
- Java package: `com.thirstwastaken`
- Upstream reference source is expected at `../Thirst-Mod` when comparing behaviour.

See [CODEMAP.md](CODEMAP.md) for the file-by-file architecture and
[FABRIC-PORT.md](FABRIC-PORT.md) for the port status matrix.

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
`Thirst Was Taken Fabric initialized for Minecraft 26.2` and no exceptions.

Gradle needs network access on the first run for `maven.modrinth` artifacts (Create Fly, Mod Menu).
Once cached, `--offline` works — except that `clientCompileOnly` on Mod Menu must already be cached.

## Conventions

- **Source sets are split** (`loom.splitEnvironmentSourceSets()`). Anything that touches
  `net.minecraft.client` belongs in `src/client/java`, never in `src/main/java`.
- **Mixins live in `com.thirstwastaken.mixin`**, are package-private, `abstract`, and prefix every
  injected member with `thirst$`. New mixins must be listed in `thirstwastaken.mixins.json`.
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
  - Item NBT (`"Purity"` tag) → the `thirstwastaken:water_purity` data component
  - Forge global loot modifiers → `LootTableEvents.MODIFY`
  - Forge GUI overlays → Fabric `HudElementRegistry`
- When behaviour differs from the original mod on purpose, say so in a comment at the divergence.

## Things that are deliberately not 1:1 with upstream

- Structure-chest water uses one Fabric loot pool per table instead of the original's
  Farmer's-Respite / Brewin'-and-Chewin' loot variants.
- The AppleSkin exhaustion underlay and quenched overlay are drawn by this mod directly and gated on
  `showExhaustionUnderlay` / `showQuenchedOverlay`, since AppleSkin's config is not available.
- Water purity at altitude follows the documented rule (mountains *or* caves are cleaner); upstream
  had a contradictory extra clause that made the mountain case dead code.
