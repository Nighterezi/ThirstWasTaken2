# Commands

All commands need permission level 2, the level operators and command blocks have.

## /thirst query

```
/thirst query <player>
```

Shows the player's thirst and quenched to whoever ran the command.

## /thirst set

```
/thirst set <players> <thirst> <quenched>
```

Both values are 0 to 20. Quenched cannot be higher than thirst, so `/thirst set @s 5 20` sets both
to 5.

## /thirst enable

```
/thirst enable <players> <true|false>
```

Turns thirst on or off for those players. While off, their thirst does not change and they take no
dehydration damage. The setting is saved with the player.

To turn thirst off for everyone, set the drain speeds to `0` in
[Configuration](/docs/configuration#thirstdepletionmodifier).
