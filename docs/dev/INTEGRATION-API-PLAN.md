# Integration API plan

How to let other mods integrate with ThirstWasTaken2 on their own, instead of every integration being
written from this side. Today the only hooks a mod author has are the `c:drinks` tag, which gives every
tagged item the same `drinkTagValue`, and `ThirstApi`, which can only read an item's value. Anything
more means calling `ThirstManager` in `data/`, internal code with no promise that it stays put.

Nothing here changes what a player gets without a mod or data pack asking for it. With no data pack
and no listener registered, every drink restores exactly what it restores today.

## Status

| # | Item | Kind | Status |
|---|---|---|---|
| 0 | Decide how data packs carry per-item values | decision | open |
| 1 | Per-item thirst values from data packs | data | todo |
| 2 | Player-level methods on `ThirstApi` | code | todo |
| 3 | Loader-neutral callbacks: drink and exhaustion | code | todo |
| 4 | Water purity in the API | code | todo |
| 5 | Stability rules for `com.thirstwastaken2.api` and a check that enforces them | check | todo |
| 6 | Getting the jar: Modrinth Maven, sources, maybe a Maven of our own | build | decision open |
| 7 | Developer page: `docs/dev/API.md` | docs | todo |

Order: 0 before 1. 1 comes first because it is the only item that needs no code in the other mod, so
it helps the most mods. 2 before 3, since the callbacks hand out the same types. 4 can go any time
after 2. 5 goes in with 2, not after, so the first public release of the new API is already
checked. 7 goes with each step, not at the end.

## 0. Decision: how data packs carry values

Resolution order today, in `ThirstApi.resolve`: `itemBlacklist`, then `drinks`/`foods` in the config,
then `c:drinks`, then keywords. Data pack values slot in after the config and before `c:drinks`, so a
server owner's config always wins over what a mod ships, and a mod's own value wins over the flat tag
value.

The catch: **the tooltip resolves on the client**, and the config is not synced (each side reads its
own file). Whatever carries a mod's values has to reach the client, or the tooltip shows one value and
drinking restores another.

| | A. Value tags | B. JSON reload listener | C. NeoForge Data Maps |
|---|---|---|---|
| What a mod ships | Item tags such as `thirstwastaken2:thirst/3_4` or a fixed set like `thirst/small`, `medium`, `large` | `data/<ns>/thirstwastaken2/drinks/*.json` with `{ "item": ..., "thirst": 6, "quenched": 8 }` | `data/<ns>/data_maps/item/thirstwastaken2/drink.json` |
| Reaches the client | Yes, vanilla syncs tags | No, needs a sync packet on join and on `/reload` | Yes, NeoForge syncs it |
| Both loaders | Yes | Yes | NeoForge only; Fabric would need B anyway |
| Per-item values | Only from a fixed set of buckets | Exact | Exact |
| Cache drop | `Loader.onTagsLoaded` already clears `ThirstApi` | New hook | Needs a NeoForge-only hook |
| Cost | Smallest | A codec, a listener, a payload on both loaders | Two code paths |

Recommendation: **B**. Exact values are what mod authors will ask for, and one path on both loaders
beats C's split. A is a fallback if the sync packet turns out to cost more than it is worth: it could
ship first and B be added later, with B winning where both name an item.

Points to settle with the choice:

- Whether a data pack can also blacklist an item (`"thirst": 0` or a `remove` list), or only the config can.
- Whether `foods` get their own folder or a `"kind"` field; today `drinks` and `foods` differ only in
  which config map they sit in.
- Unknown item ids are skipped with one warning per file, never an error: the mod they name may be absent.

## 1. Per-item values from data packs

Assuming B:

- A new class, e.g. `com.thirstwastaken2.data.DrinkValues`, holding an immutable
  `Map<Item, int[]>` swapped wholesale on reload, the same way `ThirstConfig` is swapped.
- Server: a reload listener registered through `Loader`, one per loader in `src/main/fabric` and
  `src/main/neoforge`. Parsing lives in common code.
- Sync: a payload sent on join and after `/reload`, received into the same `DrinkValues` on the client.
  Singleplayer must not parse twice.
- `ThirstApi.resolve` reads `DrinkValues` after the config maps. The cache drop happens on the same
  event, like tags today.
- Our own defaults (milk, honey, Farmer's Delight) stay in the config for now. Moving them into a
  built-in data pack is a separate change, because it changes what an existing config file means.

Tests: gametests for load, override order (config > data pack > `c:drinks` > keyword), a blacklisted
item staying blacklisted, and a value surviving `/reload`. A two-player gametest for sync if the
gametest setup can fake a client; otherwise a manual step in `MANUAL-TESTING.md`.

## 2. Player-level methods on `ThirstApi`

Everything another mod should be able to do to a player, without touching `data/`:

```java
int thirst(Player player);              // 0..MAX
int quenched(Player player);
boolean isEnabled(Player player);       // false in creative/spectator or when the mod is off for them
void drink(Player player, int thirst, int quenched);   // server side; no-op on the client
void addExhaustion(Player player, float amount);
int maxThirst();                        // ThirstData.MAX, so nobody hard-codes 20
```

- Each is a one-line forward to `ThirstManager`. `ThirstData` itself stays internal: returning the
  record would freeze its fields.
- Client-side calls to the write methods do nothing, documented, rather than throw; that is what
  `ThirstManager.drink` does already.
- `ThirstApi.clearCache()` is public today only because `ThirstWasTaken2` calls it. Move that call to an
  internal entry point and deprecate the public one, or keep it and document it as harmless.

## 3. Loader-neutral callbacks

Plain listener lists in common code, not Fabric `Event` or NeoForge `Event` classes: one API for both
loaders, and no loader dependency in `api/`.

```java
ThirstEvents.DRINK.register((player, stack, values) -> {
    // values is mutable: change thirst/quenched, or cancel
});
ThirstEvents.EXHAUSTION.register((player, amount) -> amount * 0.5F);
```

- `DRINK` fires in `ThirstManager.drinkItem` after the value is resolved and before it is applied, so it
  covers the item mixin and every integration (Sophisticated, Supplementaries) at once. Drinking from a
  block by hand goes through the same event with an empty or synthetic stack; decide which.
- `EXHAUSTION` fires where exhaustion is applied, **once per tick**, in `ThirstManager.tick`, not in
  `mirrorExhaustion`, which runs several times a tick.
- A listener that throws is logged and skipped; it must not take the tick down.
- An empty listener list must cost nothing measurable. Check with the benchmark in `src/dev`.

Not in this item: a "thirst changed" event. Add it only when someone asks; it fires on every sync step.

## 4. Water purity in the API

- `WaterQuality quality(ItemStack)`, or a flattened `int purity(ItemStack)` plus `boolean isSalt(ItemStack)`,
  so the sealed `WaterQuality` type need not become public. Decide which.
- `ItemStack waterBottle(int purity)` for mods that hand out purified water.
- `boolean isWaterContainer(ItemStack)`.

Forwarding to `WaterPurity`; no new behaviour.

## 5. Stability rules and a check

- Everything in `com.thirstwastaken2.api` is public API. A signature there changes only with a
  deprecation first, kept for at least one minor release.
- `api/` must not expose internal types in public signatures (`ThirstData`, `ThirstConfig`,
  `WaterQuality` unless item 4 makes it public). A build-logic check, next to `checkVersionSeam` and
  `checkOptionalSeam`, scans the package's public signatures for `com.thirstwastaken2.` types outside `api`.
- `api/` holds no `//?` branch. It must look the same on every node, or a mod compiled against one
  version breaks on another. `checkVersionSeam` already rejects branches outside `platform/`; confirm it
  covers `api/`.
- A `ThirstApi.API_VERSION` int, bumped on every addition, so a mod can check for a method before calling it.

## 6. Getting the jar

Today `publishing { repositories { } }` is empty in both loader scripts, so the only way in is Modrinth
Maven:

```kotlin
repositories { maven("https://api.modrinth.com/maven") }
dependencies { modCompileOnly("maven.modrinth:<project-slug>:<version>") }
```

Decide:

- Whether Modrinth Maven is enough. It serves the mod jar only: no sources, no javadoc, and the version
  string is whatever the upload was called, one per loader and Minecraft version.
- Or a Maven of our own (GitHub Packages, or a static repo on GitHub Pages) fed by `tools/release`, with
  the sources jars the build already makes.
- Or a separate small `api` jar. Only worth it if the API grows; one class plus callbacks does not need it.

## 7. Developer page

`docs/dev/API.md`, linked from the README and from the root `AGENTS.md` table:

- Data pack format from item 1, with a full example file.
- `c:drinks` and what `drinkTagValue` means for a tagged item.
- The Gradle snippet from item 6.
- A short Java example for items 2 to 4, and a note that everything must be guarded with
  "is ThirstWasTaken2 loaded", since it is an optional dependency for them.
- The stability rules from item 5.

The player-facing site in `docs/docs` gets one line in the FAQ ("my mod's drink does nothing") pointing
server owners at the config and mod authors at the data pack format; nothing more.
