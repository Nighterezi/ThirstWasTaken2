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

A pass can be driven with computer use, but checks with an exact answer should use the agent client.
`tools/agent/hud-layout.jsonl` compares the real thirst, food and air draw rectangles,
`hud-hidden.jsonl` proves the bar stops receiving draw calls while the HUD is hidden, and
`client-sync.jsonl` asserts the client-owned values. The `manual-testing` skill in `.claude/skills`
remains useful for the genuinely qualitative parts such as whether text is comfortable to read.

Add `-Pdriven` when the agent client is doing the work and you want the machine back:

```bash
./gradlew ":26.1.x-neoforge:runManualA" -Pdriven
```

The client then opens maximised and never takes the mouse pointer, so it can sit there being driven
while you work in another window; clicking it to look at the bar no longer traps the cursor inside
the frame. Leave it off for the qualitative items below, which are played by hand or by computer use:
without the grab there is no mouse look and no click reaches the world.
[src/dev/java/AGENTS.md](../../src/dev/java/AGENTS.md) has the rest.

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

**Automated.** The server owns thirst, and every check that the client is told now answers with a
number out of the agent client rather than with a pair of eyes on a screenshot: the client reports the
value it holds, and `LocalPlayer.isSprinting()` reports the sprint gate. Run it with
[tools/agent/client-sync.jsonl](../../tools/agent/client-sync.jsonl), and the two-client item the way
[src/dev/java/AGENTS.md](../../src/dev/java/AGENTS.md) describes; that file also says what each answer
has to be. Last run on `1.21.1-neoforge` and `26.1.x-neoforge` on 2026-09-16, all five green on both.

The five checks it replaced were: `/thirst set` reaching the bar and sprinting refused at 6 but not at
7; quitting to the title screen and rejoining; dying and respawning to a full bar; a round trip
through the Nether; and two clients each seeing their own bar and no one else's. Do not put them back
here. What stays manual is only what a number cannot settle — whether the bar, the tooltips and the
config screen *read* well, which is the sections below.

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
      Checked on 26.2 on 2026-09-15 and on 1.21.11 NeoForge on 2026-09-16.

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
26.2 Fabric and the same 124 gametests pass on this node. Run it on `26.2.x-neoforge`, which has no
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
      player at full thirst. This is now a GameTest: it writes the real player tag, moves
      `thirstwastaken2:player_data` between `fabric:attachments` and `neoforge:attachments`, loads it
      through the real player load path and requires `ThirstData.full()`.

Checked on 2026-09-15 with computer use; the save-key item became an automated GameTest on
2026-09-16. Sneaking has to be held by a real key press for hand drinking: a Shift modifier on a
single click is released before the server sees the player crouch.

### 1.21.1 NeoForge

The 26.2 NeoForge list, on `1.21.1-neoforge`, plus what is only true of 1.21.1 on this loader. Every
1.21.1 item above that is not about Fabric API applies here too: drinking by
the item itself, the shadowed tooltip droplets, the sea-water sprites, the config screen headings,
the water cauldron's name and the advancement background.

- [x] Every item of the 26.2 NeoForge section above. Checked on 2026-09-15 with computer use, then
      completed with the agent client on 2026-09-16. The numeric HUD pass measured an 81 px thirst
      row with the same right edge as the 81 px food row, and the air row ending above the thirst
      row. AppleSkin's live config reload changed `exhaustionStrip` from true to false and back.
      The earlier pass covered creative and a ridden horse hiding it, the
      Diamond quenched outline, tooltips (grade line, droplet rows after NeoForge's lines, Salty,
      waterskin servings), Mods → Config → Done, Jade on still water, a water cauldron and sea water,
      drinking by hand from targeted water and from water whose floor is out of reach, bottles and
      bowls filling on a second use of the same block, `runServer` starting with Jade in `mods`, and
      thirst full after death. The Fabric-to-NeoForge save-key behaviour is now a GameTest rather
      than a screenshot/manual check.
- [x] **Sync first.** NeoForge 21.1 only syncs thirst to a connection that negotiated its attachment
      channel, which a NeoForge client does. Run the automated "Sync to the client" section on this
      node, with `runServer`, `runManualA` and `runManualB`. Checked by hand on a dedicated server with
      two NeoForge clients on 2026-09-16: joining raised no sync error, `/thirst set` updated the bar
      at once, the value held through death, the Nether and a disconnect/rejoin, and A and B received
      two different values before and after swapping them without either client drawing the other's.
- [x] **Sprinting is gated by `LocalPlayerMixin`**, now in the shared client mixin config: at thirst 6
      holding sprint walks, at 7 it runs. Measured over 3 s of Ctrl+W on a flat track: 16.8 blocks at 7,
      13.2 at 6.
- [x] The thirst bar and the air bubbles stack by `Gui.rightHeight`, not `Hud`: same place as on 26.2,
      bubbles above the bar underwater.
- [x] The purification recipes show in the recipe book and a furnace boils a looted bottle. The recipe
      JSON is translated differently on 1.21.1 (`type` and `items`), and only the gametests' furnace
      check has seen it. A dirty bottle came out of the furnace graded clean.
- [x] **Known, by design:** a 1.21.1 NeoForge world opened on a later NeoForge version starts every
      player at full thirst; 21.1 saves the attachment without the `value` field later versions use.
      This is now a GameTest on both generations: it changes the real saved attachment to the other
      generation's wrapper shape, loads it and requires `ThirstData.full()`.

### 1.21.11 and 26.1.x NeoForge

The 26.2 NeoForge list, on `1.21.11-neoforge` and on `26.1.x-neoforge`, plus the items of the Fabric
section for the same version. The qualitative checks ran on `1.21.11-neoforge` on 2026-09-15/16;
the remaining exact client seams ran numerically on `26.1.x-neoforge` on 2026-09-16.

- [x] Every item of the 26.2 NeoForge section above, on each node. On 1.21.11, the qualitative pass
      was completed on 2026-09-15/16. On 26.1.x, the shared qualitative pieces are already covered
      by the general, 26.1 Fabric and 26.2 NeoForge passes; its distinct loader/version seams were
      completed numerically by the agent client on 2026-09-16. On 1.21.11, checked:
      the bar above hunger and the bubbles above it, creative and a ridden horse hiding it, the
      Diamond quenched outline, the exhaustion strip after sprinting and its removal by
      `showFoodExhaustionHudUnderlay = false`, tooltips (grade line, droplet rows after NeoForge's
      lines, Salty, waterskin servings, apple), Mods → Config → Done, Jade on still water, sea water
      (a waterlogged kelp) and a water cauldron, drinking by hand from targeted water and from water
      whose floor is out of reach (F3 showing no Targeted Block), bottles from a cauldron and bowls
      from water filling on a second use, and thirst full after death. `runServer` with Jade in
      `mods` checked from the console on 2026-09-16: the server reached "Done", loaded Jade
      21.1.7+neoforge out of `mods`, and loaded `JadeIntegration` as a Jade plugin there, with no
      error in the log. Leaving and rejoining with thirst not full, the last item open on 1.21.11, was
      answered by the agent client on 2026-09-16 and is now part of the automated "Sync to the client"
      section. On 26.1.x, the dedicated server loaded Jade and its plugin, a real client joined, and
      the HUD, hidden-HUD and sync assertions below passed.
- [x] **Sync first on 1.21.11.** Like 21.1, NeoForge 21.11 only syncs thirst to a connection that
      negotiated the attachment channel. The whole "Sync to the client" section ran there on
      2026-09-16 through the agent client, on `runServer` with `runManualA` and `runManualB`: the
      sprint gate opened at 7 and closed at 6, the value held through death, the Nether and a rejoin,
      and with the two testers standing together each drew its own bar and neither drew the other's.
      The same five checks passed on 26.1.x on 2026-09-16.
- [x] The thirst bar and the air bubbles stack by `Gui.rightHeight` on both: same place as on 26.2,
      bubbles above the bar underwater. On 26.1.x the agent measured thirst and food as 81 px wide
      with the same right edge; underwater the air row ended at y=248 and thirst began at y=249.
- [x] F1 hides the bar on both (read from the options, as on the Fabric nodes of these versions).
      On 26.1.x the agent toggled that same vanilla state, observed `hudHidden=true` and
      `barShouldRender=false`, and proved the last thirst draw aged past 1000 ms before drawing again.

## When this file changes

Anything a gametest can reach belongs in a gametest, not here. When a check moves into one, delete it
from this list. When a new version node is added, give it a section, starting from what its row in
`platform/AGENTS.md` says it lacks.
