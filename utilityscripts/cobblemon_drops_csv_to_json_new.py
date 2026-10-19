import json
import re
import time
from io import StringIO
from pathlib import Path

import pandas as pd
import requests
from tqdm import tqdm

from scriptutils import (
    printCobblemonHeader,
    print_cobblemon_script_description,
    print_cobblemon_script_footer,
    print_warning,
    sanitize_pokemon,
)


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
RESOURCE_LOCATION_PATTERN = re.compile(r"^[a-z0-9_.-]+:[a-z0-9/._-]+$")
QUANTITY_RANGE_PATTERN = re.compile(r"^(\d+)-(\d+)$")
FORM_ENTRY_MAPPING = {
    "Alolan": "Alola",
    "Galarian": "Galar",
    "Hisuian": "Hisui",
    "Paldean": "Paldea",
}


def main(dex_range=None):
    printCobblemonHeader()
    print_cobblemon_script_description(
        "Cobblemon Drops Generator",
        "Generates Pokémon loot tables from the Drops column in the Google Spreadsheet.",
    )

    drops_df = get_drops_df(dex_range)
    species_files = get_species_file_lookup()
    LOOT_TABLE_DIRECTORY.mkdir(parents=True, exist_ok=True)

    generated = []
    rows_marked_manual = []
    rows_without_drops = []
    rows_removed = []
    malformed_rows = []
    species_files_updated = set()
    for _, row in tqdm(drops_df.iterrows(), total=len(drops_df), desc="Generating loot tables"):
        pokemon_label = row["Pokémon"]
        drops_string = row["Drops"]
        manual_status = row.get("Manual")
        if isinstance(manual_status, str) and manual_status.strip().casefold() == "manual":
            rows_marked_manual.append(pokemon_label)
            continue

        no_drops = pd.isna(drops_string) or not str(drops_string).strip()
        removed = not no_drops and "REMOVED" in str(drops_string)
        if no_drops:
            rows_without_drops.append(pokemon_label)
        elif removed:
            rows_removed.append(pokemon_label)

        try:
            pokemon_name, form_name = parse_pokemon_name(pokemon_label)
            loot_table_name = get_loot_table_name(pokemon_name, form_name)
            species_file = species_files.get(pokemon_name)
            if species_file is None:
                raise ValueError(f"No species file found for {pokemon_name}")
            species_data = read_species_file(species_file)
            set_species_loot_table(species_data, form_name, loot_table_name)
            loot_table = (
                make_empty_loot_table(loot_table_name)
                if no_drops or removed
                else parse_drops(str(drops_string), loot_table_name)
            )
        except ValueError as error:
            malformed_rows.append((pokemon_label, str(drops_string), str(error)))
            continue

        output_file = LOOT_TABLE_DIRECTORY / f"{loot_table_name}.json"
        with output_file.open("w", encoding="utf-8", newline="\n") as file:
            json.dump(loot_table, file, ensure_ascii=False, indent=2)
            file.write("\n")
        with species_file.open("w", encoding="utf-8", newline="\n") as file:
            json.dump(species_data, file, ensure_ascii=False, indent=2)
            file.write("\n")
        generated.append(output_file.name)
        species_files_updated.add(str(species_file.relative_to(REPOSITORY_ROOT)))

    print_generation_report(
        generated,
        rows_marked_manual,
        rows_without_drops,
        rows_removed,
        malformed_rows,
        species_files_updated,
    )
    print_cobblemon_script_footer("Thanks for using the Cobblemon Drops Generator!")
    input("Press Enter to exit...")


def print_generation_report(
    generated,
    rows_marked_manual,
    rows_without_drops,
    rows_removed,
    malformed_rows,
    species_files_updated,
):
    print("\nGeneration report")
    print(f"  Loot tables generated: {len(generated)}")
    print(f"  Species files updated: {len(species_files_updated)}")
    print(f"  Rows skipped (Manual): {len(rows_marked_manual)}")
    print(f"  Rows with no drops (empty tables): {len(rows_without_drops)}")
    print(f"  Rows marked REMOVED (empty tables): {len(rows_removed)}")
    print(f"  Malformed rows: {len(malformed_rows)}")

    if rows_marked_manual:
        print("\nRows skipped because they are marked as manual:")
        for pokemon in rows_marked_manual:
            print(f"  {pokemon}")

    if rows_without_drops:
        print("\nRows with no drops:")
        for pokemon in rows_without_drops:
            print(f"  {pokemon}")

    if rows_removed:
        print("\nRows marked REMOVED:")
        for pokemon in rows_removed:
            print(f"  {pokemon}")

    if malformed_rows:
        print("\nMalformed rows:")
        for pokemon, drops_string, error in malformed_rows:
            print(f"  {pokemon}: {error}")
            print(f"    Drops: {drops_string}")


def get_species_file_lookup():
    species_files = {}
    for species_file in SPECIES_DIRECTORY.rglob("*.json"):
        pokemon_name = sanitize_pokemon(species_file.stem)
        if pokemon_name in species_files:
            raise ValueError(f"Multiple species files found for {pokemon_name}")
        species_files[pokemon_name] = species_file
    return species_files


def read_species_file(species_file):
    with species_file.open(encoding="utf-8-sig") as file:
        return json.load(file)
    return None


def set_species_loot_table(species_data, form_name, loot_table_name):
    drops = {
        "amount": 1,
        "entries": [
            {
                "type": "loot_table",
                "percentage": 100,
                "quantity": 1,
                "maxSelectableTimes": 1,
                "id": f"cobblemon:pokemon/{loot_table_name}",
            }
        ],
    }

    if form_name is None:
        species_data["drops"] = drops
        return

    normalized_form = FORM_ENTRY_MAPPING.get(form_name, form_name)
    matching_form = next(
        (
            form
            for form in species_data.get("forms", [])
            if form.get("name") == normalized_form
        ),
        None,
    )
    if matching_form is None:
        raise ValueError(f"Form {normalized_form!r} not found in {species_data.get('name')}")
    matching_form["drops"] = drops


def make_empty_loot_table(loot_table_name):
    return {
        "type": "minecraft:generic",
        "pools": [],
        "random_sequence": f"cobblemon:pokemon/{loot_table_name}",
    }


def get_drops_df(dex_range=range(0, 1111)):
    drops_spreadsheet_csv_url = (
        "https://docs.google.com/spreadsheets/d/e/2PACX-1vTcW0b63Lw9LKKRT7AOu_jKqDla1f67bzi-waxNdZ9btGeZy5woHQU_axnjrEVekKcilcu0w8fw41HV/pub?output=csv"
    )
    csv_data = download_spreadsheet_data(drops_spreadsheet_csv_url)
    drops_df = load_data_from_csv(csv_data)

    if dex_range is not None:
        drops_df = drops_df[drops_df["No."].isin(dex_range)]

    return drops_df


def parse_pokemon_name(name):
    if not isinstance(name, str) or not name.strip():
        raise ValueError(f"Invalid Pokémon name: {name!r}")

    name = name.strip()
    if "[" in name:
        pokemon_name, form = name.split("[", 1)
        if not form.endswith("]"):
            raise ValueError(f"Invalid Pokémon form name: {name!r}")
        form_name = form[:-1].strip()
        if not form_name:
            raise ValueError(f"Invalid Pokémon form name: {name!r}")
    else:
        pokemon_name, form_name = name, None

    return sanitize_pokemon(pokemon_name.strip()), form_name


def get_loot_table_name(pokemon_name, form_name=None):
    if form_name is None:
        return pokemon_name

    normalized_form = FORM_ENTRY_MAPPING.get(form_name, form_name)
    form_slug = re.sub(r"[^a-z0-9]+", "_", normalized_form.lower()).strip("_")
    if not form_slug:
        raise ValueError(f"Invalid form name: {form_name!r}")
    return f"{pokemon_name}_{form_slug}"


def parse_drops(drops_string, loot_table_name):
    drop_entries = drops_string.split(" ; ")
    if not drop_entries or any(not entry.strip() or ";" in entry for entry in drop_entries):
        raise ValueError(f"Invalid drops format; expected entries separated by ' ; ': {drops_string!r}")

    pools = []
    for drop_entry in drop_entries:
        item_text = drop_entry.strip()
        is_tag = item_text.startswith("#")
        if is_tag:
            item_text = item_text[1:]

        item_id, separator, amount_text = item_text.rpartition(":")
        if not separator or not item_id or not amount_text:
            raise ValueError(f"Invalid drop entry: {drop_entry!r}")

        item_id = item_id.replace("mc:", "minecraft:", 1).replace("cb:", "cobblemon:", 1)
        if not RESOURCE_LOCATION_PATTERN.fullmatch(item_id):
            raise ValueError(f"Invalid item or tag ID in drop entry: {drop_entry!r}")

        entry = {
            "type": "minecraft:tag" if is_tag else "minecraft:item",
            "name": item_id,
        }
        if is_tag:
            entry["expand"] = True

        if amount_text.endswith("%"):
            percentage_text = amount_text[:-1]
            try:
                percentage = float(percentage_text)
            except ValueError as error:
                raise ValueError(f"Invalid drop percentage in {drop_entry!r}") from error
            if not 0 <= percentage <= 100:
                raise ValueError(f"Drop percentage must be between 0 and 100 in {drop_entry!r}")
            if percentage < 100:
                entry["conditions"] = [
                    {
                        "condition": "minecraft:random_chance",
                        "chance": percentage / 100,
                    }
                ]
        else:
            quantity_range = QUANTITY_RANGE_PATTERN.fullmatch(amount_text)
            if quantity_range is None:
                raise ValueError(f"Expected a quantity range or percentage in {drop_entry!r}")
            minimum, maximum = map(int, quantity_range.groups())
            if maximum < minimum:
                raise ValueError(f"Invalid quantity range in {drop_entry!r}")
            entry["functions"] = [
                {
                    "function": "set_count",
                    "count": {
                        "min": minimum,
                        "max": maximum,
                    },
                }
            ]

        pools.append({"rolls": 1, "entries": [entry]})

    return {
        "type": "minecraft:generic",
        "pools": pools,
        "random_sequence": f"cobblemon:pokemon/{loot_table_name}",
    }


def download_spreadsheet_data(url, max_retries=10):
    delay = 1
    for attempt in range(max_retries):
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            return response.content.decode("utf-8")
        except requests.RequestException:
            if attempt == max_retries - 1:
                raise
            time.sleep(delay)
            delay *= 2
    return None


def load_data_from_csv(csv_data):
    return pd.read_csv(
        StringIO(csv_data),
        encoding="utf8",
        engine="python",
        dtype={"Pokémon": str, "Drops": str},
    )


if __name__ == "__main__":
    try:
        main(range(0, 1111))
    except Exception as error:
        print_warning(f"Error generating Pokémon loot tables: {error}")
        raise
