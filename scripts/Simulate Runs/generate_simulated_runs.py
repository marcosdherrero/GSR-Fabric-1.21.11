#!/usr/bin/env python3
"""
Generates simulated GSR runs with configurable count and player range.
Split order (canonical progression; no cheating):
  1. Nether (required)
  2. Fortress (required - blaze rods for eyes)
  3. Bastion (optional)
  4. End (required)
  5. Dragon (required for victory)
Each split completion adds to the time pool for the next split's randomness.
Output: run/gsr_folder/personal_runs/run_<id>.json
Run from project root: python scripts/generate_simulated_runs.py
"""

import json
import random
import uuid
from datetime import datetime, timedelta
from pathlib import Path

# Player names and UUIDs
PLAYER_NAMES = [
    "Alex", "Blaze", "Charlie", "Diana", "Echo",
    "Frost", "Gem", "Haven", "Ivy", "Jade"
]
PLAYER_UUIDS = {name: str(uuid.uuid4()) for name in PLAYER_NAMES}

WORLD_NAMES = ["Speedrun_Alpha", "Speedrun_Beta", "Speedrun_Gamma", "New World", "Practice_World"]

DEATH_MESSAGES = [
    "was slain by Zombie", "was shot by Skeleton", "was killed by Creeper",
    "fell from a high place", "drowned", "was burned to death",
    "was killed by Blaze", "was killed by Wither Skeleton",
    "was slain by Enderman", "was killed by Piglin Brute",
    "tried to swim in lava", "was impaled by a fall",
    "was killed by Witch", "was killed by Guardian"
]

PLACED_BLOCKS = ["minecraft:oak_log", "minecraft:cobblestone", "minecraft:netherrack", "minecraft:end_stone"]
BROKEN_BLOCKS = ["minecraft:stone", "minecraft:oak_log", "minecraft:netherrack"]
KILLED_ENTITIES = ["minecraft:zombie", "minecraft:skeleton", "minecraft:blaze", "minecraft:enderman"]
EATEN_FOOD = ["minecraft:cooked_beef", "minecraft:golden_apple", "minecraft:bread"]

# Split names for failure point
SPLITS = ["nether", "fortress", "bastion", "end", "dragon"]


def prompt_int(prompt: str, default: int, min_val: int, max_val: int) -> int:
    """Prompt for integer in range; use default on empty/invalid."""
    while True:
        s = input(f"{prompt} [{default}] (min {min_val}, max {max_val}): ").strip()
        if not s:
            return default
        try:
            v = int(s)
            if min_val <= v <= max_val:
                return v
        except ValueError:
            pass
        print(f"  Enter a number between {min_val} and {max_val}.")


def make_snapshot(run_id: str, player_uuid: str, player_name: str, is_victory: bool,
                  splits_completed: int, reached_fortress: bool, reached_bastion: bool) -> dict:
    """Create a plausible GSRRunPlayerSnapshot; stats scale with splits completed."""
    scale = 0.3 + 0.7 * (splits_completed / 5)  # 0.3 at 0 splits, 1.0 at 5
    blocks_placed = int(random.uniform(50, 800) * scale) if random.random() > 0.3 else 0
    blocks_broken = int(random.uniform(30, 500) * scale) if random.random() > 0.3 else 0
    distance = random.uniform(200, 2500) * scale
    damage_dealt = random.uniform(0, 150) * scale if random.random() > 0.4 else 0
    damage_taken = random.uniform(0, 80) * scale if random.random() > 0.3 else 0
    dragon_dmg = random.uniform(0, 400) if is_victory and random.random() > 0.5 else 0
    pearls = int(random.uniform(0, 12) * scale) if random.random() > 0.5 else 0
    # Blazes spawn in fortresses; bastion trading can yield blaze rods. If neither reached, keep very low.
    if reached_fortress or reached_bastion:
        blaze_rods = int(random.uniform(0, 6) * scale) if random.random() > 0.6 else 0
    else:
        blaze_rods = random.randint(0, 1) if random.random() > 0.9 else 0  # Rare spawn in nether hub

    most_placed = random.choice(PLACED_BLOCKS) if blocks_placed > 0 else None
    most_broken = random.choice(BROKEN_BLOCKS) if blocks_broken > 0 else None
    most_killed = random.choice(KILLED_ENTITIES) if random.random() > 0.6 else None
    most_eaten = random.choice(EATEN_FOOD) if random.random() > 0.7 else None

    return {
        "runId": run_id,
        "playerUuid": player_uuid,
        "playerName": player_name,
        "damageDealt": round(damage_dealt, 2),
        "damageTaken": round(damage_taken, 2),
        "mostDamageDealtTypeId": "minecraft:player_attack" if damage_dealt > 0 else None,
        "mostDamageDealtTypeAmount": round(damage_dealt, 2) if damage_dealt > 0 else 0,
        "mostDamageTakenTypeId": random.choice(["minecraft:fall", "minecraft:mob_projectile"]) if damage_taken > 0 else None,
        "mostDamageTakenTypeAmount": round(damage_taken, 2) if damage_taken > 0 else 0,
        "dragonDamage": round(dragon_dmg, 2),
        "enderPearls": pearls,
        "blazeRods": blaze_rods,
        "blocksPlaced": blocks_placed,
        "blocksBroken": blocks_broken,
        "mostPlacedBlockId": most_placed,
        "mostPlacedBlockCount": random.randint(20, blocks_placed) if most_placed and blocks_placed >= 20 else (blocks_placed if most_placed else 0),
        "mostBrokenBlockId": most_broken,
        "mostBrokenBlockCount": random.randint(10, blocks_broken) if most_broken and blocks_broken >= 10 else (blocks_broken if most_broken else 0),
        "distanceMoved": round(distance, 2),
        "mostTraveledTypeId": random.choice(["sprint", "walk", "fly"]),
        "mostTraveledTypeAmount": round(distance * random.uniform(0.3, 0.8), 2),
        "entityKills": int(random.uniform(0, 25) * scale),
        "mostKilledEntityId": most_killed,
        "mostKilledEntityCount": random.randint(1, 8) if most_killed else 0,
        "foodEaten": int(random.uniform(0, 15) * scale),
        "mostEatenFoodId": most_eaten,
        "mostEatenFoodCount": random.randint(1, 5) if most_eaten else 0,
        "screenTimeTicks": int(random.uniform(0, 1200) * scale),
        "mostUsedScreenId": "minecraft:generic_9x3" if random.random() > 0.7 else None,
        "mostUsedScreenTicks": int(random.uniform(0, 600) * scale) if random.random() > 0.7 else 0,
        "fallDamageTaken": round(random.uniform(0, 15), 2) if random.random() > 0.6 else 0,
    }


def strip_nulls(obj):
    """Remove keys with None values so JSON matches GSR loader expectations."""
    if isinstance(obj, dict):
        return {k: strip_nulls(v) for k, v in obj.items() if v is not None}
    if isinstance(obj, list):
        return [strip_nulls(x) for x in obj]
    return obj


def generate_split_times(is_victory: bool) -> tuple[int, int, int, int, int]:
    """
    Generate split times with canonical progression (no cheating):
    1. Nether (required)
    2. Fortress (required - blaze rods for eyes of ender)
    3. Bastion (optional)
    4. End (required - stronghold)
    5. Dragon (required for victory)

    Each split completion adds to the pool size for the next split's randomness.
    Returns (time_nether, time_bastion, time_fortress, time_end, time_dragon).
    """
    MS_PER_MIN = 60 * 1000
    pool_min = 2.5 * MS_PER_MIN
    pool_max = 6.0 * MS_PER_MIN
    fail_at = random.choice(SPLITS) if not is_victory else None

    def draw() -> int:
        return random.randint(int(pool_min), int(pool_max))

    def grow_pool():
        nonlocal pool_min, pool_max
        add_min = random.uniform(1.0, 3.0) * MS_PER_MIN
        add_max = random.uniform(2.0, 5.0) * MS_PER_MIN
        pool_min += add_min
        pool_max += add_max

    # 1. Nether (required)
    time_nether = draw()
    grow_pool()
    if fail_at == "nether":
        return time_nether, 0, 0, 0, 0

    # 2. Fortress (required)
    time_fortress = draw()
    grow_pool()
    if fail_at == "fortress":
        return time_nether, 0, time_fortress, 0, 0

    # 3. Bastion (optional)
    time_bastion = draw() if random.random() > 0.4 else 0
    if time_bastion > 0:
        grow_pool()
    if fail_at == "bastion":
        return time_nether, time_bastion, time_fortress, 0, 0

    # 4. End (required)
    time_end = draw()
    grow_pool()
    if fail_at == "end":
        return time_nether, time_bastion, time_fortress, time_end, 0

    # 5. Dragon (required for victory)
    time_dragon = draw()
    return time_nether, time_bastion, time_fortress, time_end, time_dragon


def main():
    num_runs = prompt_int("Number of runs to simulate", 100, 1, 1000)
    min_players = prompt_int("Min players per run", 1, 1, 10)
    max_players = prompt_int("Max players per run", 6, 1, 10)
    if min_players > max_players:
        min_players, max_players = max_players, min_players

    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent
    out_dir = project_root / "run" / "gsr_folder" / "personal_runs"
    out_dir.mkdir(parents=True, exist_ok=True)

    base_time = datetime(2025, 1, 1, 12, 0, 0)
    runs_created = []

    for i in range(num_runs):
        run_id = str(uuid.uuid4())
        participant_count = random.randint(min_players, max_players)
        participants_pool = random.sample(PLAYER_NAMES, min(participant_count, len(PLAYER_NAMES)))
        while len(participants_pool) < participant_count:
            participants_pool.append(random.choice(PLAYER_NAMES))

        is_victory = random.random() < 0.28
        time_nether, time_bastion, time_fortress, time_end, time_dragon = generate_split_times(is_victory)

        total_ms = time_nether + time_bastion + time_fortress + time_end + time_dragon
        if total_ms < 3 * 60 * 1000:
            total_ms = random.randint(3 * 60 * 1000, 8 * 60 * 1000)
        if is_victory and total_ms < 12 * 60 * 1000:
            total_ms = max(total_ms, random.randint(12 * 60 * 1000, 25 * 60 * 1000))

        start_dt = base_time + timedelta(
            days=random.randint(0, 55),
            hours=random.randint(0, 23),
            minutes=random.randint(0, 59)
        )
        start_ms = int(start_dt.timestamp() * 1000)
        end_ms = start_ms + total_ms
        end_dt = datetime.fromtimestamp(end_ms / 1000.0)

        failed_player = random.choice(participants_pool) if not is_victory else ""
        death_msg = f"{failed_player} {random.choice(DEATH_MESSAGES)}" if not is_victory else ""

        splits_done = sum(1 for t in [time_nether, time_bastion, time_fortress, time_end, time_dragon] if t > 0)
        reached_fortress = time_fortress > 0
        reached_bastion = time_bastion > 0

        run_record = {
            "runId": run_id,
            "worldName": random.choice(WORLD_NAMES),
            "startMs": start_ms,
            "endMs": end_ms,
            "startDateIso": start_dt.strftime("%Y-%m-%dT%H:%M:%S.000Z"),
            "endDateIso": end_dt.strftime("%Y-%m-%dT%H:%M:%S.000Z"),
            "status": "VICTORY" if is_victory else "FAIL",
            "failedByPlayerName": failed_player,
            "failedByDeathMessage": death_msg,
            "participantCount": participant_count,
            "timeNether": time_nether,
            "timeBastion": time_bastion,
            "timeFortress": time_fortress,
            "timeEnd": time_end,
            "timeDragon": time_dragon,
            "deranked": 0,
            "runDifficulty": "Hard" if participant_count >= 2 else random.choice(["Easy", "Normal", "Hard"]),
        }

        participants = []
        snapshots = []
        for name in participants_pool[:participant_count]:
            uid = PLAYER_UUIDS[name]
            participants.append({"runId": run_id, "playerUuid": uid, "playerName": name})
            snapshots.append(make_snapshot(run_id, uid, name, is_victory, splits_done,
                                          reached_fortress, reached_bastion))

        # Victory requires minimum blaze rods (eyes) and ender pearls; top up if needed.
        if is_victory:
            min_blaze = random.randint(4, 7)
            min_pearls = random.randint(10, 14)
            total_blaze = sum(s.get("blazeRods", 0) for s in snapshots)
            total_pearls = sum(s.get("enderPearls", 0) for s in snapshots)
            if total_blaze < min_blaze:
                deficit = min_blaze - total_blaze
                idx = random.randint(0, len(snapshots) - 1) if snapshots else 0
                if snapshots:
                    snapshots[idx]["blazeRods"] = snapshots[idx].get("blazeRods", 0) + deficit
            if total_pearls < min_pearls:
                deficit = min_pearls - total_pearls
                idx = random.randint(0, len(snapshots) - 1) if snapshots else 0
                if snapshots:
                    snapshots[idx]["enderPearls"] = snapshots[idx].get("enderPearls", 0) + deficit

        doc = strip_nulls({"run": run_record, "participants": participants, "snapshots": snapshots})
        out_path = out_dir / f"run_{run_id}.json"
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(doc, f, indent=None, separators=(",", ":"))

        runs_created.append((run_id, run_record["status"], participant_count))

    print(f"\nGenerated {len(runs_created)} runs in {out_dir}")
    victories = sum(1 for _, s, _ in runs_created if s == "VICTORY")
    print(f"  Victories: {victories}, Fails: {len(runs_created) - victories}")
    by_size = {}
    for _, _, n in runs_created:
        by_size[n] = by_size.get(n, 0) + 1
    print(f"  By group size: {dict(sorted(by_size.items()))}")


if __name__ == "__main__":
    main()
