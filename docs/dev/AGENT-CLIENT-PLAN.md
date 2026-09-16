# Agent client plan

How the checks in [MANUAL-TESTING.md](MANUAL-TESTING.md) that need a client stop needing a pair of
eyes: a dev-only way for an agent to drive a real Minecraft client and read real numbers out of it.
This is a working plan with an end state, not a standing rule: delete it once the last phase lands,
and promote anything that outlives it to [src/dev/java/AGENTS.md](../../src/dev/java/AGENTS.md).

## What it is for

Everything a gametest cannot reach is client side: the HUD, the config screen, and everything synced
to a client. Today those are read off screenshots with computer use, which is slow and not always
conclusive. The 1.21.1 NeoForge pass has one item still open for exactly that reason: the exhaustion
strip could not be told apart from a drained droplet in an F2 capture.

An agent client replaces the picture with a number. The client itself answers what thirst it holds,
whether it is sprinting, where it drew the bar and what colour a given pixel is. A screenshot stays
useful as evidence for a human, never as the assertion.

| Check | Today | With the agent client |
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
| **P0** | `dev` source set on the NeoForge nodes, carrying the agent only, not the benchmark | 1 to 2 days | `/thirst agent probe` answers on `1.21.11-neoforge`, and the published jar still has nothing of it |
| **P1** | Server-side agent: the file queue, the autorun property, a probe of every online player | ~1 day | an agent sets thirst and reads it back out of `out.jsonl`, with nothing parsed out of chat |
| **P2** | Client-side probe: client state, HUD rectangles, framebuffer samples, input, screenshots on demand | 2 to 3 days | the whole "Sync to the client" section ticked on `1.21.11-neoforge` from JSON alone |
| **P3** | Two clients driven at once, one queue each | ~half a day | "each player sees only their own bar" ticked from two JSON answers |
| **P4** | Experimental: a simulated player whose connection reports the mod's channel, with outgoing payloads captured | open | a gametest asserts per-player sync for N players and goes red when `syncsTo` accepts everyone |
| **P5** | Extract the shared harness out of the benchmark and the agent | ~1 day | `runBenchmark` behaves exactly as `src/dev/java/AGENTS.md` describes, report shape unchanged |

P0 comes first because `src/dev` is Fabric only today, by the decision recorded at
`build.neoforge.gradle.kts:55`, and every open manual item is on a NeoForge node. Carrying the
benchmark across at the same time would double that phase for no gain, so the NeoForge `dev` mod
starts with the agent alone. What P0 has to translate is small: the entrypoint and its command and
tick hooks, which `platform/Loader` already covers on both loaders, and the DEV gate, which
`Loader.isDevelopmentEnvironment` already answers on both. The one thing with no counterpart is
`FakePlayer`, and that belongs to the benchmark, which stays behind.

P4 is the one phase that may not be worth its cost, and it is deliberately last. It would move the
hardest remaining item, that a player is told their own thirst and no one else's, into CI without a
client at all. It also means setting a loader's negotiated channel set from dev code: on NeoForge
`syncsTo` asks `connection.hasChannel(SyncAttachmentsPayload.TYPE)` before 26.1, Fabric answers the
same question through its own registry, and both differ across four Minecraft versions. Start it only
if P1 to P3 have paid for themselves and the sync items are still eating time. Even then it proves the
server sent the right payload, never that the client drew it.

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
