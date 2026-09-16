# src/dev — development-only tooling

Tools for working on the mod that must never ship. Like `src/gametest`, this is its own source set and
its own small mod, so none of it can reach a published jar: `thirstwastaken2-dev` on Fabric, declared
in `src/dev/resources/fabric.mod.json`, and `thirstwastaken2_dev` on NeoForge, declared in
`src/dev/neoforge/resources/META-INF/neoforge.mods.toml`, because a NeoForge mod id cannot contain a
hyphen. `runGametest` and `runDatagen` do not load it; every other run task does — `runServer`,
`runBenchmark` and `runClient` on Fabric, and `runServer`, `runClient`, `runManualA` and `runManualB`
on NeoForge.

Two tools live here.

| Tool | What it is for | Where |
|---|---|---|
| [`/thirst benchmark`](#thirst-benchmark) | what the mod costs a server, with nobody joining | Fabric nodes only |
| [the agent client](#the-agent-client) | driving a real client and reading numbers out of it | every node |

The split of the directory follows that. `benchmark/` is the first; `agent/core` and `agent/thirst`
are the second; `ThirstDev` and `ThirstDevClient` are the two halves of the entrypoint, kept apart so
that a dedicated server never loads a class naming `Minecraft`; `fabric/` and `neoforge/` hold each
loader's entrypoints and the small `DevLoader`/`DevClientLoader` seam for the four calls the mod's own
`Loader` does not cover.

`ThirstDev` registers nothing unless `ThirstWasTaken2.DEV` is true. That flag is the loader's own
development-environment check, true under every run task and false in the jar players install.
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
`tooltip_waterskin`, `tooltip_food`, `thirst_lookup`, `water_quality_read`, `waterskin_mix`,
`exhaustion_mirror`, `thirst_tick_idle`. The report's `description` field says what each one does.

### With Create Fly

```bash
./gradlew ":26.2.x:runBenchmark" -Pcreate
```

`-Pcreate` puts Create Fly on the benchmark server's classpath, on the nodes that build the Sand Filter
(`deps.create_fly`). The report's `environment.createFly` says whether it was loaded, and six operations
join the list:

| Operation | What it times |
|---|---|
| `createfly_filter_active` | one Sand Filter tick moving dirty water into an empty output |
| `createfly_filter_blocked` | one tick whose output holds water of another grade, so nothing moves |
| `createfly_filter_idle` | one tick with an empty input |
| `createfly_fill_bottle` | `GenericItemFilling.fillItem` filling a glass bottle, with the purity stamp |
| `createfly_empty_bottle` | `GenericItemEmptying.emptyItem`, simulated, reading a graded bottle |
| `createfly_open_pipe_draw` | `OpenEndedPipe#removeFluidFromSpace`, simulated, over the water fixture |

The operations live in `src/dev/createfly/java` and are reached by name from `InteractionScenario`, so
nodes without Create Fly compile nothing of them. The filter is a real block entity attached to the
level but not placed, so the server never ticks it behind the benchmark's back. Everything else in the
report still runs, so comparing a run with and without `-Pcreate` shows what installing Create Fly
changes for the mod's own work.

### Memory

- `thirstDataBytes`, `exhaustionTrackerBytes`: the shallow size of each object, from allocating ten
  thousand of them.
- `firstTouchBytesPerPlayer`: allocated the first time a player drains, attachment creation included.
- `steadyTickBytesPerPlayer`: allocated by one ordinary exhaustion charge plus thirst tick afterwards.
- `thirstCacheEntries`, `purityInfoEntries`: the sizes of `ThirstApi.CACHE` and `WaterPurity.INFO`.
- `heapUsedAfterGcBeforeMiB`, `heapUsedAfterGcAfterMiB`, `heapRetainedKiB`: whole-heap use after a full
  collection, taken once the benchmark area has loaded and again after the run has let go of its players,
  with the area still loaded so chunk memory cancels out. The first run on a freshly started server also
  counts what stays for the server's lifetime: the thirst cache and vanilla's per-UUID stats and
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

- Force-loads 5x5 chunks around chunk (0, 0), or around the nearest of a grid of candidates 32 chunks
  apart that is outside spawn protection and not an ocean or a beach (sea water has no grade to stamp
  and never hydrates, so the fill and drink interactions would fail there), and builds a small water fixture a few blocks below the build limit. Both are
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

## The agent client

A way for an agent to drive a real Minecraft client and read real numbers out of it, in place of
counting droplets in a screenshot. It exists for the checks in
[docs/dev/MANUAL-TESTING.md](../../docs/dev/MANUAL-TESTING.md) that a gametest cannot reach because
they are client side: the HUD, what a client is told, the config screen. The design and what is left
to do are in [docs/dev/AGENT-CLIENT-PLAN.md](../../docs/dev/AGENT-CLIENT-PLAN.md); this is how to use
it.

It is on every node, Fabric and NeoForge, on every run task except `runGametest` and `runDatagen`.
The two extra clients, `runManualA` and `runManualB`, are on the NeoForge nodes only.

### The queue

Two text files in a directory, one line of JSON each way. No port, no firewall prompt, and the whole
exchange is still on disk to read afterwards.

    run/<node>/agent/<name>/
        ready.json    written once the game will answer; holds startedAt, pid and the command list
        in.jsonl      the agent appends one request a line
        out.jsonl     the game appends one reply a line
        previous-*    the run before this one, kept rather than overwritten
        screenshots/  what client.capture wrote

`<name>` is `server` or `client` by default and `A` or `B` on the two extra clients, so a server and
a client of the same node never share a file. One queue per process: a client with an integrated
server is one process and has one queue, polled on the client tick, and `server.*` reaches its
integrated server from there.

A request is `{"id": "...", "command": "...", "args": {...}}`; `id` is echoed back and may be left
out, in which case the position in the queue stands in for it. A reply is

```json
{"id":"set","sequence":3,"command":"server.thirst.set","ok":true,"result":{...},"tookMs":1.1}
```

with `error` instead of `result` when `ok` is false. **Requests are answered in order, one at a
time.** A command that takes ticks to finish — `wait`, `client.hold`, `client.capture`,
`client.respawn` — holds the next request up until it has answered, so a file of requests is a
sequence rather than a batch of things that all happen in one tick.

`drive.py` also accepts an `expect` object keyed by dotted reply paths, plus relational `checks`.
Expectations use exact JSON equality; a mismatch is printed as `assertionErrors` and exits 1. A check
has `left`, `op`, and either another reply path in `right` or a literal in `value`. The operations are
`eq`, `ne`, `lt`, `le`, `gt`, `ge`, `contains`, `not_contains`, and `within` (with `tolerance`).

`client.hud` includes numeric rectangles from the real thirst, food and air draw calls.
`client.hud.toggle` changes the same vanilla hidden-HUD state as F1, so a script can assert both the
flag and that the thirst row's last-draw age stops refreshing, then toggle it back.

**Wait for this run's `ready.json`, not for the file.** The previous run leaves one behind, and a
game takes most of a minute to come up; a request written into `in.jsonl` before the game opens the
queue is rotated into `previous-in.jsonl` and never answered. `startedAt` is there to tell the two
apart.

### Driving it

Appending a line and reading `out.jsonl` is the whole protocol, so an agent with only file tools
needs nothing else. [tools/agent/drive.py](../../../tools/agent/drive.py) does the matching up:

```bash
python tools/agent/drive.py run/1.21.11-neoforge/agent/server tools/agent/server-probe.jsonl
```

```bash
echo '{"command": "client.state"}' | python tools/agent/drive.py run/manual-1.21.11-neoforge-A/agent/A -
```

It waits for each answer, prints them in the order asked, and exits 1 if any was refused. Pass
`--ready <seconds>` when the game is still starting: it then waits for a `ready.json` newer than the
moment the command began. Requests can be read from a file or from standard input, and a line
starting with `//` is a comment.

Unattended, without an agent at all:

```bash
./gradlew ":1.21.11-neoforge:runServer" -Pagent=tools/agent/server-probe.jsonl
```

`-Pagent=<file>` answers that file once the game is up and then stops the game. The path is relative
to the repository root. It applies to every run task of the node, clients included.

### Driving a client while the machine is in use

A client that is being driven is not being played, and two things a played client does get in the way
of using the desktop it opened on. `-Pdriven` turns both around:

```bash
./gradlew ":26.1.x-neoforge:runManualA" -Pdriven
```

- **It never takes the mouse pointer.** Minecraft grabs the cursor as soon as a window with a world
  open is focused, and holds it inside the frame until a screen opens, so clicking the window to
  glance at the bar costs the pointer everywhere else on the desktop. A driven client refuses the
  grab outright, which costs nothing: every key the agent holds goes through the game's own key state
  and every command through the player's connection, so nothing here was ever steered by a physical
  mouse. Refusing it rather than releasing it a tick later is what keeps the cursor from being hidden
  and warped to the middle of the window on every click.
- **It opens maximised**, rather than at the small size the run tasks ask for, which is what makes the
  HUD worth looking at while a script drives it. Maximised and not full screen, on purpose: exclusive
  full screen takes over the display the person is working on.

`-Pagent=<file>` implies `-Pdriven`, because an unattended run has nobody at the keyboard. It is off
by default otherwise, because it is the opposite of what a manual pass needs: with no grab there is
no mouse look and no click reaches the world, so a person — or computer use standing in for one —
cannot play the client at all. `client.info` answers `driven` and `mouseGrabbed`, so a script can
tell which kind of client it is talking to rather than assuming.

It is client side only. `-Pdriven` on `runServer` does nothing, and passing it to every task of a
node is harmless.

### The commands

`ready.json` lists what the process it belongs to answers; a server answers the first two groups and
a client answers all three.

| Command | Arguments | Answers |
|---|---|---|
| `probe` | | side, loader, Minecraft version, run directory, queue, whether a server is running, the command list |
| `wait` | `ticks` | after that many game ticks have run. The way to let the game catch up |
| `stop` | | after stopping this process: halting a server, closing a client's window |
| `server.info` | | dedicated, tick count, players, difficulty, levels |
| `server.players` | | every online player's thirst, position, health, food and flags |
| `server.thirst.get` | `player` | the same, for one player |
| `server.thirst.set` | `player`, `thirst`, `quenched`, `exhaustion`, `enabled` | what it was and what it is now. Writes the state directly, not through `/thirst set` |
| `server.command` | `command`, `as` | what the command returned and what it said, collected rather than logged |
| `client.info` | | window and GUI size, GUI scale, fps, screen, server, player, key names, `toggleCrouch`, `toggleSprint`, and whether this client is `driven` and `mouseGrabbed` |
| `client.state` | | what this client holds: thirst, sprinting, sneaking, health, food, dimension, position, whether the bar should render |
| `client.hud` | | the rectangle the mod drew the bar in, the values it drew, the ten droplet rectangles, and the config preview's |
| `client.hud.toggle` | | the new hidden state after toggling the same vanilla state as F1 |
| `client.capture` | `name` | a PNG beside the queue, once it is on disk |
| `client.pixels` | `points`, `space`, `capture`, `name` | the framebuffer colour at each point, in GUI pixels by default |
| `client.command` | `command` | after sending it through the player's own connection |
| `client.chat` | `message` | after sending it |
| `client.hold` | `keys`, `ticks` | the movement state before and after holding those keys for that long |
| `client.key` | `key`, `down` | after setting one key's state and leaving it there |
| `client.screen` | `open` (`none`, `config`) | which screen is open now |
| `client.tooltip` | `item`, `count`, `advanced` | the tooltip lines that item produced, as text, with their colours |
| `client.respawn` | | after pressing the death screen's button through the connection |
| `client.disconnect` | | after leaving to the title screen |
| `client.connect` | `address` | after starting a connection; `wait` for it to finish |

`/thirst agent probe` in game says the same thing `probe` does, for the moment before an agent is
wired up at all. It needs permission level 4.

Two things about input. It goes through the game's own key state, never the operating system, so a
key the agent holds stays held for as many ticks as it asks — which is what makes the sprint gate
readable. But vanilla's Sneak and Sprint accessibility settings turn those two keys into toggles, and
the dev clients here have `toggleCrouch:true`, so holding sneak for thirty ticks crouches the player
and leaves them crouching. `client.info` answers both settings; read the state back rather than
assuming. The dev client also turns `pauseOnLostFocus` off when it opens the agent: an unattended
window is necessarily unfocused, and otherwise vanilla repeatedly opens `PauseScreen` and suppresses
the movement keys the agent is deliberately holding.

### What a check looks like

[tools/agent/client-sync.jsonl](../../../tools/agent/client-sync.jsonl) is MANUAL-TESTING.md's "Sync
to the client" section, the four items one client can answer, with the expected answer written above
each one. Start `runServer` and `runManualA`, put the player somewhere flat with `fall_damage` off,
and:

```bash
python tools/agent/drive.py run/manual-1.21.11-neoforge-A/agent/A tools/agent/client-sync.jsonl
```

The fifth item, that each player sees only their own bar, needs three queues and so is not one file.
Start `runServer`, `runManualA` and `runManualB`, stand the two testers together, then write
different values from the server's queue and read both clients' `client.hud` back:

```bash
python tools/agent/drive.py run/1.21.11-neoforge/agent/server - <<'EOF'
{"command": "server.command", "args": {"command": "tp TesterB 21 103 20"}}
{"command": "server.thirst.set", "args": {"player": "TesterA", "thirst": 6, "quenched": 0}}
{"command": "server.thirst.set", "args": {"player": "TesterB", "thirst": 14, "quenched": 2}}
{"command": "wait", "args": {"ticks": 20}}
EOF
```

Then `client.hud` on each of `run/manual-<node>-A/agent/A` and `run/manual-<node>-B/agent/B`, and
swap the two values and read again, so that "each client kept what it already had" cannot pass for
"each client was told its own".

### Rules

- **Every probe answers a number or a string, never a picture.** `client.capture` exists so a person
  can look at the frame afterwards. The moment an assertion depends on a screenshot, the item is not
  automated.
- **No change to the mod for the agent's sake.** Where a probe cannot reach something, read it from
  this source set instead — that is what `dev/mixin/ThirstHudMixin` and `HudRecord` are, rather than
  the mod recording its own rectangles.
- **`dev/agent/core` knows nothing of Minecraft, of a loader or of the mod.** `checkAgentCore`, in
  `gradle/shared.gradle.kts`, fails the build when a class there imports one. Everything that names
  them lives in `dev/agent/thirst`.
- **A check that becomes a number leaves MANUAL-TESTING.md**, the same rule that file already states
  for gametests.

## Rules

These are the benchmark's; the agent's are in its own section above.

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
