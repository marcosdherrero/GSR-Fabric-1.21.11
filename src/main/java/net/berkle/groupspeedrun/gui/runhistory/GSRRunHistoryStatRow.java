package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.parameter.GSRStatTrackerParameters;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

/**
 * Stat category for Run History "Stats vs Avg" chart.
 * Supports snapshot-level stats (from player snapshots) and run-level stats (Run Time, Split Times).
 * Uses descriptive labels for chart display.
 */
public record GSRRunHistoryStatRow(
        String label,
        String descriptiveLabel,
        String unit,
        ToDoubleFunction<GSRRunSaveState> runAccessor,
        /** For player-level charts: (playerName, run) -> value. Run-level stats return same value for all players. */
        BiFunction<String, GSRRunSaveState, Double> playerAccessor,
        boolean isInt,
        /** Quip/leaderboard title (e.g. "Pog Champ"); null if no correlation. Shown in chart title in parentheses. */
        String quipTitle,
        /** True if value is damage (HP) and should display as hearts (value / 2). */
        boolean displayAsHearts
) {

    /** Returns chart title: descriptiveLabel, with quipTitle in parentheses when present. */
    public String chartTitle() {
        return quipTitle != null && !quipTitle.isEmpty()
                ? descriptiveLabel + " (" + quipTitle + ")"
                : descriptiveLabel;
    }

    /** True for times (Run Time, Split Times): best = lowest. False for other scores: best = highest. */
    public boolean isLowerBetter() {
        return "Run Time".equals(label) || "Split Times".equals(label);
    }

    /** Unit suffix for chart titles: " (mm:ss)" for time stats, " (unit)" when unit set, else "". */
    public String unitSuffix() {
        if (!unit().isEmpty()) return " (" + unit().trim() + ")";
        return isLowerBetter() ? " (mm:ss)" : "";
    }

    /** Extract value from run using snapshot max (best in category, e.g. most pearls). */
    private static ToDoubleFunction<GSRRunSaveState> fromSnapshot(ToDoubleFunction<GSRRunPlayerSnapshot> acc) {
        return run -> run.snapshots().stream().mapToDouble(acc::applyAsDouble).max().orElse(0);
    }

    /** Extract value from run using snapshot sum (total across all players, e.g. total party damage taken). */
    private static ToDoubleFunction<GSRRunSaveState> fromSnapshotSum(ToDoubleFunction<GSRRunPlayerSnapshot> acc) {
        return run -> run.snapshots().stream().mapToDouble(acc::applyAsDouble).sum();
    }

    /** Player accessor from snapshot stat: finds player's snapshot and applies accessor. */
    private static BiFunction<String, GSRRunSaveState, Double> fromPlayerSnapshot(ToDoubleFunction<GSRRunPlayerSnapshot> acc) {
        return (playerName, run) -> run.snapshots().stream()
                .filter(s -> playerName != null && playerName.equals(s.playerName()))
                .findFirst()
                .map(s -> acc.applyAsDouble(s))
                .orElse(0.0);
    }

    /** Run-level stat: same value for all players. */
    private static BiFunction<String, GSRRunSaveState, Double> fromRunLevel(ToDoubleFunction<GSRRunSaveState> runAcc) {
        return (playerName, run) -> runAcc.applyAsDouble(run);
    }

    public static final List<GSRRunHistoryStatRow> ALL = List.of(
            // Run Time (default)
            new GSRRunHistoryStatRow("Run Time", "Run Time", "",
                    run -> run.record().endMs() - run.record().startMs(),
                    fromRunLevel(run -> run.record().endMs() - run.record().startMs()), false, null, false),
            // Split Times (aggregate of dimension splits)
            new GSRRunHistoryStatRow("Split Times", "Split Times", "",
                    run -> {
                        var r = run.record();
                        return r.timeNether() + r.timeBastion() + r.timeFortress() + r.timeEnd() + r.timeDragon();
                    },
                    fromRunLevel(run -> {
                        var r = run.record();
                        return r.timeNether() + r.timeBastion() + r.timeFortress() + r.timeEnd() + r.timeDragon();
                    }), false, null, false),
            // Blaze Rods (Pog Champ)
            new GSRRunHistoryStatRow("Blaze Rods", "Blaze Rods Collected", GSRStatTrackerParameters.POG_CHAMP_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::blazeRods), fromPlayerSnapshot(GSRRunPlayerSnapshot::blazeRods), true, GSRStatTrackerParameters.POG_CHAMP_NAME, false),
            // Ender Pearls (Pearl Jam)
            new GSRRunHistoryStatRow("Ender Pearls", "Ender Pearls Collected", GSRStatTrackerParameters.PEARL_JAM_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::enderPearls), fromPlayerSnapshot(GSRRunPlayerSnapshot::enderPearls), true, GSRStatTrackerParameters.PEARL_JAM_NAME, false),
            // Damage Done (ADC)
            new GSRRunHistoryStatRow("Damage Done", "Damage Done", GSRStatTrackerParameters.ADC_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::damageDealt), fromPlayerSnapshot(GSRRunPlayerSnapshot::damageDealt), false, GSRStatTrackerParameters.ADC_NAME, false),
            // Damage Taken (Tank): run-level = sum (total party damage); player-level = per-player; display in hearts
            new GSRRunHistoryStatRow("Damage Taken", "Damage Taken", GSRStatTrackerParameters.TANK_UNIT,
                    fromSnapshotSum(GSRRunPlayerSnapshot::damageTaken), fromPlayerSnapshot(GSRRunPlayerSnapshot::damageTaken), false, GSRStatTrackerParameters.TANK_NAME, true),
            // Distance Moved (Sightseer)
            new GSRRunHistoryStatRow("Distance Moved", "Distance Moved", GSRStatTrackerParameters.SIGHTSEER_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::distanceMoved), fromPlayerSnapshot(GSRRunPlayerSnapshot::distanceMoved), false, GSRStatTrackerParameters.SIGHTSEER_NAME, false),
            // Blocks Placed (Builder placed)
            new GSRRunHistoryStatRow("Blocks Placed", "Blocks Placed", GSRStatTrackerParameters.BUILDER_PLACED_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::blocksPlaced), fromPlayerSnapshot(GSRRunPlayerSnapshot::blocksPlaced), true, GSRStatTrackerParameters.BUILDER_PLACED_NAME, false),
            // Blocks Broken (Builder broken)
            new GSRRunHistoryStatRow("Blocks Broken", "Blocks Broken", GSRStatTrackerParameters.BUILDER_BROKEN_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::blocksBroken), fromPlayerSnapshot(GSRRunPlayerSnapshot::blocksBroken), true, GSRStatTrackerParameters.BUILDER_BROKEN_NAME, false),
            // Dragon Damage (Dragon Warrior)
            new GSRRunHistoryStatRow("Dragon Damage", "Dragon Damage", GSRStatTrackerParameters.DRAGON_WARRIOR_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::dragonDamage), fromPlayerSnapshot(GSRRunPlayerSnapshot::dragonDamage), false, GSRStatTrackerParameters.DRAGON_WARRIOR_NAME, false),
            // Entity Kills (Killer)
            new GSRRunHistoryStatRow("Entity Kills", "Entity Kills", GSRStatTrackerParameters.KILLER_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::entityKills), fromPlayerSnapshot(GSRRunPlayerSnapshot::entityKills), true, GSRStatTrackerParameters.KILLER_NAME, false),
            // Food Eaten (Food Eater)
            new GSRRunHistoryStatRow("Food Eaten", "Food Eaten", GSRStatTrackerParameters.FOOD_EATER_UNIT,
                    fromSnapshot(GSRRunPlayerSnapshot::foodEaten), fromPlayerSnapshot(GSRRunPlayerSnapshot::foodEaten), true, GSRStatTrackerParameters.FOOD_EATER_NAME, false),
            // Screen Time (Screen Addict)
            new GSRRunHistoryStatRow("Screen Time", "Screen Time", GSRStatTrackerParameters.SCREEN_ADDICT_UNIT,
                    fromSnapshot(s -> (double) s.screenTimeTicks()), fromPlayerSnapshot(s -> (double) s.screenTimeTicks()), false, GSRStatTrackerParameters.SCREEN_ADDICT_NAME, false),
            // Fall Damage Taken: run-level = sum; display in hearts
            new GSRRunHistoryStatRow("Fall Damage", "Fall Damage Taken", GSRStatTrackerParameters.FALL_DAMAGE_UNIT,
                    fromSnapshotSum(GSRRunPlayerSnapshot::fallDamageTaken), fromPlayerSnapshot(GSRRunPlayerSnapshot::fallDamageTaken), false, GSRStatTrackerParameters.FALL_DAMAGE_NAME, true)
    );

    /** Compare options sorted alphabetically by label. Default is Run Time. */
    public static final List<GSRRunHistoryStatRow> ALL_SORTED = ALL.stream()
            .sorted(Comparator.comparing(GSRRunHistoryStatRow::label, String.CASE_INSENSITIVE_ORDER))
            .toList();

    /** Index of Run Time in ALL_SORTED; used as default Compare selection. */
    public static final int DEFAULT_COMPARE_INDEX = computeDefaultCompareIndex();

    private static int computeDefaultCompareIndex() {
        for (int i = 0; i < ALL_SORTED.size(); i++) {
            if ("Run Time".equals(ALL_SORTED.get(i).label())) return i;
        }
        return 0;
    }
}
