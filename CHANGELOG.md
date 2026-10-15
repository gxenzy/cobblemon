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
- Correctly grants "They're Minerals!" advancement when planting a type gem
- Shoulder mounted Pokémon now follow player body animations, improving their positioning with custom player animations and default actions.
- Shoulder mounted Pokémon now sit slightly higher with chest armor equipped for better alignment, with a configurable blacklist.
- Type Gem Blocks now have advancements to give you the recipe in the recipe book (the data for the advancements were inside the recipes somehow??)
- Some other recipes without advancements have also been corrected
- Corrected the TM Recipes for Charge Beam and Payday
- Type Gem blocks now properly generate clusters off of the block.
- Starter Screen Categories will no longer stop rendering if they are bigger than the viewport, and you are not viewing top or bottom edges
- Random Starter option now properly handled in height calculation, so now it won't run off into other categories or be un-accessible due to scroll limits if it causes a new row
- Mochi and Feathers no longer drop glass bottles on use
- Fixed a rare set of concurrency bugs related to reloading packs on NeoForge; fixes a ModernFix issue
- Removed unnecessary conditions for Mimikyu's standing pose. Hopefully fixes the rare instance of it using its shoulder animations during battle in multiplayer scenarios.
- Fixed Rockruff line not blinking
- Fixed Flygon sometimes using its running animation instead of walking
- Fixed Bramblin always sleeping outside of battle
- Fixed Dragonair using its battle idle as a normal idle
- Fixed Lycanroc's head movement
- Added head movement to Archaludon
- Fixed Duraludon line not having any sleep animations
- Fixed Diglett line not having their faint animations
- Fixed Kingambit moving its legs while walking
- Fixed Alolan Diglett's cry not playing
- Fixed shiny alpha Snorlax using the non-shiny texture
- Various Structure fixes
- Fixed shiny Pokémon under certain circumstances forgetting taught TMs
- Fixed certain Pokémon sounds sometimes being doubled when played at specific times in their animations
- Fixed ball capture beam origin coming from behind the ball.
- Fixed Safari Ball catch rate outside of battle
- Fixed alpha eye (and other) particles not respecting client particle density setting in video options
- Fixed TM-Machine UI not closing when player is moving away from it
- Fixed config category for alpha/shiny chance config fields for fossil machine and honey log slathering to Spawning
- Fixed crashes for addon species with non-Cobblemon namespaces, in evolution, fishing bait and Pokédex lookups.
- Bees now grow Apricorns and the Galarica Nut Bush, which were in the bee growables tag but never handled, and correctly spend their nectar when they do
- Saccharine Leaves can now be bone mealed
- Fixed party overlay not hiding when player opens the pokedex scanner 
- Added compat for AppleSkin

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
