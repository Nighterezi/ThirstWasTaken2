# tools/agent — agent client scripts

Request files for the agent queue: one JSON request per line, `//` lines are comments, and `expect` /
`checks` beside a request are what `drive.py --verify` asserts afterwards. The protocol, the queue
directories and every command are in
[src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md](../../src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md).

```bash
./gradlew ":26.3.x:runClient" -Pagent=tools/agent/gameplay/parched.jsonl -Pquickplay=ParchedAgent
```

```bash
python tools/agent/drive.py run/26.3.x/agent/client tools/agent/gameplay/parched.jsonl --verify
```

Every path is relative to the repository root. Each script's header says which nodes it runs on, the
world it needs and how to read the result; read it before running one.

## Layout

| Folder | What is in it |
|---|---|
| `drive.py` | Sends a file or standard input to a queue, waits for the answers, and `--verify`s a finished run |
| `smoke/` | Checks with no gameplay: `boot.jsonl` (a client comes up and stays up, with `-PwithoutOptional`) and `server-probe.jsonl` (a dedicated server's queue answers with nobody online) |
| `ui/` | The client's own drawing and screens: `hud-layout.jsonl` (thirst against food and air), `hud-hidden.jsonl` (F1), `hud-death-screen.jsonl`, `config-screen.jsonl` |
| `gameplay/` | The mod's own mechanics in a real client: `client-sync.jsonl`, `parched.jsonl`, `loot-and-boil.jsonl`, `waterskin-stack.jsonl`, `hanging-pot.jsonl`, `canteen.jsonl` |
| `integrations/` | One script per optional mod: `create-water.jsonl`, `createfly-waterskin.jsonl`, `farmers-delight.jsonl`, `kaleidoscope-cookery.jsonl`, `supplementaries.jsonl` |
| `integrations/sophisticated/` | Sophisticated Backpacks and Storage, one script per upgrade, and `sophisticated-pack/`, the data pack of backpack templates those scripts give out |

## Adding a script

- Put it in the folder its subject belongs to. A new optional mod gets its script in
  `integrations/`, or a folder of its own there once it has more than one.
- Start with a header: what it proves, the nodes it needs, the world (`-Pquickplay=<world>` and how to
  make it), and what to read afterwards when the file cannot say it with `expect` and `checks`.
- Reference it from the `AGENTS.md` or `docs/dev` page of the area it checks, by its full
  `tools/agent/<folder>/<name>.jsonl` path, so a move shows up in a search.
- Prefer a gametest when the check needs no client and no real time. A script is for what
  [src/gametest](../../src/gametest/java/AGENTS.md) cannot reach: rendering, screens, the real right
  click, other mods, and the client's view of synced values.
- Delete a script once a gametest covers the same thing, and remove its references with it.
