# Changelog
## [1.8.3 (MONTH xth, 2026)](#1-8-3)

### Additions

### Pokémon Added
#### Generation 4

#### Generation 5

#### Generation 6

#### Generation 7

#### Generation 8

#### Generation 9

### Model updates for the following Pokémon

### Animation updates for the following Pokémon

### Changes
- Changed the Misc icon in the Campfire Pot Recipe Book to Health Candy
- Moved EXP Candies from the Medicine recipe book category to the Misc category

### Fixes
- Fixed datapacks with item interaction evolutions that don't specify an item crashing the world on load.
- Fixed the Pokédex evolution tree crashing when a datapack's dex entry is missing an evolution's form.
- Fixed the Pokédex evolution tree crashing with a stack overflow on evolutions that loop back on themselves.
- Fixed the Pokédex evolution tree crashing when looking at a dex that is not part of the "National Dex" aggregate.
- Fixed evolution trees not building properly when a pokemon has several pokedex entries for different regions.
- Fixed form-changing evolutions sometimes resolving to the source form in the Pokédex evolution tree.
- Broken datapack evolutions are now logged and skipped in the Pokédex evolution tree instead of crashing.
- Fixed friendship booster items not being recognised as such
- Fixed conflicts with other mods that would make use of recipe books in certain ways (Farmer's Delight Cooking Pot)
- Fixed Pokémon battles stalling when a sidemod cancels the `THROWN_POKEBALL_HIT` hit event
- Fixed choice feature tab completion in `spawnpokemon/pokegive/pokeedit` commands
- Fixed MoLang that's targeting players intermittedly failing when they have died, needing them to relog
- Fixed all pots of mod on NeoForge

### Developer
- Bumped NeoForge to `21.1.195` to get access to MixinExtras `0.5` and the expressions annotation
- New Platform Event `PLAYER_RESPAWN` which is fired when a player respawns (either through death or returning from the end)
- Platform Events `SERVER_PLAYER_TICK_PRE` and `SERVER_PLAYER_TICK_POST` no longer fire when the player is dead
  - it is now tied much closer to the actual entity rather than the player (or their connection)

### Molang & Datapacks
- Fixed `q.pokemon.nature` returning class instance instead of proper string value (`cobblemon:adamant`)
- Fixed `q.entity.is_looking_at` result being always `1` for whatever entity passed as target

### Localization
- Updated translations for:
