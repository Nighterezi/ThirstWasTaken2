# Manual testing

What the gametests cannot see, written as a checklist to run by hand before a release. Everything
else is automated: see [src/gametest/java/AGENTS.md](../../src/gametest/java/AGENTS.md) for what the
gametests cover, and run them first.

```bash
./gradlew ":26.2.x:runGametest"
```

A gametest runs on a headless dedicated server with mock players. That rules out four kinds of
check, and every item below belongs to one of them:

| Why it is manual | What falls under it |
|---|---|
| **No client.** A gametest never opens a window, never renders, never receives a packet. | the HUD, sprites, tooltips as drawn, the config screen, anything synced to a client |
| **Mock players cannot be hurt.** They report creative mode, and vanilla refuses to damage them. | dehydration damage, the death message, armour against dehydration |
| **One world, one biome, one dimension.** Tests run in a flat overworld patch. | the Nether drain, climate, `nether_drink`, loot found in real structures |
| **Nothing on a real timer.** A test body runs inside one tick. | drain over minutes of play, peaceful refill over time, rain filling a cauldron in the open |

## How to run a pass

1. `./gradlew ":<node>:runClient"`, where `<node>` is `26.2.x`, `26.1.x`, `1.21.11` or `1.21.1`, or
   one of those with a `-neoforge` suffix. The dev client already has AppleSkin, Cloth Config and Jade,
   and Mod Menu on the Fabric nodes.
2. Create a new **survival** world on **Normal**, cheats on. Keep the world per version; saves do
   not move between versions.
3. Work down the general checklist, then the section for that version.
4. Note the version, the date and anything that failed in the release PR.

Useful commands while testing:

```
/thirst set @s <thirst> <quenched>
/thirst enable @s false
/effect give @s minecraft:fire_resistance
/give @s thirstwastaken2:waterskin
/time set night
/weather rain
```

## General checklist (every version)

### HUD

- [x] The thirst bar sits above the hunger bar on the right, ten droplets wide, the same width as hunger.
- [x] `/thirst set @s 13 0`: six and a half droplets, the half droplet on the left end.
- [x] With quenched at 0, sprinting makes the next droplet drain through the quarter and three-quarter
      frames before a point is spent, and the bar shakes the way hunger does at zero saturation.
- [x] `/thirst set @s 20 20`: the quenched outline is drawn over every droplet, cyan by default.
- [x] Mod Menu → ThirstWasTaken2 → HUD & AppleSkin: Quenched Outline reads "Quenched Outline: Diamond"
      (the name once) and cycles Diamond, Ice, Gold, AppleSkin and Off. The preview and the real bar
      change at once. Off removes the outline from the bar; the tooltip row falls back to the plain
      blue outline.
- [x] The bar is hidden with F1, in creative and spectator, and while riding a horse, pig or strider
      (a living mount's hearts take its place). It stays while in a boat or minecart.
- [x] Underwater, the air bubbles sit **above** the thirst bar, not on top of it.
- [x] Mod Menu → ThirstWasTaken2 → HUD & AppleSkin: the X and Y offsets move the bar live.
- [x] With AppleSkin's exhaustion underlay on, a translucent strip grows under the thirst bar while
      sprinting; turning the AppleSkin option off removes it. Nothing drawn after it (air bubbles,
      the hotbar) is left tinted.
- [x] `/thirst enable @s false` hides the bar; `true` brings it back.

### Sync to the client

The server owns thirst. These are the checks that the client is told.

- [x] `/thirst set @s 6 0` updates the bar immediately, and sprinting is refused at 6. Hold sprint and
      walk: at 7 the player runs, at 6 they walk. A gametest cannot see this, because the client decides
      sprinting.
- [x] Quit to the title screen and rejoin: the bar shows the value it had.
- [x] Die and respawn: the bar is full again.
- [x] Go through a Nether portal and back: the bar is still correct on both sides.
- [x] Open the world to LAN or use `runServer` with a second client: each player sees only their own bar,
      and it is right for each.

### Tooltips

- [x] A water bottle, a filled bowl and a filled waterskin show a coloured grade line (Dirty, Murky,
      Clean, Pure) and two rows of droplet glyphs, not boxes or letters.
- [x] Turning Tooltip Droplets off removes both droplet rows and keeps the grade line.
- [x] A sea-water bottle shows a single "Salty" line and no droplet rows.
- [x] A waterskin says how many drinks it holds, or that it is empty. A clay bowl says it has to be fired.
- [x] Milk, honey and an apple show droplet rows; stone shows nothing.
- [x] The lines look the same in JEI, EMI or REI if one of them is installed.

### Jade

- [x] Looking at river water, sea water, a waterlogged block and a cauldron filled by rain shows the
      grade (or Salty) under the block name, in the grade's colour. A bottle filled there gets the
      same grade.
- [x] Placing mud next to the water being looked at lowers the grade within half a second.
- [x] Turning Water Purity off in Jade's plugin settings removes the line.

### Sprites and sounds

- [x] A filled bowl's water colour changes with its grade: four fresh colours and a sea colour.
- [x] A waterskin's sprite shows 0, 1, 2 or 3 drinks, and its bar is coloured by grade.
- [x] Drinking a bowl or a waterskin plays the drinking animation and sound, and takes about as long
      as a potion. The bowl leaves an empty terracotta bowl; the waterskin stays in hand.
- [x] Crouch with an empty hand and use water at less than full thirst: the drinking sound is heard
      by the drinker.
- [x] Crouch with an empty hand and use water whose floor is out of reach (deep water, a waterfall):
      it is drunk all the same, once per click, and an item in the other hand is still used.
- [x] Scooping with a bowl plays the bucket sound, with a waterskin the bottle sound.

### Config screen

- [x] Mod Menu → ThirstWasTaken2 → Configure opens the root screen: the animated preview (tooltip on
      the left, thirst bar above the food bar on the right), five page buttons with hover text, the
      server note, and Cancel and Done.
- [x] Every page opens, every slider and toggle has a tooltip, and no slider can go outside its range.
- [x] Reset to Defaults puts that page back and leaves the other pages alone.
- [x] Item Values → "Open thirstwastaken2.json" opens the file.
- [x] Change a value, Done, relaunch: it is kept. Change a value, Cancel: it is back, in the HUD too.
- [x] Resize the window with a page open and after returning to the root screen: nothing is drawn
      twice and the preview stays centred.

### Damage and the world

- [x] `/thirst set @s 0 0` on Normal: half a heart of damage every two seconds, down to death; the death
      message names dehydration. On Easy it stops at five hearts. Armour does not reduce it.
- [x] Natural regeneration stops below full thirst and resumes once it is full again.
- [x] Standing still in a desert drains faster than in a snowy biome; in the Nether faster again.
- [x] Drinking in the Nether earns "nether_drink".
- [x] On Peaceful, thirst does not drop and slowly refills.
- [x] Rain on an empty cauldron in the open fills it; drawing a bottle from it gives water of the
      configured rain grade. A dripstone dripping into a cauldron gives pure water.
- [x] A dungeon, mineshaft or shipwreck supply chest sometimes holds water bottles; a piglin sometimes
      barters one. Those bottles boil in a furnace.
- [x] The advancement tab has its icon and terracotta background, and the recipe book lists the
      purification recipes once a bottle, bowl or bucket is held.

### Without the optional mods

- [x] Remove Mod Menu, AppleSkin and Cloth Config from the run: the game loads and the bar draws
      without the exhaustion strip or the quenched outline, and tooltips have no droplet rows.
- [x] A dedicated server (`runServer`) starts and a client joins it without a crash on either side.
- [x] A dedicated server with Jade in its mods folder starts without a crash. Jade loads the plugin
      on the server too. `runServer` with Jade copied into `run/<node>/mods` is enough on both loaders:
      the Fabric dev server has the client classes on its classpath, as the released jar does.
      Checked on 26.2 on 2026-09-15.

## Per version

What differs between the nodes, and so what has to be looked at on that node in particular. Why
each one differs is in [VERSION-DIFFERENCES.md](VERSION-DIFFERENCES.md).

### 26.2

- [x] F1 hides the bar. 26.2 moved the "HUD hidden" state into the HUD object itself, so this is a
      separate code path from every other version.
- [x] The config screen's "Open thirstwastaken2.json" button is one full-width row, and Done and
      Cancel close the screen (26.2 opens screens through a different call).
- [x] A sea-water bottle is drawn in the sea colour, and a sea-water bucket with its recoloured water.

### 26.1.x

- [x] F1 hides the bar (read from the options, unlike 26.2).
- [x] The config file button is a row of its own, even though 26.1 has no full-width row call.
- [x] Sea-water bottle and bucket sprites, as on 26.2.

### 1.21.11

- [x] F1 hides the bar, as on 26.1.
- [x] The config file button, as on 26.1.
- [x] Sea-water bottle and bucket sprites, as on 26.2.
- [x] The droplet glyphs have no shadow.

### 1.21.1

The version with the most of its own code, and the only one where several things are known to look
different on purpose. Check all of these on every release that ships a 1.21.1 jar.

- [x] **Sprinting is gated by a client mixin** (`LocalPlayerMixin`), because 1.21.1 keeps the food
      check a sprint needs on LocalPlayer. At thirst 6 holding sprint must walk, at 7 it must run.
- [x] **Sync first.** Fabric API's attachment sync on 1.21.1 is a backport the gametests cannot reach.
      Run the whole "Sync to the client" section on this version before anything else.
- [x] **The HUD is drawn by a mixin**, not by Fabric API's HUD registry, which 1.21.1 does not have.
      Check that the bar is in the same place as on 26.2, that the air bubbles move up above it, that
      it disappears with the hearts in creative, and that a living mount's health replaces it.
- [x] **Tinting uses render state.** With AppleSkin's underlay on, the air bubbles and the hotbar
      drawn after the strip must not come out faded.
- [x] **Drinking is implemented by the item itself**, not by the consumable component. The animation
      must be drinking, not eating; the sound must play; a creative player keeps the waterskin's
      servings and the bowl.
- [x] **Known, by design:** the tooltip droplets are drawn with a shadow (styles cannot turn it off
      before 1.21.4). Check that they are still legible, not that the shadow is gone.
- [x] **Known, by design:** a sea-water bottle and bucket look like ordinary water (there is no
      item-model component before 1.21.2). Check that the tooltip says "Salty" and that the bowl
      still changes to the sea colour.
- [x] **The config screen's headings are text rows**, not vanilla headings, which 1.21.1 lacks. Check
      that they are centred and readable and that the list still scrolls.
- [x] The config preview's food icons and droplet outlines have transparent corners, not black ones
      (1.21.1 needs blending turned on around each draw).
- [x] The water cauldron's name is "Water Cauldron" in the F3 target and in `/give` suggestions. On
      1.21.1 the mod has to identify the cauldron before its name exists, and a mistake there would
      rename it.
- [x] The advancement tab background is terracotta, not a missing texture.
- [x] The jar loads on Minecraft 1.21 as well as 1.21.1.

### 26.2 NeoForge

A subset of the general checklist, for what the loader changes: the HUD layer, the config screen
entry, the client mixins, the event hooks and the attachment. Everything else is the same code as
26.2 Fabric and the same 123 gametests pass on this node. Run it on `26.2.x-neoforge`, which has no
Mod Menu and no Farmer's Delight or Create Fly.

- [x] The thirst bar sits above the hunger bar, and underwater the air bubbles sit above the thirst
      bar. NeoForge stacks the right-hand bars by a shared height rather than Fabric's registry.
- [x] In creative the thirst bar disappears with the hearts and hunger, and a living mount's health
      replaces it, as on Fabric.
- [x] With AppleSkin, the quenched outline and the exhaustion strip draw on the thirst bar, and
      turning AppleSkin's own exhaustion underlay off removes the strip. Setting
      `showFoodExhaustionHudUnderlay = false` in `config/appleskin-client.toml` while in game is
      enough: NeoForge reloads the file and the strip goes from both bars at once.
- [x] Item tooltips show the purity line and the thirst and quenched droplet rows. The rows come
      after NeoForge's own tooltip lines here, so check they are still together and in order.
- [x] Mods, ThirstWasTaken2, Config opens the mod's config screen, and Done returns to the mods list.
- [x] Jade shows the grade when looking at river water, sea water and a water cauldron.
- [x] Drinking by hand works, from water under the crosshair and from water the crosshair misses
      (`MinecraftMixin`, now in the shared client mixin config). F3 showing no Targeted Block is the
      way to be sure the crosshair missed.
- [x] Filling a bottle from a cauldron and scooping with a bowl keep working after a second right
      click on the same block (the use events are cancelled with a result, not just stopped).
- [x] `./gradlew ":26.2.x-neoforge:runServer"` starts without an error, once as it is and once with
      Jade in `run/26.2.x-neoforge/mods`. Jade loads the mod's plugin on the server too.
- [x] Set thirst to 8 with `/thirst set @s 8 0`, die, respawn: thirst is full again, as on Fabric.
      Then leave and rejoin the world with thirst not full: the value survives the save.
- [x] **Known, by design:** a world from the Fabric jar opened with the NeoForge jar starts every
      player at full thirst. The two loaders save the value under different keys: both player files
      hold `thirstwastaken2:player_data`, under `fabric:attachments` on one and
      `neoforge:attachments` on the other.

Checked on 2026-09-15 with computer use, except the last item, read from the save files. Sneaking has
to be held by a real key press for hand drinking: a Shift modifier on a single click is released
before the server sees the player crouch.

### 1.21.1 NeoForge

The 26.2 NeoForge list, on `1.21.1-neoforge`, plus what is only true of 1.21.1 on this loader. Not yet
checked by hand. Every 1.21.1 item above that is not about Fabric API applies here too: drinking by
the item itself, the shadowed tooltip droplets, the sea-water sprites, the config screen headings,
the water cauldron's name and the advancement background.

- [ ] Every item of the 26.2 NeoForge section above. Checked on 2026-09-15 with computer use, all but
      two: the bar above hunger and the bubbles above it, creative and a ridden horse hiding it, the
      Diamond quenched outline, tooltips (grade line, droplet rows after NeoForge's lines, Salty,
      waterskin servings), Mods → Config → Done, Jade on still water, a water cauldron and sea water,
      drinking by hand from targeted water and from water whose floor is out of reach, bottles and
      bowls filling on a second use of the same block, `runServer` starting with Jade in `mods`, and
      thirst full after death. **Open:** the exhaustion strip could not be told apart from the
      drained droplet in an F2 capture with AppleSkin's underlay on or off, and the Fabric-to-NeoForge
      world item was not tried.
- [ ] **Sync first.** NeoForge 21.1 only syncs thirst to a connection that negotiated its attachment
      channel, which a NeoForge client does. Run the whole "Sync to the client" section, including a
      dedicated server with a second client. Checked on a dedicated server with one NeoForge client:
      joining raised no sync error, `/thirst set` updated the bar at once, the value held through the
      Nether and back and through a disconnect and rejoin, and a singleplayer world kept it through a
      save and reload. **Open:** a second client, which two dev clients named `Dev` cannot do.
- [x] **Sprinting is gated by `LocalPlayerMixin`**, now in the shared client mixin config: at thirst 6
      holding sprint walks, at 7 it runs. Measured over 3 s of Ctrl+W on a flat track: 16.8 blocks at 7,
      13.2 at 6.
- [x] The thirst bar and the air bubbles stack by `Gui.rightHeight`, not `Hud`: same place as on 26.2,
      bubbles above the bar underwater.
- [x] The purification recipes show in the recipe book and a furnace boils a looted bottle. The recipe
      JSON is translated differently on 1.21.1 (`type` and `items`), and only the gametests' furnace
      check has seen it. A dirty bottle came out of the furnace graded clean.
- [ ] **Known, by design:** a 1.21.1 NeoForge world opened on a later NeoForge version starts every
      player at full thirst; 21.1 saves the attachment without the `value` field later versions use.
      Not tried.

### 1.21.11 and 26.1.x NeoForge

The 26.2 NeoForge list, on `1.21.11-neoforge` and on `26.1.x-neoforge`, plus the items of the Fabric
section for the same version. Not yet checked by hand.

- [ ] Every item of the 26.2 NeoForge section above, on each node.
- [ ] **Sync first on 1.21.11.** Like 21.1, NeoForge 21.11 only syncs thirst to a connection that
      negotiated the attachment channel. Run the whole "Sync to the client" section there.
- [ ] The thirst bar and the air bubbles stack by `Gui.rightHeight` on both: same place as on 26.2,
      bubbles above the bar underwater.
- [ ] F1 hides the bar on both (read from the options, as on the Fabric nodes of these versions).

## When this file changes

Anything a gametest can reach belongs in a gametest, not here. When a check moves into one, delete it
from this list. When a new version node is added, give it a section, starting from what its row in
`platform/AGENTS.md` says it lacks.
