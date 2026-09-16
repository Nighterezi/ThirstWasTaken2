# src/dev — development-only tooling

Tools for working on the mod that must never ship. Like `src/gametest`, this is its own source set and
its own small mod, so none of it can reach a published jar: `thirstwastaken2-dev` on Fabric, declared
in `src/dev/resources/fabric.mod.json`, and `thirstwastaken2_dev` on NeoForge, declared in
`src/dev/neoforge/resources/META-INF/neoforge.mods.toml`, because a NeoForge mod id cannot contain a
hyphen. `runGametest` and `runDatagen` do not load it; every other run task does — `runServer`,
`runBenchmark` and `runClient` on Fabric, and `runServer`, `runBenchmark`, `runClient`, `runManualA`
and `runManualB` on NeoForge.

Two tools live here.

| Tool | What it is for | Where | Its own file |
|---|---|---|---|
| `/thirst benchmark` | what the mod costs a server, with nobody joining | every node | [benchmark/AGENTS.md](com/thirstwastaken2/dev/benchmark/AGENTS.md) |
| the agent client | driving a real client and reading numbers out of it | every node | [agent/AGENTS.md](com/thirstwastaken2/dev/agent/AGENTS.md) |

Each has a directory under `tools/` for what is sent to it or read back out of it from outside the
game: `tools/agent/` writes a client's request queue, `tools/benchmark/` runs the benchmark over every
node k times and reduces the reports to a median and a spread. Both are documented in the tool's own
file, not here.

Each tool documents itself beside its own code — how to run it, what it answers and the rules that
keep it worth having. This file is what they have in common: where things live, the gate they are
all behind, the harness they share, and the rules that hold for everything in the source set.

The split of the directory follows that. `benchmark/` is the first; `agent/core` and `agent/thirst`
are the second; [`harness/`](#the-shared-harness) is the little both of them need; `mixin/` is how
the agent reads the HUD's real draw calls and refuses the mouse grab; `ThirstDev` and `ThirstDevClient` are
the two halves of the entrypoint, kept apart so that a dedicated server never loads a class naming
`Minecraft`; `fabric/` and `neoforge/` hold each loader's entrypoints, the small
`DevLoader`/`DevClientLoader` seam for the calls the mod's own `Loader` does not cover, and one copy
each of `BenchmarkPlayer`, which is the whole of the benchmark that names a loader.

`ThirstDev` registers nothing unless `ThirstWasTaken2.DEV` is true. That flag is the loader's own
development-environment check, true under every run task and false in the jar players install.
`-Dthirstwastaken2.dev=true|false` overrides it either way.

## The shared harness

`harness/` is what both tools need and neither owns. It was pulled out of them once each had grown a
copy of its own, and not before.

| In `harness/` | Why both wanted it |
|---|---|
| `ServerAwake` | A dedicated server with nobody online stops ticking after `pause-when-empty-seconds`, and a paused server fires no tick. A benchmark run typed in late never advances and a long one stalls a minute in; an agent's queue stops being polled a minute after startup, before a client can be brought up. Both used to reset vanilla's `emptyTicks` by reflection themselves, and only one of the two copies bothered to check the server was dedicated |
| `DevEnvironment` | Which loader, which Minecraft, which build of the mod, whether the DEV gate is open, where the run directory is. The benchmark report's `environment` block, the agent's `ready.json` and the agent's `probe` all answer that question, and the benchmark used to read the mod's version out of `FabricLoader` while the agent read the same version out of `DevLoader` |
| `Autorun` | `-Pbenchmark` and `-Pagent` both reach the game as a pair of system properties: one naming the work and one, always the first with `.exit` on the end, saying the game stops once it is done. An unattended run starts that work as soon as the game can accept it and ends with a `DONE` line, because the Gradle task exits 0 whatever happened |

`BenchmarkWorld` stayed in `benchmark/`. Placing blocks and putting them back has exactly one caller,
and a seam with one side to it is a guess about the second; the agent drives a real client and asks
the server for what it needs by command. Move it when something else actually wants it.

Two rules hold this together.

- **The harness names no mod loader.** Everything loader-specific goes through `DevLoader`. No Gradle
  task checks it and none is needed: `harness/` is compiled into the NeoForge dev mod too, and CI
  builds `devClasses` on every node, so a Fabric import here fails four of them. The same now holds
  for `benchmark/` and for `agent/`: both are built on every node, and the loader classes each needs
  live in `fabric/` and `neoforge/` under one name.
- **It may name Minecraft and the mod**, unlike `agent/core`, which `checkAgentCore` holds to plain
  Java and Gson. The harness is the half that is allowed to know where it is running.

## Rules for everything here

Each tool has rules of its own, in its own file. These hold for all of it.

- Nothing in `main` or `client` may reference this source set.
- Everything here compiles unchanged on every supported version. If a vanilla call ever needs a
  Stonecutter branch, give it a seam in this source set rather than branching at the call site, the
  same way `platform/` works for the mod.
- No change to the mod for a tool's sake. Where a tool cannot reach something, read it from this
  source set instead, rather than widening an API surface players never get. Each tool's own file
  says what that looks like for it.
- CI only compiles this source set (`devClasses`); it runs neither tool.
