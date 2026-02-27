#!/usr/bin/env python3
"""
Generates 100 simulated GSR runs with varied group sizes (1-6 players) and up to 10 players.
Output: gsr_folder/personal_runs/run_<id>.json
Run from project root: python scripts/generate_simulated_runs.py
"""

import json
import random
import uuid
from datetime import datetime, timedelta
from pathlib import Path

# 10 player names to use across runs
PLAYER_NAMES = [
    "Alex", "Blaze", "Charlie", "Diana", "Echo",
    "Frost", "Gem", "Haven", "Ivy", "Jade"
]

# Player UUIDs (fixed per name for consistency)
PLAYER_UUIDS = {name: str(uuid.uuid4()) for name in PLAYER_NAMES}

# World names for variety
WORLD_NAMES = ["Speedrun_Alpha", "Speedrun_Beta", "Speedrun_Gamma", "New World", "Practice_World"]

# Realistic death messages for failed runs
DEATH_MESSAGES = [
    "was slain by Zombie", "was shot by Skeleton", "was killed by Creeper",
    "fell from a high place", "drowned", "was burned to death",
    "was killed by Blaze", "was killed by Wither Skeleton",
    "was slain by Enderman", "was killed by Piglin Brute",
    "tried to swim in lava", "was impaled by a fall",
    "was killed by Witch", "was killed by Guardian"
]

# Most common block/entity IDs for snapshots
PLACED_BLOCKS = ["minecraft:oak_log", "minecraft:cobblestone", "minecraft:netherrack", "minecraft:end_stone"]
BROKEN_BLOCKS = ["minecraft:stone", "minecraft:oak_log", "minecraft:netherrack"]
KILLED_ENTITIES = ["minecraft:zombie", "minecraft:skeleton", "minecraft:blaze", "minecraft:enderman"]
EATEN_FOOD = ["minecraft:cooked_beef", "minecraft:golden_apple", "minecraft:bread"]


def make_snapshot(run_id: str, player_uuid: str, player_name: str, is_victory: bool) -> dict:
    """Create a plausible GSRRunPlayerSnapshot."""
    blocks_placed = random.randint(50, 800) if random.random() > 0.3 else 0
    blocks_broken = random.randint(30, 500) if random.random() > 0.3 else 0
    distance = random.uniform(200, 2500)
    damage_dealt = random.uniform(0, 150) if random.random() > 0.4 else 0
    damage_taken = random.uniform(0, 80) if random.random() > 0.3 else 0
    dragon_dmg = random.uniform(0, 400) if is_victory and random.random() > 0.5 else 0
    pearls = random.randint(0, 12) if random.random() > 0.5 else 0
    blaze_rods = random.randint(0, 6) if random.random() > 0.6 else 0

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
        "mostPlacedBlockCount": random.randint(20, blocks_placed) if most_placed else 0,
        "mostBrokenBlockId": most_broken,
        "mostBrokenBlockCount": random.randint(10, blocks_broken) if most_broken else 0,
        "distanceMoved": round(distance, 2),
        "mostTraveledTypeId": random.choice(["sprint", "walk", "fly"]),
        "mostTraveledTypeAmount": round(distance * random.uniform(0.3, 0.8), 2),
        "entityKills": random.randint(0, 25),
        "mostKilledEntityId": most_killed,
        "mostKilledEntityCount": random.randint(1, 8) if most_killed else 0,
        "foodEaten": random.randint(0, 15),
        "mostEatenFoodId": most_eaten,
        "mostEatenFoodCount": random.randint(1, 5) if most_eaten else 0,
        "screenTimeTicks": random.randint(0, 1200),
        "mostUsedScreenId": "minecraft:generic_9x3" if random.random() > 0.7 else None,
        "mostUsedScreenTicks": random.randint(0, 600) if random.random() > 0.7 else 0,
        "fallDamageTaken": round(random.uniform(0, 15), 2) if random.random() > 0.6 else 0,
    }


def strip_nulls(obj):
    """Remove keys with None values so JSON matches GSR loader expectations."""
    if isinstance(obj, dict):
        return {k: strip_nulls(v) for k, v in obj.items() if v is not None}
    if isinstance(obj, list):
        return [strip_nulls(x) for x in obj]
    return obj


def main():
    # Resolve output dir: run/gsr_folder/personal_runs (game dir for Fabric dev)
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent
    out_dir = project_root / "run" / "gsr_folder" / "personal_runs"
    out_dir.mkdir(parents=True, exist_ok=True)

    base_time = datetime(2025, 1, 1, 12, 0, 0)
    runs_created = []

    for i in range(100):
        run_id = str(uuid.uuid4())
        participant_count = random.randint(1, 6)
        participants_pool = random.sample(PLAYER_NAMES, min(participant_count, len(PLAYER_NAMES)))
        # Ensure we use exactly participant_count (may repeat if count > 10, but we cap at 6)
        while len(participants_pool) < participant_count:
            participants_pool.append(random.choice(PLAYER_NAMES))

        is_victory = random.random() < 0.28  # ~28% success rate
        # Duration: victories 12-42 min, fails 3-55 min
        if is_victory:
            duration_ms = random.randint(12 * 60 * 1000, 42 * 60 * 1000)
        else:
            duration_ms = random.randint(3 * 60 * 1000, 55 * 60 * 1000)

        start_dt = base_time + timedelta(days=random.randint(0, 55), hours=random.randint(0, 23), minutes=random.randint(0, 59))
        start_ms = int(start_dt.timestamp() * 1000)
        end_ms = start_ms + duration_ms
        end_dt = datetime.fromtimestamp(end_ms / 1000.0)

        # Split times (ms) - must sum to roughly duration; distribute realistically
        total = duration_ms
        time_nether = int(total * random.uniform(0.15, 0.35))
        time_bastion = int(total * random.uniform(0.05, 0.15))
        time_fortress = int(total * random.uniform(0.20, 0.40))
        time_end = int(total * random.uniform(0.08, 0.20))
        time_dragon = total - time_nether - time_bastion - time_fortress - time_end
        if time_dragon < 0:
            time_dragon = int(total * 0.1)

        if not is_victory:
            time_nether = min(time_nether, random.randint(0, int(total)))
            time_bastion = 0 if random.random() > 0.6 else random.randint(0, int(total * 0.2))
            time_fortress = 0 if random.random() > 0.5 else random.randint(0, int(total * 0.4))
            time_end = 0
            time_dragon = 0

        failed_player = random.choice(participants_pool) if not is_victory else ""
        death_msg = f"{failed_player} {random.choice(DEATH_MESSAGES)}" if not is_victory else ""

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
            snapshots.append(make_snapshot(run_id, uid, name, is_victory))

        doc = strip_nulls({"run": run_record, "participants": participants, "snapshots": snapshots})
        out_path = out_dir / f"run_{run_id}.json"
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(doc, f, indent=None, separators=(",", ":"))

        runs_created.append((run_id, run_record["status"], participant_count))

    print(f"Generated {len(runs_created)} runs in {out_dir}")
    victories = sum(1 for _, s, _ in runs_created if s == "VICTORY")
    print(f"  Victories: {victories}, Fails: {100 - victories}")
    by_size = {}
    for _, _, n in runs_created:
        by_size[n] = by_size.get(n, 0) + 1
    print(f"  By group size: {dict(sorted(by_size.items()))}")


if __name__ == "__main__":
    main()
