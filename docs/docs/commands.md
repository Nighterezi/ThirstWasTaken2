# Commands

Everything lives under `/thirst`. All three subcommands need permission level 2, the level operators
and command blocks normally have, so ordinary players cannot use them.

## /thirst query

```
/thirst query <player>
```

Prints that player's thirst and quenched values to whoever ran the command. Nobody else is told.

## /thirst set

```
/thirst set <players> <thirst> <quenched>
```

Both numbers are 0 to 20. `players` takes a selector, so `@a` works.

Quenched can never sit above thirst, so `/thirst set @s 5 20` leaves the player at 5 and 5. The
result is broadcast to operators, the way vanilla commands are.

## /thirst enable

```
/thirst enable <players> <true|false>
```

Turns the whole system off for those players. While disabled they stop losing thirst, stop taking
dehydration damage, and keep whatever the bar showed when you flipped it.

This is per player and is saved with them, which makes it the right tool for a spectator, a builder
on a survival server, or an admin who does not want the bar.

To turn thirst off for everyone instead, leave it enabled and set the depletion speeds to `0` in
[Configuration](/docs/configuration#thirstdepletionmodifier).
