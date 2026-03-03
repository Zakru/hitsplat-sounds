# Hitsplat Sounds

Rule-based customizable hitsplat sounds for RuneLite.

## Instructions

To configure hitsplat sounds, add lines to its configuration for each sound.

The configurations should have the following format:

```
<cond1>,<cond2>,...:<filename>
```

The file name should refer to a file (.wav should work) in `.runelite/hitsplat-sounds`.


The available conditions are the following:
- `10`, `5-`, `-15`, `5-15` - Specifies a hit number filter. Ranges can define a minimum and/or maximum. Defaults to any number.
- `own`, `-own` - Specifies whether the rule only applies to your or other players' hitsplats. Leave unspecified for no filter.
- `self`, `-self` - Specifies whether the rule only applies to hitsplats on you or hitsplats not on you. Leave unspecified for no filter.
- The following conditions filter specific hitsplat types: `damage`, `block`, `heal`, `shield`, `poise`, `charge`, `poison`, `venom`, `disease`, `corruption`, `bleed`, `burn`, `doom`.
- `max` filters only max hitsplats and works only with types that use them.


Lines starting with `#` are ignored and can be used as comments.

## Examples

When I deal exactly 10 normal damage to an opponent

```
damage,own,-self,10:sound.wav
```

Play sounds with an intensity based on the damage

```
damage,own,-self,-29:light.wav
damage,own,-self,30-:heavy.wav
damage,own,-self,max:crit.wav
```
