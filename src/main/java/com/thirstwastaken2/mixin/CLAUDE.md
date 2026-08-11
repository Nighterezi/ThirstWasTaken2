# mixin/

Vanilla hooks. Everything the mod cannot do through a Fabric API event lands here, and nothing else.

## Rules

- Package-private, `abstract`, one target class per file, named `<Target>Mixin`.
- Every injected member is prefixed `thirst$` — methods, `@Unique` fields, constants excepted.
- **A new mixin must be added to `src/main/resources/thirstwastaken2.mixins.json`** or it silently
  does nothing. `injectors.defaultRequire` is 1, so a stale target throws at load instead of failing
  quietly — a `runServer` that starts is already proof every injection point still resolves.
- Client-only mixins would need their own `thirstwastaken2.client.mixins.json`; there are none yet, so
  if you add one, wire the file into `fabric.mod.json` as well.
- Keep the mixin thin: capture or redirect, then call into `com.thirstwastaken2.*`. Game logic does
  not belong in this package.

## What is hooked

| Mixin | Target | Purpose |
|---|---|---|
| `PlayerMixin` | `causeFoodExhaustion` (HEAD), `canSprint` (RETURN) | mirror hunger exhaustion into thirst; block sprinting at thirst ≤ 6 |
| `FoodDataMixin` | `FoodData#tick`, both `ServerPlayer#heal` call sites | dehydration halts natural regen and refunds the food cost vanilla would have charged |
| `ItemStackMixin` | `finishUsingItem` (HEAD), `addDetailsToTooltip` (TAIL) | grant hydration on consume; append waterskin, purity and droplet lines |
| `BottleItemMixin` | `BottleItem#use` | stamp sampled quality onto a bottle filled from a water block |
| `BucketItemMixin` | `BucketItem#use` | stamp sampled quality onto a bucket filled from a water block |
| `LayeredCauldronBlockMixin` | `createBlockStateDefinition` | add purity and salinity properties |

## The fragile ones

`BottleItemMixin` and `BucketItemMixin` share a two-step shape: a server-only `@Inject` at HEAD
re-raycasts the player's view (`ClipContext.Fluid.SOURCE_ONLY`) and stores the sampled quality in a
`@Unique` field, then a `@ModifyArg` stamps the resulting stack. They depend on an exact target descriptor, and the bucket one
also on `ordinal = 1` of `ItemUtils#createFilledResult` — the first call is the empty-bucket branch.
Both break on a vanilla refactor rather than misbehaving, which is the intent.

`FoodDataMixin` uses `@Redirect` with `ordinal = 0` (saturation-driven regen) and `ordinal = 1`
(hunger-driven regen). Redirecting means vanilla's `heal` is *not* called unless the mixin calls it, so
every branch must either heal or refund exhaustion — dropping both would let hunger drain for free.

`@Unique` fields on an item mixin live on the shared item singleton, not per stack. Bottle and bucket
capture therefore use `ThreadLocal`: integrated-client prediction and the server may call the same
item singleton from different threads. Values are removed at HEAD and immediately after stamping;
do not let interaction state outlive one `use` call.
