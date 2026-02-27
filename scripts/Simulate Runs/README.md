# GSR Scripts

## generate_simulated_runs.py

Generates simulated GSR (Group Speed Run) runs for testing the Run History screen, charts, and player comparisons.

### Usage

**Click and run (Windows):** Double-click `generate_simulated_runs.bat` in the project root. A console window will open, run the script, and stay open so you can see the output.

**Click and run (Mac/Linux):** Make executable (`chmod +x scripts/run_simulate.sh`) then run `./scripts/run_simulate.sh` or double-click from a file manager that supports .sh execution.

**Command line:** From the project root:

```bash
python scripts/generate_simulated_runs.py
```

The script will prompt for:

- **Number of runs** — How many simulated runs to generate (e.g. 50, 100)
- **Min players** — Minimum participants per run (1–10)
- **Max players** — Maximum participants per run (1–10)

### Requirements

Python 3.6+ (standard library only; no extra packages). Ensure `python` (or `python3` on Mac/Linux) is in your PATH.

### Output

Writes JSON files to `run/gsr_folder/personal_runs/run_<uuid>.json` (Fabric dev game directory). Each file matches the GSR run format: run record, participants, and per-player snapshots.

### Split Order (Canonical Progression)

Splits follow the required Minecraft speedrun progression (no cheating):

1. **Nether** — Required (enter Nether)
2. **Fortress** — Required (blaze rods for eyes of ender)
3. **Bastion** — Optional; may be skipped (0 time)
4. **End** — Required (stronghold portal)
5. **Dragon** — Required for victory (dragon kill)

Victory requires a dragon kill; you cannot reach the dragon without completing Nether, Fortress, and End.

### Timing Model

- Each split’s duration is drawn from a random pool.
- Completing a split **increases the pool size** for the next split (later splits have more variance).
- Failed runs stop at a random split; later splits are 0.

### Statistics

Per-player snapshots (damage, blocks, pearls, blaze rods, etc.) are randomized but scaled with run outcome and split progression.

- **Victory runs:** Enforced random minimum blaze rods (4–7) and ender pearls (10–14) across participants (required for eyes of ender).
- **Blaze rods:** If the run never reached fortress or bastion, blaze rods stay very low (0–1) since blazes spawn in fortresses; bastion trading can also yield rods.
