# FAQ

## Do players need the mod to join?

Yes. A client without it does not see the bar, while the server still drains thirst.

## Why can I not sprint?

Thirst is at 6 or below. Drink something, or turn off
[preventSprintingWhenThirsty](/docs/configuration#preventsprintingwhenthirsty).

## Why is my health not coming back?

Natural healing waits until thirst is nearly full. The food it would have used is not spent.

## I changed the config and nothing happened

- A file edited by hand is read on the next start.
- On someone else's server, only the HUD and AppleSkin settings apply. The rest comes from the
  server.
- The settings screen only saves on **Done**.

## Can thirst be turned off for one player?

Yes, with [/thirst enable](/docs/commands#thirst-enable). It is saved with the player.

## A drink from another mod does nothing

Add it to `drinks` in the config file, or turn on
[keyword matching](/docs/configuration#enablekeywordmatching).

## Is Create required?

No. With [Create](/docs/features/create) installed, the Sand Filter is added.

## Does it work on Peaceful?

Thirst refills on its own there, unless
[thirstDepletionInPeaceful](/docs/configuration#thirstdepletioninpeaceful) is on.
