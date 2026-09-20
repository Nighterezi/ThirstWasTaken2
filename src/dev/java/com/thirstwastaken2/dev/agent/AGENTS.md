# The agent client

A way for an agent to drive a real Minecraft client and read real numbers out of it, in place of
counting droplets in a screenshot. It exists for the checks in
[docs/dev/MANUAL-TESTING.md](../../../../../../../docs/dev/MANUAL-TESTING.md) that a gametest cannot reach because
they are client side: the HUD, what a client is told, the config screen. What used to be read off a
screenshot is read as a value the client already holds: `/thirst set` reaching the client is the
number the client reports, the sprint gate is `LocalPlayer.isSprinting()` after the key has been
held, the bar's place above the hunger bar is the rectangles the mod drew, and the quenched outline's
colour is a framebuffer sample at a point those rectangles name. A screenshot stays useful as
evidence for a person; it is never the assertion.

It is on every node, Fabric and NeoForge, on every run task except `runGametest` and `runDatagen`.
The two extra clients, `runManualA` and `runManualB`, are on the NeoForge nodes only.

`core/` is the queue, the reply envelope and the dispatch loop, in plain Java and Gson; `thirst/` is
everything that names Minecraft, a loader or the mod. [src/dev/java/AGENTS.md](../../../../AGENTS.md)
is the source set this belongs to, and holds the rules every tool here follows and the harness this
one shares with `/thirst benchmark`.

## The queue

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
`client.slot`, `client.respawn` — holds the next request up until it has answered, so a file of requests is a
sequence rather than a batch of things that all happen in one tick.

`drive.py` also accepts an `expect` object keyed by dotted reply paths, plus relational `checks`.
Expectations use exact JSON equality; a mismatch is printed as `assertionErrors` and exits 1. An
expected `null` passes when the path is absent as well as when it is null, because Gson leaves a null
property out of the reply: that is how "no screen is open" is written. A check has `left`, `op`, and
either another reply path in `right` or a literal in `value`. The operations are `eq`, `ne`, `lt`,
`le`, `gt`, `ge`, `contains`, `not_contains`, and `within` (with `tolerance`); `contains` on a list of
messages is membership, not a substring, so a whole message goes in `value`.

`client.hud` includes numeric rectangles from the real thirst, food and air draw calls.
`client.hud.toggle` changes the same vanilla hidden-HUD state as F1, so a script can assert both the
flag and that the thirst row's last-draw age stops refreshing, then toggle it back.

A dedicated server's queue is polled on the server tick, and a dedicated server with nobody online
stops ticking after `pause-when-empty-seconds`, which would kill the queue about a minute after
startup — before an agent has had time to bring a client up. `ServerAwake`, in the source set's
[shared harness](../../../../AGENTS.md#the-shared-harness), holds that pause off for as long as the
queue is open. A client's integrated server is left to pause on the player's own rules.

**Wait for this run's `ready.json`, not for the file.** The previous run leaves one behind, and a
game takes most of a minute to come up; a request written into `in.jsonl` before the game opens the
queue is rotated into `previous-in.jsonl` and never answered. `startedAt` is there to tell the two
apart.

## Driving it

Appending a line and reading `out.jsonl` is the whole protocol, so an agent with only file tools
needs nothing else. [tools/agent/drive.py](../../../../../../../tools/agent/drive.py) does the matching up:

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
to the repository root. It applies to every run task of the node, clients included. Nothing in the
game reads the `expect` and `checks` lines beside the requests, so a run like this is checked
afterwards against what it recorded:

```bash
python tools/agent/drive.py run/1.21.11-neoforge/agent/server tools/agent/server-probe.jsonl --verify
```

A client script that needs a world gets one with `-Pquickplay=<world>`, which opens that singleplayer
world of `run/<node>/saves` straight from launch; server commands then reach its integrated server.
[tools/agent/hanging-pot.jsonl](../../../../../../../tools/agent/hanging-pot.jsonl) runs that way, in a
throwaway world made from another world's `level.dat`.

## Driving a client while the machine is in use

A client that is being driven is not being played, and four things a played client does get in the way
of using the desktop it opened on, or of the run going anywhere at all. `-Pdriven` turns them around:

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
- **It passes vanilla's experimental settings prompt.** "Worlds using Experimental Settings are not
  supported" stands between `-Pquickplay` and the world whenever a mod on the classpath turns a feature
  flag on, which the Supplementaries and Moonlight Lib clients do, so an unattended run waits on a
  button nobody is there to press. A driven client presses "I know what I'm doing!" for itself and
  writes a line to the log; a played one still gets to read it. `ClientWindow.passWorldPrompt`, on the
  client tick while no world is loaded. It finds the button by its message rather than by naming the
  screen, so a version that moves that screen leaves the prompt standing rather than failing to build.
- **On NeoForge it passes the "warnings while loading mods" screen** when no warning on it is this
  mod's, as if the button had been pressed, and logs each warning it passes over. Other mods' warnings
  (a deprecated `logoFile` in Cloth Config, AppleSkin's old translation key) otherwise stop a
  `-Pquickplay` or `-Pagent` client before the world opens. It stays on the screen for an error, for a
  warning about this mod, and for a warning that names no mod. `LoadingWarnings` in
  `src/dev/neoforge`; the screen keeps its issues private, so it reads them by reflection.

`-Pagent=<file>` implies `-Pdriven`, because an unattended run has nobody at the keyboard. It is off
by default otherwise, because it is the opposite of what a manual pass needs: with no grab there is
no mouse look and no click reaches the world, so a person — or computer use standing in for one —
cannot play the client at all. `client.info` answers `driven` and `mouseGrabbed`, so a script can
tell which kind of client it is talking to rather than assuming.

It is client side only. `-Pdriven` on `runServer` does nothing, and passing it to every task of a
node is harmless.

## The commands

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
| `client.screen` | `open` (`none`, `config`, `inventory`) | which screen is open now |
| `client.tooltip` | `item`, `count`, `advanced` | the tooltip lines that item produced, as text, with their colours |
| `client.click` | `x`, `y`, `button`, `from` (`centre`, `corner`, `top`, `bottom`) | after pressing and releasing a mouse button on the open screen: which child was under the point and whether the press was taken |
| `client.slots` | | the open menu's slots that hold something, with their class and player inventory index, and what the cursor carries |
| `client.slot` | `slot` or `inventory`, `button`, `action` (`pickup`, `quick_move`, …) | the same, a few ticks after clicking that slot through the game mode |
| `client.language` | `code` | the language now selected, after loading its translations again |
| `client.textWidth` | `keys`, `texts` | how wide the game's font draws each, in GUI pixels, keys translated in the current language |
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

Clicks are for controls nothing else reaches, another mod's settings buttons for instance; a screen the
game can open, and a slot, have commands of their own. `client.click` measures from the centre of the
screen by default because a container screen is centred, so a control on one keeps its offset from
the centre at any window size. A screen made of a header, a list and a footer anchors its rows to an
edge rather than to the centre, so `from: "top"` and `from: "bottom"` keep x measured from the centre
and measure y from that edge, which is what makes a script for the config screen portable between
window sizes. `button` is 0 left, 1 right and 2 middle everywhere: 26.3 moved the client from GLFW to
SDL, which numbers the buttons from one, and `AgentClientVanilla.click` turns the number back so that
a file written on one node presses the same button on another.

`client.slot` takes a player inventory index as `inventory`, the `container.N` of `/item replace`,
and finds that slot in whatever menu is open. `client.language` loads only the translations again: a full resource reload in a world freed a font atlas that Jade's
overlay drew from a frame later and crashed the client, and the fonts already hold every script.

## What a check looks like

[tools/agent/client-sync.jsonl](../../../../../../../tools/agent/client-sync.jsonl) is MANUAL-TESTING.md's "Sync
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

## Many players

Three different things get called "many players", and only one of them is what a given check needs.

| Way | Cost each | Sees the HUD | Gets synced | Practical ceiling |
|---|---|---|---|---|
| Server-side simulated player (`BenchmarkPlayer`, a Fabric `FakePlayer`) | almost nothing | no | no, the fake connection drops every packet | 2000, already measured |
| Real modded client (`runClient`, `runManualA`, `runManualB`) | a whole Minecraft client | yes | yes | 2 to 4 on a desktop |
| Vanilla protocol bot | small | no | no, it never negotiates the mod's channel | many, and useless here |
| Simulated player with a captured connection (`SyncPlayer`, in `src/gametest`) | almost nothing | no | yes, into a buffer the test reads | as many as the test wants |

So the answer depends on the check. Anything about cost, drain, loot or damage at scale belongs to
the benchmark's simulated players. Anything about the HUD needs real clients, and two is enough for
every item in MANUAL-TESTING.md — which is why `runManualA` and `runManualB` exist and why there is
no third. Anything about what a client is *told*, for many players at once, is
`PlayerSyncGameTest`; its ceiling is that it still cannot see a pixel.

The agent client deliberately does not scale by simulating players. A fake connection drops every
packet, so a simulated player can never answer a question about sync or about the HUD, which is the
only kind of question this tool exists for.

## Known issues

**The Fabric 26.2.x client crashes on its way out, after the script has been answered.** The run
prints `[ThirstAgent] DONE`, then about 15 seconds later `java.lang.Error: Watchdog (Client shutdown
from post-main)`, writes a crash report to `run/26.2.x/crash-reports/`, and Gradle reports exit value
-8 and `BUILD FAILED`. The replies in `out.jsonl` are complete; nothing the script asked failed. Until
this is gone, a 26.2.x run's result is its `DONE` line, not the Gradle task's exit.

It is Create Fly's, not the agent's. Create Fly (`26.2-rc-2-6.0.9-1`, the newest for 26.2 on
2026-09-19) starts Flywheel's `Flywheel Task Executor #N` threads as non-daemon threads and never
stops them: nothing calls `ParallelTaskExecutor.stopWorkers()`. Up to 26.1, `Minecraft.destroy()`
ended in `System.exit(0)`, which took them down with everything else, so 26.1.x has the same threads
and still exits cleanly. 26.2 dropped that exit: `Main` returns, leaves the JVM to end when its last
non-daemon thread does, and starts `ClientShutdownWatchdog`, which dumps the threads after 15 seconds
and calls `System.exit(-8)`. The thread dump holds nothing else that is not a daemon. `stop`, and the
end of a `-Pagent` script, call `Minecraft.stop()`, the same as the title screen's Quit button, so a
person quitting a 26.2 client that has Create Fly gets the same crash report. The other nodes do not
load Create Fly.

Setting Flywheel's `workerThreads` to `0` in `run/26.2.x/config/flywheel-client.json`
(`"workerThreads": {"value": 0}`) avoids it: Flywheel then does its work on the render thread and
starts no workers, and the run ends with `BUILD SUCCESSFUL`. The file is under `run/`, which is not
committed, so each checkout sets it once. Delete this entry once a Create Fly build for 26.2 stops its
workers or makes them daemon threads. To check, run any client script on 26.2.x with `workerThreads`
back at `-1`.

## Rules

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
