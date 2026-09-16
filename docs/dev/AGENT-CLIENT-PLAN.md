# Agent client plan

How the checks in [MANUAL-TESTING.md](MANUAL-TESTING.md) that need a client stop needing a pair of
eyes: a dev-only way for an agent to drive a real Minecraft client and read real numbers out of it.
This is a working plan with an end state, not a standing rule: delete it once the last phase lands,
and promote anything that outlives it to [src/dev/java/AGENTS.md](../../src/dev/java/AGENTS.md).

## What it is for

Everything a gametest cannot reach is client side: the HUD, the config screen, and everything synced
to a client. Those used to be read off screenshots with computer use, which is slow and not always
conclusive. The 1.21.1 NeoForge pass still has one item open for exactly that reason: the exhaustion
strip could not be told apart from a drained droplet in an F2 capture.

An agent client replaces the picture with a number. The client itself answers what thirst it holds,
whether it is sprinting, where it drew the bar and what colour a given pixel is. A screenshot stays
useful as evidence for a human, never as the assertion.

| Check | Before | With the agent client |
|---|---|---|
| `/thirst set` reaches the client | count droplets in a screenshot | the client reports the value it holds |
| sprint gate at 6 and 7 | walk a measured track, compare distances | `LocalPlayer.isSprinting()` after the key is held |
| bar above hunger, bubbles above bar | look at a screenshot | the rectangles the mod drew |
| quenched outline colour, exhaustion strip | look closely at a screenshot | framebuffer samples at known points |
| each player sees only their own bar | two clients, two screenshots | two JSON answers |
| tooltips and the config screen read well | look | still look. Legibility is not a number |

## Settled decisions

**One source set, two packages.** The agent code lives in the existing `dev` source set, split into
`dev.agent.core`, which may not import anything of `com.thirstwastaken2` beyond the platform seam, and
`dev.agent.thirst`, which is free to. A new source set would have to be wired in both build scripts
across eight nodes; the boundary that actually matters is imports, and `checkLoaderSeam` already shows
that a package level check is enough in this repo. If a second project ever wants `core`, it extracts
cleanly because nothing points the wrong way across that line.

**The benchmark is not folded into this.** They share a harness, not a purpose, and three things keep
them apart. The agent client is needed on NeoForge, where every open manual item is, while the
benchmark measures server cost, which one loader answers. `/thirst benchmark` already has a stable
command and report contract that `src/dev/java/AGENTS.md` documents for agents to use unattended, and
folding it into a new tool breaks that contract for nothing. And a measurement needs a quiet server,
while an agent polling a queue and taking screenshots is noise, so the two have to run at different
times whatever the code looks like. What they genuinely share, the autorun property, the JSON report
envelope, the empty-server keep-awake and the arena that places and restores blocks, is extracted in
P5, once there are two real consumers and not before.

**A file queue, not a socket.** `run/<node>/agent/in.jsonl` and `out.jsonl`. No port to allocate, no
Windows firewall prompt, it works between a Gradle-launched client and an agent that has only file
tools, and the exchange is still readable after the run. Revisit only if latency becomes the problem,
which at one command per second it is not.

**Simulated players stay server side.** The benchmark's `FakePlayer` is the right tool for load and
for server-side behaviour at scale, and the wrong tool for anything synced, because a fake connection
drops every packet. The agent client scales by running more real clients instead, and there are only
ever a handful of those. See [Many players](#many-players).

## Phases

| Phase | Work | Estimate | Gate to move on |
|---|---|---|---|
| **P0** ✅ | `dev` source set on the NeoForge nodes, carrying the agent only, not the benchmark | 1 to 2 days | `/thirst agent probe` answers on `1.21.11-neoforge`, and the published jar still has nothing of it |
| **P1** ✅ | Server-side agent: the file queue, the autorun property, a probe of every online player | ~1 day | an agent sets thirst and reads it back out of `out.jsonl`, with nothing parsed out of chat |
| **P2** ✅ | Client-side probe: client state, HUD rectangles, framebuffer samples, input, screenshots on demand | 2 to 3 days | the whole "Sync to the client" section ticked on `1.21.11-neoforge` from JSON alone |
| **P3** ✅ | Two clients driven at once, one queue each | ~half a day | "each player sees only their own bar" ticked from two JSON answers |
| **P4** ✅ | Experimental: a simulated player whose connection reports the mod's channel, with outgoing payloads captured | open | a gametest asserts per-player sync for N players and goes red when `syncsTo` accepts everyone |
| **P5** | Extract the shared harness out of the benchmark and the agent | ~1 day | `runBenchmark` behaves exactly as `src/dev/java/AGENTS.md` describes, report shape unchanged |

### Where this has got to

Written down here rather than in a commit message, because the next person to pick this up needs the
half-finished parts named. Update it as phases close, and delete the whole file once P5 lands.

| Phase | State | What is left |
|---|---|---|
| **P0** | **Done**, gate proven on 2026-09-16 | |
| **P1** | **Done**, gate proven on 2026-09-16 | |
| **P2** | **Done** on `1.21.11-neoforge`, gate proven on 2026-09-16 | the same run on the other three NeoForge nodes and on Fabric |
| **P3** | **Done** on `1.21.11-neoforge`, gate proven on 2026-09-16 | the same |
| **P4** | **Done**: the test now goes red under the mutation | |
| **P5** | Not started | all of it |

**How each gate was met.** All four ran against `1.21.11-neoforge` on 2026-09-16. Every request and
every answer is still in that node's queues under `run/`, which is not committed, so the summary here
is the record: re-run the two request files to see it again.

- **P0.** `runServer -Pagent=tools/agent/server-probe.jsonl` opened the queue, answered four requests
  and stopped the server. One of them was `server.command` running `thirst agent probe`, whose reply
  carries the command's own output. The published jar has nothing of `dev`, `agent`, `benchmark` or
  `gametest` in it, on this node as on 26.2.
- **P1.** With a client joined, `server.thirst.set` wrote `TesterA`'s thirst and `server.thirst.get`
  read it back, both out of `out.jsonl`, with nothing parsed out of chat.
- **P2.** [tools/agent/client-sync.jsonl](../../tools/agent/client-sync.jsonl) ran the whole "Sync to
  the client" section in one pass: the client held 7 and sprinted, held 6 and walked, the HUD record
  said what it drew each time, the bar came back full after a death, held 9/3 through the Nether and
  back, and held 9/3 through a disconnect and a rejoin. `client.pixels` read the quenched outline off
  the framebuffer as `#FF47ACF7` at the droplet the HUD record named.
- **P3.** `runManualA` and `runManualB` on one `runServer`, standing together, written to from the
  server's queue: A drew 6/0 and B drew 14/2, then the two values were swapped and each drew the
  other's. Two JSON answers, no screenshots.
- **P4.** `PlayerSyncGameTest` goes red with `Loader.syncsTo` changed to accept everyone, which is the
  mutation the gate names. See below.

**What running them changed.** Four things were wrong and are now fixed; all four were invisible until
something was actually run.

1. *Nothing launched at all.* The NeoForge run source sets assigned `runtimeClasspath` rather than
   adding to it, which threw away the source set's own configuration — where ModDevGradle puts
   DevLaunch, whose `Main` every run is started through. Every NeoForge run task failed with
   `Could not find or load main class net.neoforged.devlaunch.Main`.
2. *A script was a batch, not a sequence.* `wait` deferred its own answer and nothing else, so every
   line the poll read ran in that one tick: "hold sprint at 7, set 6, hold sprint again" sent both
   commands two milliseconds apart and started both holds together, and the two answers came out
   identical. `AgentDispatcher` now starts one request at a time and holds the next until an
   outstanding deferral has answered.
3. *A stale `ready.json` swallowed the first request.* Waiting for the file is not enough: the last
   run leaves one behind, and a game takes most of a minute to come up, so a request written in that
   window goes into the file the new run is about to rotate away. `ready.json` now carries `startedAt`
   and `pid`, and `drive.py` waits for one newer than the moment it started.
4. *An empty dedicated server stopped answering.* It pauses after `pause-when-empty-seconds` and a
   paused server fires no tick, so the queue died a minute after startup, before a client could be
   brought up. `ServerAwake` holds the pause off while the queue is open, the way `BenchmarkRunner`
   already did for a run. Those two are one of the things P5 has to extract.

Two commands were added because a check needed them and nothing in the list could reach them:
`client.respawn`, since vanilla's only way back from a death screen is that button and clicking at its
coordinates is exactly the assertion-about-a-picture this exists to avoid; and `toggleCrouch` /
`toggleSprint` in `client.info`, because the dev clients have Sneak set to toggle, so a key held for
thirty ticks crouches the player and leaves them crouching.

**What is on disk.** `src/dev` is split the way the settled decisions describe: `dev/agent/core`
is the queue, the reply envelope and the dispatch loop in plain Java and Gson, and `checkAgentCore`
(in `gradle/shared.gradle.kts`, run by CI on every node) fails the build if a class there imports
Minecraft, a loader or the mod. `dev/agent/thirst` is everything that names them. The dev tools have
loader directories of their own — `src/dev/fabric` and `src/dev/neoforge` — holding the
entrypoints and a small `DevLoader`/`DevClientLoader` seam for the four calls the mod's own `Loader`
does not cover. `ThirstDev` is the server half and names no client class; `ThirstDevClient` is the
client half. The NeoForge nodes carry the dev tools as `thirstwastaken2_dev`, with the benchmark
package excluded from the source set rather than ported. `tools/agent/` holds `drive.py` and the two
request files; [src/dev/java/AGENTS.md](../../src/dev/java/AGENTS.md) documents the queue, the command
list and what a check looks like.

The queue is `run/<node>/agent/<name>/`, holding `ready.json`, `in.jsonl` and `out.jsonl`, with the
previous run's pair kept beside them. `<name>` is `server` or `client` by default and `A`/`B` on the
two extra NeoForge clients, so a server and a client of one node never share a file. One queue per
process, polled on that process's own tick: `server.*` reaches an integrated server as readily as a
dedicated one. `-Pagent=<file>` answers a file of requests once and stops the game.

**P4, and what was wrong with it.** `PlayerSyncGameTest` places three `SyncPlayer`s — real
`ServerPlayer`s in the level whose connection reports the mod's channel and keeps every packet sent to
it — and asserts that writing one player's thirst produces a payload for that player and for nobody
else. It used to pass with `Loader.syncsTo` changed to `return true`, which is the mutation the gate
names, so it proved less than it claimed.

The cause was that both loaders build their sync list from who is *tracking* the player, and the
tracking never completed. `ChunkMap.addEntity` does ask once who can see a new player, but that is
before a single chunk has been sent, so `isChunkTracked` refuses and the answer is nobody; afterwards
`ChunkMap.tick` only asks again for an entity whose section has changed since the last tick, and three
players standing still never change section. `seenBy` stayed empty, the sync list was `[self]`
whatever the predicate said, and the mutation went unnoticed.

`SyncPlayer.settle` now does both halves once a tick: it sends the chunks the tracking view is waiting
on, and it moves each player a section up and back down again — vertically, so the chunk they are in
never changes while the section this is read from does. And the fixture is no longer trusted on
reasoning: `SyncPlayer.reachedByBroadcastAbout` broadcasts about one of them through the same
mechanism the sync uses and requires all three to receive it, so a run where nobody is watching anybody
fails as a broken fixture rather than passing on nothing. With that in place the mutation makes the
test red, checked on 2026-09-16 on `1.21.11-neoforge` and on `26.2.x-neoforge` — one node either side
of the version branch in `syncsTo` — and reverted afterwards. All four NeoForge nodes and 26.2 Fabric
run their whole suite green without it: 125 tests on 1.21.1, 126 on the rest.

**What is left.** P5, and running P2 and P3 on the other seven nodes. Neither is blocked: the same two
request files and the same two-client procedure answer on any of them.

**Why the order was this**, kept because it still explains the shape of what landed. P0 came first
because `src/dev` was Fabric only, by a decision `build.neoforge.gradle.kts` used to record where it
now explains carrying the agent across, and every open manual item was on a NeoForge node. Carrying the
benchmark across at the same time would have doubled that phase for no gain, so the NeoForge `dev` mod
carries the agent alone; `FakePlayer` has no counterpart there and belongs to the benchmark, which
stayed behind. What P0 had to translate was small, because `platform/Loader` already covered the
entrypoint and the command and tick hooks on both loaders, and `Loader.isDevelopmentEnvironment`
already answered the DEV gate on both.

P4 was last because it was the one phase that might not be worth its cost. It proves only that the
server sent the right payload, never that the client drew it, and it means reporting a loader's
negotiated channel set from dev code: on NeoForge `syncsTo` asks
`connection.hasChannel(SyncAttachmentsPayload.TYPE)` before 26.1 and simply compares the holder after
it. It turned out cheap — `ServerGamePacketListenerImpl` answers `hasChannel` itself, so
`CapturingConnection` is one override — and what cost the time was not the channel at all but getting
the chunk map to have the simulated players watching each other. Fabric has no counterpart and is not
covered: there the check is two agent clients.

## What P2 has to read

The probe answers questions, so the list of questions is the design. Each is a value the client
already holds; none of them needs a change to the mod.

| Question | Where the answer comes from |
|---|---|
| what thirst does this client hold | `ThirstManager.get(minecraft.player)` |
| is it sprinting | `LocalPlayer.isSprinting()`, after the agent holds the sprint key |
| where did the mod draw the bar | the rectangle the HUD code used, recorded as it draws |
| what colour is the outline, is the strip there | framebuffer samples at points derived from that rectangle |
| is the bar hidden | the same state F1, creative and a living mount feed |
| what does this tooltip say | the lines the item produced, as text |
| which screen is open | `minecraft.screen`, by class |

Input goes through the game, never through the operating system: commands through
`player.connection`, keys by setting the game's own key state, screens through `minecraft.setScreen`.
That is what removes the footnote the `manual-testing` skill carries today, that a Shift modifier on a
single click is released before the server sees the player crouch.

## Many players

Three different things get called "many players", and only one of them is what a given check needs.

| Way | Cost each | Sees the HUD | Gets synced | Practical ceiling |
|---|---|---|---|---|
| Server-side simulated player (`FakePlayer`, what the benchmark uses) | almost nothing | no | no, the fake connection drops packets | 2000, already tested |
| Real modded client (`runClient`, `runManualA`, `runManualB`) | a whole Minecraft client | yes | yes | 2 to 4 on this machine |
| Vanilla protocol bot | small | no | no, it never negotiates the mod's channel | many, and useless here |
| Simulated player with a captured connection (P4) | almost nothing | no | yes, into a buffer the test reads | as many as the test wants |

So the answer depends on the check. Anything about cost, drain, loot or damage at scale is already
served by the benchmark's simulated players and should stay there. Anything about the HUD or about
what a client is told needs real clients, and two is enough for every item in MANUAL-TESTING.md. P4 is
the only path to both at once, and its ceiling is that it still cannot see a pixel.

## Rules that keep this cheap

- **Nothing reaches a published jar.** The same rule the benchmark and the gametests already follow:
  own source set, own small mod, gated on `ThirstWasTaken2.DEV`, checked with `unzip -l`.
- **Every probe answers a number or a string, never a picture.** A screenshot is evidence for a human
  reading the report later. The moment an assertion depends on one, the item is not automated.
- **No change to the mod for the agent's sake.** If a probe cannot reach something, read it in dev
  code rather than widening the mod's API surface for a tool players never get.
- **A check that becomes a number leaves MANUAL-TESTING.md.** The rule that file already states for
  gametests: when a check moves into automation, delete it from the list rather than leaving it to be
  run twice.
