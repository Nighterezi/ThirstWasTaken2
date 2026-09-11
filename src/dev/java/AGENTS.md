# src/dev — development-only tooling

Tools for working on the mod that must never ship. Like `src/gametest`, this is its own source set and
its own small mod, `thirstwastaken2-dev`, declared in `src/dev/resources/fabric.mod.json`, so none of it
can reach a published jar. `runServer` and `runBenchmark` load it; `runClient` and `runGametest` do not.

`ThirstDev` registers nothing unless `ThirstWasTaken2.DEV` is true. That flag is Fabric Loader's
development-environment check, true under every Loom run task and false in the jar players install.
`-Dthirstwastaken2.dev=true|false` overrides it either way.

## /thirst benchmark

Measures what the mod costs a server, in time and in memory, with nobody joining. Players are simulated
entirely on the server, and the results land in a JSON report plus a console summary.

### Running it unattended (the way an agent should)

```bash
./gradlew ":26.2.x:runBenchmark"
```

Run it in the background and wait for the task to exit. It starts the dedicated server in
`run/<version>/`, runs `/thirst benchmark` from the console as soon as the server is up, writes the report
and stops the server. Progress is printed as `[ThirstBenchmark] ...` lines, and the last one is

    [ThirstBenchmark] DONE status=ok in 41.3 s report=<absolute path>

The Gradle task exits 0 even when the benchmark itself failed, so always read `status` in the report:
`ok`, `cancelled` or `failed`, with `message` explaining the last two. The newest report is also copied to
`run/<version>/benchmark/latest.json`.

`runBenchmark` shares `runServer`'s run directory, world and accepted EULA. The two cannot run at the same
time, and neither can two versions' benchmarks when something else holds port 25565.

`-Pbenchmark=<arguments>` picks what runs, exactly as typed after `/thirst benchmark`:

| Arguments | Player counts | Warm-up + measured ticks per count | Ops per interaction |
|---|---|---|---|
| `quick` | 1, 10, 50 | 100 + 200 | 2 000 |
| `standard` (default) | 1, 10, 50, 100, 200 | 200 + 600 | 10 000 |
| `stress` | 1, 100, 250, 500, 1000 | 200 + 600 | 20 000 |
| `"players <count> [ticks]"` | `count` (1 to 2000) | `ticks`/3, at least 100, + `ticks` (default 600) | 5 000 |

A typo in the arguments is logged as `DONE status=failed` and the server stops anyway.

Ticks in a profile are simulated ticks, not wall-clock ones. Each server tick the benchmark runs as many
simulated ticks as fit in its 35 ms budget, so 20 000 ticks for 200 players take a few seconds rather than
17 minutes. Intervals inside the simulation, such as a drink every 600 ticks, count simulated ticks too.

On a 22-thread desktop the whole `runBenchmark` task takes about 25-35 seconds for `quick` or
`standard`, most of it Gradle and server startup; the benchmark itself runs for 3-6 seconds. `stress`
and large `players` counts take longer mainly because creating the simulated players is slow.

### Running it by hand

Start `./gradlew ":26.2.x:runServer"` and type into its console:

- `thirst benchmark [quick|standard|stress|players <count> [ticks]]`
- `thirst benchmark status`
- `thirst benchmark cancel`, which still restores the world and writes a partial report

In game it needs permission level 4. The server stays responsive while it runs: the benchmark does at most
35 ms of work per server tick and carries on over the following ticks.

To drive that console from a script rather than a keyboard, write lines to the server's standard input:
Loom forwards it, and `runServer` accepts commands on it. Wait for the `Done (` line before the first
command. When the writer is .NET, for example `System.Diagnostics.Process` from PowerShell, send an empty
line first: .NET puts a UTF-8 byte order mark in front of the first write, which turns that line into
`Unknown or incomplete command`. `runBenchmark` needs none of this and is the better choice unless the
point is to test the command itself.

## What it measures

### Tick scenarios

For each player count, every simulated tick does, for every player, what a real session does:

| Section | Work | Mod code exercised |
|---|---|---|
| `exhaustion` | Vanilla's exhaustion charges: 6 in 10 players sprint, 2 swim, 2 idle; sprint-jumps, attacks, mining, and the Hunger effect on 1 in 10 | `PlayerMixin` → `ThirstManager.mirrorExhaustion` |
| `sprint_gate` | One `Player#canSprint` per player | `PlayerMixin` sprint gate |
| `interactions` | Once every 30 s per player, staggered: sample the fixture water into a bowl, and half a cycle later drink a bowl | `WaterPurity.sampleAt`, `ItemStackMixin` → `ThirstManager.drinkItem` |
| `food_tick` | Vanilla `FoodData#tick`, with health kept below full so regeneration keeps firing | `FoodDataMixin`, `HealthRegen` |
| `thirst_tick` | `ThirstManager.tickPlayer` | buffered exhaustion, modifier cache, Hunger refund, consumption |
| `sync_encode` | `ThirstData.STREAM_CODEC` for every player whose attachment changed | what a real client's sync packet carries |

Half the players wear diamond armour with Protection IV, Unbreaking III and Mending, which is what makes
the exhaustion modifier expensive to compute. 1 in 20 has Nausea and 1 in 20 Fire Resistance. Upkeep that
only keeps the simulation going runs outside every timed section: topping thirst up before it runs dry
and restoring health and food.

Per count, the report gives milliseconds per tick (mean, p50, p95, p99, max), the share of a 50 ms tick,
microseconds per player, bytes allocated per tick and per player, the time share of each section, sync
packets per player per second, and garbage collections during the measurement.

### Interactions

One player standing over the water fixture repeats each operation. Operations that change the world or
the player are reset between runs outside the timed window. Operations that change nothing are timed in
batches of 64, because one alone is quicker than the clock resolves; their percentiles are over batch
means. Each operation checks once that it actually did something and fails the run otherwise, so spawn
protection refusing a fill cannot pass as an impressively fast result.

`sample_water`, `fill_bottle`, `fill_bucket`, `fill_bowl`, `fill_waterskin`, `drink_water_bottle`,
`drink_waterskin`, `drink_by_hand`, `cauldron_pour`, `full_bar_guard`, `tooltip_water_bottle`,
`tooltip_waterskin`, `tooltip_food`, `hydration_lookup`, `water_quality_read`, `waterskin_mix`,
`exhaustion_mirror`, `thirst_tick_idle`. The report's `description` field says what each one does.

### Memory

- `thirstDataBytes`, `exhaustionTrackerBytes`: the shallow size of each object, from allocating ten
  thousand of them.
- `firstTouchBytesPerPlayer`: allocated the first time a player drains, attachment creation included.
- `steadyTickBytesPerPlayer`: allocated by one ordinary exhaustion charge plus thirst tick afterwards.
- `hydrationCacheEntries`, `purityInfoEntries`: the sizes of `ThirstApi.CACHE` and `WaterPurity.INFO`.
- `heapUsedAfterGcBeforeMiB`, `heapUsedAfterGcAfterMiB`, `heapRetainedKiB`: whole-heap use after a full
  collection, taken once the benchmark area has loaded and again after the run has let go of its players,
  with the area still loaded so chunk memory cancels out. The first run on a freshly started server also
  counts what stays for the server's lifetime: the hydration cache and vanilla's per-UUID stats and
  advancements for the simulated players. A second run in the same session should come out near zero;
  a value that keeps growing from run to run is a leak. Other threads allocate too, so a few MiB either
  way is noise.

Allocation figures count only the server thread and are exact regardless of garbage collection. If the
JVM cannot count them, `environment.allocationTracking` is false and every byte figure is zero.

## Comparing two versions of the code

Use the same Minecraft version, the same profile and the same machine, with nothing else heavy running.
Run each side at least twice. Timings on a desktop move by around 10% between runs; allocation figures do
not move at all, so a changed `bytes` value is always real. The fields that matter:

- `tickScenarios[].msPerTick.mean` and `.p99`, `microsPerPlayerPerTick`
- `tickScenarios[].sections.*.sharePercent` and `microsPerTick`, to see which part moved
- `tickScenarios[].allocatedBytesPerPlayerPerTick`, `syncPacketsPerPlayerPerSecond`
- `interactions[].microsPerOp.mean`, `interactions[].bytesPerOp`
- `memory.*`

## What it does not measure

- Client work: HUD drawing, tooltip rendering (building the tooltip lines is measured), the config screen.
- The wire. Changed attachments are counted and their `ThirstData` encoded, but Fabric's payload wrapping,
  compression and Netty are not.
- Vanilla's own per-player cost, such as movement, chunk sending and entity tracking. Percentages are of a
  50 ms budget, not of a real server's tick.
- Other dimensions, so the Nether branch of the exhaustion modifier.
- Interleaving. Sections run one kind of work across all players, which is slightly friendlier to the CPU
  cache than the game's per-player order.

## What it does to the server and the dev world

- A dedicated server with nobody online stops ticking after `pause-when-empty-seconds` (60 by default), and
  a paused server fires no tick events. While a run is active `BenchmarkRunner` resets vanilla's
  `emptyTicks` counter every tick, and once more when the command starts one, which also wakes a server
  that has already paused. Without that, a run typed into the console more than a minute after startup
  never advances, and a long run stalls a minute in. The pause behaves normally again once the run ends.

- Force-loads 5x5 chunks around chunk (0, 0), or around the first of (32, 0), (0, 32), (-32, -32) that is
  outside spawn protection, and builds a small water fixture a few blocks below the build limit. Both are
  undone when the run ends: completed, cancelled, failed, or cut short by the server stopping. Only a
  crash mid-run can leave them behind.
- Simulated players are `BenchmarkPlayer`s, Fabric `FakePlayer`s built fresh per run: not in the player
  list, not in the level, invulnerable, and every packet to them is dropped. Nothing is saved for them.
- Vanilla creates a stats object and full advancement progress for every player it constructs, keyed by
  UUID, and only forgets them when a player disconnects. That progress costs hundreds of kilobytes a
  player, so `BenchmarkWorld.releasePlayers` removes the simulated players' entries from `PlayerList` by
  reflection when the run ends, unregistering the advancement listeners the way `PlayerList#remove`
  does. Any player the benchmark creates must go through `BenchmarkWorld.player` or `extraPlayer`, or it
  is not forgotten. The UUIDs are derived from the player index, so even a missed cleanup is reused by the
  next run rather than piling up.

## Rules

- Nothing in `main` or `client` may reference this source set.
- The benchmark compiles unchanged on every supported version. If a vanilla call here ever needs a
  Stonecutter branch, give it a seam in this source set rather than branching at the call site, the same
  way `platform/` works for the mod.
- A scenario that stops exercising the mod must fail, never report fast numbers. Interaction operations
  check their outcome for this reason.
- Every block the benchmark places goes through `BenchmarkWorld.place`, so `close` can restore it.
- Stages do bounded work per call and are resumed on the next tick. Never loop until done inside a stage:
  the server watchdog kills a tick that runs for 60 seconds.
- CI only compiles this source set (`devClasses`); it does not run the benchmark.
