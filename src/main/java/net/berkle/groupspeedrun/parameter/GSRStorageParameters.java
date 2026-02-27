package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for persistent storage: DataStore directory and file names,
 * stats file, world snapshot suffix, and quick-new-world run count file.
 * Used by {@link net.berkle.groupspeedrun.managers.GSRDataStore}, {@link net.berkle.groupspeedrun.GSRStats},
 * {@link net.berkle.groupspeedrun.managers.GSRWorldSnapshotManager}, and {@link net.berkle.groupspeedrun.util.GSRQuickNewWorld}.
 */
public final class GSRStorageParameters {

    private GSRStorageParameters() {}

    /** Root folder name in instance main dir; all GSR data lives under gsr_folder. */
    public static final String GSR_ROOT = "gsr_folder";

    // --- DataStore (gsr_folder/worlds/<world>/db/) ---
    /** Subdirectory for run history NBT files. */
    public static final String DB_DIR = "db";
    /** Per-run files for O(1) lookup: db/runs/<runId>.json. New runs written here; fallback to tables for legacy. */
    public static final String DB_RUNS_DIR = "runs";
    /** Per-player completed runs: gsr_folder/worlds/<world>/completed_runs/<uuid>/run_<timestamp>.json */
    public static final String COMPLETED_RUNS_DIR = "completed_runs";
    /** Runs table file name. */
    public static final String RUNS_FILE = "runs.json";
    /** Run participants table file name. */
    public static final String PARTICIPANTS_FILE = "run_participants.json";
    /** Run player snapshots table file name. */
    public static final String SNAPSHOTS_FILE = "run_player_snapshots.json";

    // --- GSRStats (gsr_folder/worlds/<world>/stats.json) ---
    public static final String STATS_FILE = "stats.json";

    // --- World snapshot (gsr_folder/snapshots/<world>/, restore on next load) ---
    /** Flag file name: if present in world dir, restore from snapshot on next load. */
    public static final String RESTORE_FLAG_FILE = "restore_next_load.json";

    // --- Quick new world (gsr_folder/run_count.json) ---
    public static final String RUN_COUNT_FILE = "run_count.json";

    // --- Run history (gsr_folder/personal_runs/, gsr_folder/shared_runs/) ---
    /** Personal run history: runs you completed. Persists across worlds. */
    public static final String PERSONAL_RUNS_DIR = "personal_runs";
    /** Shared run history: runs synced from other players. Persists across worlds. */
    public static final String SHARED_RUNS_DIR = "shared_runs";
    /** CSV export output. */
    public static final String EXPORT_DIR = "export";

    /** Suffix for atomic write: write to file.tmp then rename. */
    public static final String ATOMIC_WRITE_SUFFIX = ".tmp";
}
