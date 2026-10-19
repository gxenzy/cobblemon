import json
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
SPECIES_DIRECTORY = (
    REPOSITORY_ROOT
    / "common"
    / "src"
    / "main"
    / "resources"
    / "data"
    / "cobblemon"
    / "species"
)
LOOT_TABLE_DIRECTORY = (
    REPOSITORY_ROOT
    / "common"
    / "src"
    / "main"
    / "resources"
    / "data"
    / "cobblemon"
    / "loot_table"
    / "pokemon"
)


def convert_drops_to_loot_table(drops, species_name):
    entries = []
    for drop_entry in drops["entries"]:
        loot_entry = {
            "type": "minecraft:item",
            "name": drop_entry["item"],
        }

        percentage = drop_entry.get("percentage", 100)
        if not isinstance(percentage, (int, float)) or isinstance(percentage, bool):
            raise ValueError(f"Invalid drop percentage for {drop_entry['item']}: {percentage}")
        if not 0 <= percentage <= 100:
            raise ValueError(f"Drop percentage must be between 0 and 100: {percentage}")
        if percentage < 100:
            loot_entry["conditions"] = [
                {
                    "condition": "minecraft:random_chance",
                    "chance": percentage / 100,
                }
            ]

        quantity_range = drop_entry.get("quantityRange")
        if quantity_range is not None:
            if isinstance(quantity_range, int) and not isinstance(quantity_range, bool):
                minimum = maximum = quantity_range
            elif isinstance(quantity_range, str):
                minimum, separator, maximum = quantity_range.partition("-")
                if not separator:
                    minimum = maximum = quantity_range
                minimum = int(minimum)
                maximum = int(maximum)
            else:
                raise ValueError(f"Invalid quantity range for {drop_entry['item']}: {quantity_range}")
            if minimum < 0 or maximum < minimum:
                raise ValueError(f"Invalid quantity range for {drop_entry['item']}: {quantity_range}")
            loot_entry["functions"] = [
                {
                    "function": "set_count",
                    "count": {
                        "min": int(minimum),
                        "max": int(maximum),
                    },
                }
            ]

        entries.append(loot_entry)

    return {
        "type": "minecraft:generic",
        "pools": [
            {
                "rolls": drops["amount"],
                "entries": entries,
            }
        ],
        "random_sequence": f"cobblemon:pokemon/{species_name}",
    }


def main():
    LOOT_TABLE_DIRECTORY.mkdir(parents=True, exist_ok=True)

    generated = 0
    skipped = 0
    for species_file in SPECIES_DIRECTORY.rglob("*.json"):
        with species_file.open(encoding="utf-8") as file:
            species = json.load(file)

        drops = species.get("drops")
        if drops is None:
            print(f"Skipping {species_file.name}: no drops section")
            skipped += 1
            continue

        loot_table = convert_drops_to_loot_table(drops, species_file.stem)
        loot_table_file = LOOT_TABLE_DIRECTORY / species_file.name
        with loot_table_file.open("w", encoding="utf-8", newline="\n") as file:
            json.dump(loot_table, file, ensure_ascii=False, indent=2)
            file.write("\n")
        generated += 1

    print(f"Generated {generated} loot tables; skipped {skipped} species without drops.")
    input("Press Enter to exit...")

if __name__ == "__main__":
    main()
