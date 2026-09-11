# src/main/java — common code

Runs on both sides but **must not reference `net.minecraft.client`**. Anything client-only belongs in
`src/client/java` (see its own AGENTS.md). Gameplay here is server-authoritative: the server owns
thirst, and the client only receives it through the attachment sync.

## Where to change what

| Want to change | Go to |
|---|---|
| What an item restores | `config/ThirstConfig.defaultDrinks()` / `defaultFoods()`, read through `api/ThirstApi` |
| Drain rate, climate, damage, full-bar drinking rules, hand drinking | `data/ThirstManager` |
| The state record itself (thirst, quenched, exhaustion) | `data/ThirstData` |
| A new config key | `config/ThirstConfig` (field + `sanitize()`), then the client config screen |
| Bowls, waterskin, creative tab | `item/` |
| Anything about water cleanliness | `purity/` (has its own AGENTS.md) |
| A vanilla behaviour hook | `mixin/` (has its own AGENTS.md) |
| Loot, optional mod integrations | `compat/` (has its own AGENTS.md) |
| The thirst and quenched droplet rows on tooltips | `tooltip/ThirstTooltip` |
| `/thirst` | `command/ThirstCommands` |
| Dev-only tooling such as `/thirst benchmark` | `src/dev/java` (own AGENTS.md), gated on `ThirstWasTaken2.DEV` |

## Init order

`ThirstWasTaken2.onInitialize` is the only entry point, and the order matters:
`ThirstConfig.load()` → `ThirstData.register()` → `ThirstComponents.register()` →
`ThirstItems.register()` → `LootIntegration.register()` → events.

`ThirstItems` static fields reference `ThirstComponents`, and `WaterPurity.resolve` references
`ThirstItems`, so registration cannot be reordered without checking those class-init chains.

Events registered there, in registration order per event:

- `END_SERVER_TICK` → `ThirstManager.tick`, then `WaterInteractions.tick` (drains the deferred queue).
- `UseBlockCallback` → `ThirstManager.drinkByHand`, `WaterInteractions.emptyWaterskinOnBlock`,
  `WaterInteractions.fillWaterskinFromCauldron`, `WaterInteractions.transferCauldronPurity`. A
  handler that returns anything but `PASS` stops the rest, which is why `transferCauldronPurity`
  deliberately returns `PASS` and defers its work.
- `UseItemCallback` → `WaterInteractions.fillFromWater`.

## Invariants worth not breaking

- **One write point for player state.** `ThirstManager.set` only. `ThirstData` is a record, so mutate
  by deriving (`drink`, `addExhaustion`, `consumeExhaustion`, `withLevels`, `withEnabled`) and write
  only when the value actually changed — every write is a sync packet. `tickPlayer` accumulates into
  a local `updated` and writes once.
- **Vanilla exhaustion is buffered, not written.** `PlayerMixin` calls
  `ThirstManager.mirrorExhaustion`, which only adds the raw amount to the player's
  `data/ExhaustionTracker`. `tickPlayer` applies the total once, together with the Hunger effect
  refund, so a player who sprints, jumps and fights in the same tick still costs one packet at most.
  `addExhaustion` writes immediately and is only for one-off sources such as salt water.
- **The exhaustion modifier is cached per player for 20 ticks** on the same tracker. Reading armour
  protection builds a loot context for every enchantment on every equipped item. A dimension or
  config change recomputes it straight away; anything else (biome, armour, Fire Resistance) may lag
  by up to a second, which exhaustion accumulating over many seconds hides.
- **Per-`Item` caches, never per-call string work.** `ThirstApi.CACHE` and `WaterPurity.INFO` are
  `ConcurrentHashMap`s keyed by `Item` identity. `ThirstApi` drops its cache when
  `ThirstConfig.generation()` changes; `WaterPurity.INFO` never invalidates, so it may only hold
  facts that cannot change at runtime (registry id, not config values — those resolve to
  `PURITY_FROM_CONFIG` and are looked up per call).
- **`ThirstConfig.get()` is cheap, `sanitize()` is where clamping lives.** Compiled regex patterns are
  transient fields rebuilt by `sanitize()`; never compile a pattern on a call path.
- **Side checks.** Interaction callbacks fire on both sides. Return `InteractionResult.SUCCESS` on the
  client for the swing animation and `SUCCESS_SERVER` from the server branch;
  `ThirstManager.drinkByHand` and `WaterInteractions.fillFromWater` are the reference shape.
- **Sounds from a server-only path need `level.playSound(null, ...)`.** `Player#playSound` excludes the
  player themselves, so the drinker would hear nothing — see the comment in
  `ThirstManager.drinkByHand`.

## Divergences from upstream live as comments

Where behaviour intentionally differs from the original Forge mod, the reason sits next to the code
(`climateModifier`, `canDrinkWater`, `WaterPurity.at`). Keep that habit: a new divergence gets a comment
at the divergence, not only a note in the docs.
