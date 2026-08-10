# FAQ

## Do players need the mod to join?

Yes. The bar is drawn by the client, so a vanilla client sees nothing while the server keeps
draining thirst in the background. Put the jar in the pack.

## Why can I not sprint?

Thirst is at 6 or below. Drink something, or turn
[preventSprintingWhenThirsty](/docs/configuration#preventsprintingwhenthirsty) off.

## Why is my health not coming back?

The same reason. Natural healing waits until thirst is nearly full, and the food it would have cost
you is refunded so you do not starve while you look for water.

## I changed the config and nothing happened

If you edited the file by hand, restart the game or the server. If you used the Mod Menu screen
while connected to someone else's server, only the four HUD settings applied, because the rest come
from the server.

## Can I turn thirst off for one player?

Yes, with [/thirst enable](/docs/commands#thirst-enable). It is saved with that player, so it
survives a relog.

## A drink from another mod does nothing

Its id is not in the lists. Add it to `drinks` in the config file, or switch on
[keyword matching](/docs/configuration#enablekeywordmatching) and let the mod guess.

## Is Create required?

No. Nothing here needs another mod. Create Fly only adds the Sand Filter, and Mod Menu only adds the
settings button.

## Does it work on Peaceful?

Thirst refills by itself there unless you turn
[thirstDepletionInPeaceful](/docs/configuration#thirstdepletioninpeaceful) on.

## What about the original mod's other features?

The port is not finished. [FABRIC-PORT.md](https://github.com/Nighterezi/ThirstWasTakenFabric/blob/main/FABRIC-PORT.md)
tracks what is done, what is waiting on other mods to update, and what is missing.
