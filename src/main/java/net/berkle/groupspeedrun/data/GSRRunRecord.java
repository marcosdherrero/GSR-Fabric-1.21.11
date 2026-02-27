package net.berkle.groupspeedrun.data;

/**
 * One row in the runs table. Immutable record for a completed run.
 * Used for run history, leaderboards, and graph data.
 * startDateIso / endDateIso are ISO-8601 strings for display and filtering.
 */
public record GSRRunRecord(
    String runId,
    String worldName,
    long startMs,
    long endMs,
    String startDateIso,
    String endDateIso,
    String status,
    String failedByPlayerName,
    String failedByDeathMessage,
    int participantCount,
    long timeNether,
    long timeBastion,
    long timeFortress,
    long timeEnd,
    long timeDragon,
    boolean deranked,
    /** Difficulty: "Hard" when multiplayer; else lowest difficulty during run (Peaceful/Easy/Normal/Hard). */
    String runDifficulty
) {
    /** Ordinals: 0=Peaceful, 1=Easy, 2=Normal, 3=Hard. */
    private static final String[] DIFFICULTY_NAMES = { "Peaceful", "Easy", "Normal", "Hard" };

    /**
     * Computes run difficulty: "Hard" when multiplayer (participantCount >= 2);
     * otherwise the lowest difficulty during run from ordinal (0–3), or "—" if unknown.
     */
    public static String computeRunDifficulty(int participantCount, int lowestDifficultyOrdinal) {
        if (participantCount >= 2) return "Hard";
        if (lowestDifficultyOrdinal < 0 || lowestDifficultyOrdinal > 3) return "—";
        return DIFFICULTY_NAMES[lowestDifficultyOrdinal];
    }

    public static final String STATUS_VICTORY = "VICTORY";
    public static final String STATUS_FAIL = "FAIL";
    /** In-progress run (not yet completed). Used for active run in Run History when in world. */
    public static final String STATUS_ACTIVE = "IN_PROGRESS";
}
