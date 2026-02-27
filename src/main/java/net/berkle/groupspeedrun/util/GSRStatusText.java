package net.berkle.groupspeedrun.util;

import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRStatTrackerParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds run status text from world and player config. Used by server (with party size and leaderboard)
 * and client (from cached clientWorldConfig and PLAYER_CONFIG).
 * Format: Run Status, Run Time, Group Death, Shared Health, Splits (HUD-style), Run Stat Leaderboard.
 * Uses § color codes for differentiation; status screen renders them.
 */
public final class GSRStatusText {

    private GSRStatusText() {}

    public static String build(GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig, int partySize) {
        return build(worldConfig, playerConfig, partySize, null, null);
    }

    public static String build(GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig, int partySize, Long displayElapsedMs) {
        return build(worldConfig, playerConfig, partySize, displayElapsedMs, null);
    }

    /**
     * Build status string. When leaderboardLines is non-null and non-empty, use for Run Stat Leaderboard
     * (current run stats). When run is completed, still show Who died / Dragon Warrior above leaderboard lines.
     */
    public static String build(GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig, int partySize, Long displayElapsedMs, List<String> leaderboardLines) {
        if (worldConfig == null) return GSRUiParameters.MSG_PREFIX + GSRUiParameters.STATUS_FALLBACK_MESSAGE;

        long elapsed = displayElapsedMs != null ? displayElapsedMs : worldConfig.getElapsedTime();
        String timeStr = GSRFormatUtil.formatTime(elapsed);

        String runStatus = worldConfig.startTime <= 0 ? "Active"
                : worldConfig.isVictorious ? "Success"
                : worldConfig.isFailed ? "Fail"
                : "Active";
        String runStatusColor = worldConfig.isVictorious ? "§a" : (worldConfig.isFailed ? "§c" : "§e");

        String runTimeState = worldConfig.startTime <= 0 ? "Running"
                : worldConfig.isVictorious || worldConfig.isFailed ? "Completed"
                : worldConfig.isTimerFrozen ? "Paused"
                : "Running";
        // Run not started (startTime <= 0): normal color, not grayed as "paused"
        String runTimeColor = (worldConfig.startTime <= 0 || !worldConfig.isTimerFrozen) ? (worldConfig.isVictorious ? "§a" : (worldConfig.isFailed ? "§c" : "§f")) : "§7";

        StringBuilder sb = new StringBuilder();
        sb.append("§7Run Status: §f").append(runStatusColor).append(runStatus);
        if (worldConfig.antiCheatEnabled && worldConfig.locatorDeranked) sb.append(" §c(deranked)");
        sb.append("\n§7Run Time: §f").append(runTimeColor).append(runTimeState).append(" §f").append(timeStr);
        sb.append("\n§7Group Death: §f").append(worldConfig.groupDeathEnabled ? "§aOn" : "§cOff");
        sb.append("\n§7Shared Health: §f").append(worldConfig.sharedHealthEnabled ? "§aOn" : "§cOff");
        if (worldConfig.antiCheatEnabled && worldConfig.locatorDeranked) sb.append("\n§7Ranked: §cNo §7(locators used)");

        long latest = Math.max(worldConfig.timeNether, Math.max(worldConfig.timeBastion,
                Math.max(worldConfig.timeFortress, Math.max(worldConfig.timeEnd, worldConfig.timeDragon))));
        sb.append("\n§6Splits:");
        sb.append("\n  ").append(formatSplitLine("Nether", worldConfig.timeNether, latest));
        sb.append("\n  ").append(formatSplitLine("Bastion", worldConfig.timeBastion, latest));
        sb.append("\n  ").append(formatSplitLine("Fortress", worldConfig.timeFortress, latest));
        sb.append("\n  ").append(formatSplitLine("The End", worldConfig.timeEnd, latest));
        sb.append("\n  ").append(formatSplitLine("Dragon", worldConfig.timeDragon, latest));

        sb.append("\n§6Run Stat Leaderboard:");
        if (worldConfig.isFailed) {
            String who = (worldConfig.failedByPlayerName != null && !worldConfig.failedByPlayerName.isEmpty())
                    ? worldConfig.failedByPlayerName : "—";
            String msg = (worldConfig.failedByDeathMessage != null && !worldConfig.failedByDeathMessage.isEmpty())
                    ? worldConfig.failedByDeathMessage : "died";
            sb.append("\n  §7Who died: §c").append(who).append(" §7- §f").append(msg);
        } else if (worldConfig.isVictorious) {
            String name = (worldConfig.dragonWarriorName != null && !worldConfig.dragonWarriorName.isEmpty())
                    ? worldConfig.dragonWarriorName : "—";
            sb.append("\n  §7").append(GSRStatTrackerParameters.DRAGON_WARRIOR_NAME).append(": §b").append(name);
            if (worldConfig.dragonWarriorDamage > 0.001f)
                sb.append(" §7(").append(GSRFormatUtil.formatNumber(worldConfig.dragonWarriorDamage)).append(" damage)");
        }
        if (leaderboardLines != null && !leaderboardLines.isEmpty()) {
            for (String line : leaderboardLines) sb.append("\n  ").append(line);
        } else if (!worldConfig.isVictorious && !worldConfig.isFailed) {
            sb.append("\n  §7— (request status for live stats)");
        }

        if (playerConfig != null) {
            String modeName = switch (playerConfig.hudMode) { case 0 -> "Full"; case 1 -> "Compact"; case 2 -> "Auto"; default -> "Full"; };
            sb.append("\n").append(GSRUiParameters.MSG_PREFIX).append("§7Your HUD: §b").append(modeName).append(" §7| scale §b").append(playerConfig.timerScale).append(" §7| ").append(playerConfig.timerHudOnRight ? "Right" : "Left");
        }
        return sb.toString();
    }

    /** One split line with colors: ○/✔/★ + name + time (same logic as HUD). */
    private static String formatSplitLine(String name, long timeMs, long latestMs) {
        if (timeMs <= 0) return "§7○ §b" + name + " §8   --:--";
        String icon = (timeMs == latestMs) ? "§6★ " : "§a✔ ";
        return icon + "§b" + name + " §f   " + GSRFormatUtil.formatTime(timeMs);
    }

    /** Abbreviations for split types in Run History: N, B, F, E, D. */
    private static final String[] SPLIT_ABBREVS = { "N", "B", "F", "E", "D" };

    /**
     * Returns split lines for Run History with abbreviations. Each element is [icon, abbrev, timeStr].
     */
    public static String[][] getSplitLinesForRunHistory(GSRRunRecord record) {
        if (record == null) return new String[0][];
        long latest = Math.max(record.timeNether(), Math.max(record.timeBastion(),
                Math.max(record.timeFortress(), Math.max(record.timeEnd(), record.timeDragon()))));
        long[] times = { record.timeNether(), record.timeBastion(), record.timeFortress(), record.timeEnd(), record.timeDragon() };
        String[][] out = new String[5][3];
        for (int i = 0; i < 5; i++) {
            String icon = times[i] <= 0 ? "§7○ " : (times[i] == latest ? "§6★ " : "§a✔ ");
            String abbrev = SPLIT_ABBREVS[i];
            String timeStr = times[i] <= 0 ? "--:--" : GSRFormatUtil.formatTime(times[i]);
            out[i] = new String[] { icon, abbrev, timeStr };
        }
        return out;
    }

    /**
     * Build status-style text for a run (Run History screen).
     * Handles completed runs (Success/Fail) and active runs (Active, yellow).
     * Uses GSRRunRecord and snapshots; no player config.
     * When record is active and leaderboardLines is non-null/non-empty, uses those for the leaderboard
     * (live stats from server) instead of snapshots.
     *
     * @param record           Run record.
     * @param snapshots        Per-player stat snapshots (empty for active runs until run ends).
     * @param leaderboardLines Optional live leaderboard lines for active run; null to use snapshots or placeholder.
     */
    public static String buildForCompletedRun(GSRRunRecord record, List<GSRRunPlayerSnapshot> snapshots, List<String> leaderboardLines) {
        if (record == null) return GSRUiParameters.MSG_PREFIX + GSRUiParameters.STATUS_FALLBACK_MESSAGE;
        long elapsed = record.endMs() - record.startMs();
        String timeStr = GSRFormatUtil.formatRunTimePlainEnglish(elapsed);
        String runStatus;
        String runStatusColor;
        if (GSRRunRecord.STATUS_ACTIVE.equals(record.status())) {
            runStatus = "Active";
            runStatusColor = "§e";
        } else if (GSRRunRecord.STATUS_VICTORY.equals(record.status())) {
            runStatus = "Success";
            runStatusColor = "§a";
        } else {
            runStatus = "Fail";
            runStatusColor = "§c";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§7Run Status: §f").append(runStatusColor).append(runStatus);
        if (GSRRunRecord.STATUS_VICTORY.equals(record.status())) {
            sb.append("\n").append(GSRBroadcastParameters.VICTORY_QUIP);
        } else if (GSRRunRecord.STATUS_FAIL.equals(record.status())) {
            sb.append("\n").append(GSRBroadcastParameters.FAIL_QUIP);
        }
        if (GSRRunRecord.STATUS_ACTIVE.equals(record.status())) {
            sb.append("\n§7Run Time: §fRunning §f").append(timeStr);
        } else {
            sb.append("\n§7Run Time: §fCompleted in §f").append(timeStr);
        }
        sb.append("\n§7Party: §f").append(GSRFormatUtil.formatNumber(record.participantCount())).append(" players");
        sb.append("\n§7Difficulty: §f").append(record.runDifficulty() != null ? record.runDifficulty() : "—");
        sb.append("\n§7Rank: §f").append(record.deranked() ? "§c[Deranked]" : "§a[Ranked]");

        long latest = Math.max(record.timeNether(), Math.max(record.timeBastion(),
                Math.max(record.timeFortress(), Math.max(record.timeEnd(), record.timeDragon()))));
        sb.append("\n§6Splits:");
        sb.append("\n  ").append(formatSplitLine("Nether", record.timeNether(), latest));
        sb.append("\n  ").append(formatSplitLine("Bastion", record.timeBastion(), latest));
        sb.append("\n  ").append(formatSplitLine("Fortress", record.timeFortress(), latest));
        sb.append("\n  ").append(formatSplitLine("The End", record.timeEnd(), latest));
        sb.append("\n  ").append(formatSplitLine("Dragon", record.timeDragon(), latest));

        sb.append("\n§6Run Stat Leaderboard:");
        if (GSRRunRecord.STATUS_FAIL.equals(record.status())) {
            String who = (record.failedByPlayerName() != null && !record.failedByPlayerName().isEmpty())
                    ? record.failedByPlayerName() : "—";
            String msg = (record.failedByDeathMessage() != null && !record.failedByDeathMessage().isEmpty())
                    ? record.failedByDeathMessage() : "died";
            sb.append("\n  §7Who died: §c").append(who).append(" §7- §f").append(msg);
        } else if (GSRRunRecord.STATUS_VICTORY.equals(record.status()) && snapshots != null) {
            GSRRunPlayerSnapshot dragon = snapshots.stream()
                    .max(Comparator.comparingDouble(GSRRunPlayerSnapshot::dragonDamage))
                    .orElse(null);
            if (dragon != null && dragon.dragonDamage() > 0.001f) {
                sb.append("\n  §7").append(GSRStatTrackerParameters.DRAGON_WARRIOR_NAME).append(": §b").append(dragon.playerName()).append(" §7(").append(GSRFormatUtil.formatNumber(dragon.dragonDamage())).append(GSRStatTrackerParameters.DRAGON_WARRIOR_UNIT).append(")");
            }
        }
        if (GSRRunRecord.STATUS_ACTIVE.equals(record.status()) && leaderboardLines != null && !leaderboardLines.isEmpty()) {
            for (String line : leaderboardLines) sb.append("\n  ").append(line);
        } else if (snapshots != null && !snapshots.isEmpty()) {
            addLeaderboardFromSnapshots(sb, snapshots);
        } else if (GSRRunRecord.STATUS_ACTIVE.equals(record.status())) {
            sb.append("\n  §7— (live stats in progress)");
        }
        return sb.toString();
    }

    /** Overload without leaderboardLines; uses snapshots or placeholder for active runs. */
    public static String buildForCompletedRun(GSRRunRecord record, List<GSRRunPlayerSnapshot> snapshots) {
        return buildForCompletedRun(record, snapshots, null);
    }

    /**
     * Builds compact header line for player tooltip: wins, losses, avg run time.
     * Returns empty if no runs. Used with face+name in tooltip header.
     */
    public static String buildPlayerHeaderSummary(List<GSRRunSaveState> playerRuns) {
        if (playerRuns == null || playerRuns.isEmpty()) return "";
        int victories = 0, fails = 0;
        long totalElapsed = 0;
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            totalElapsed += r.endMs() - r.startMs();
            if (GSRRunRecord.STATUS_VICTORY.equals(r.status())) victories++;
            else if (GSRRunRecord.STATUS_FAIL.equals(r.status())) fails++;
        }
        long avgElapsed = totalElapsed / playerRuns.size();
        StringBuilder sb = new StringBuilder();
        if (victories > 0 || fails > 0) {
            sb.append("§a").append(victories).append(" win §7| §c").append(fails).append(" loss");
        }
        sb.append(sb.length() > 0 ? " §7| " : "").append("§7Avg: §f").append(GSRFormatUtil.formatRunTimePlainEnglish(avgElapsed));
        return sb.toString();
    }

    /**
     * Builds stats row for player tooltip (horizontal scroll): runs count, wins, losses, avg.
     * Single line for display below player name.
     */
    public static String buildPlayerStatsRow(List<GSRRunSaveState> playerRuns) {
        if (playerRuns == null || playerRuns.isEmpty()) return "";
        int n = playerRuns.size();
        int victories = 0, fails = 0;
        long totalElapsed = 0;
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            totalElapsed += r.endMs() - r.startMs();
            if (GSRRunRecord.STATUS_VICTORY.equals(r.status())) victories++;
            else if (GSRRunRecord.STATUS_FAIL.equals(r.status())) fails++;
        }
        long avgElapsed = totalElapsed / n;
        StringBuilder sb = new StringBuilder();
        sb.append("§7Runs: §f").append(n);
        if (victories > 0 || fails > 0) {
            sb.append(" §7| §a").append(victories).append(" win §7| §c").append(fails).append(" loss");
        }
        sb.append(" §7| §7Avg: §f").append(GSRFormatUtil.formatRunTimePlainEnglish(avgElapsed));
        return sb.toString();
    }

    /**
     * Builds score lines for player tooltip (vertical scroll): one line per run with icon and time.
     */
    public static List<String> buildPlayerScoreLines(List<GSRRunSaveState> playerRuns) {
        if (playerRuns == null || playerRuns.isEmpty()) return List.of();
        List<String> lines = new ArrayList<>();
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            long elapsed = r.endMs() - r.startMs();
            String icon = GSRRunRecord.STATUS_VICTORY.equals(r.status()) ? GSRRunHistoryParameters.STATUS_ICON_VICTORY
                    : GSRRunRecord.STATUS_FAIL.equals(r.status()) ? GSRRunHistoryParameters.STATUS_ICON_FAIL
                    : GSRRunHistoryParameters.STATUS_ICON_ACTIVE;
            lines.add(icon + " §f" + GSRFormatUtil.formatRunTimePlainEnglish(elapsed));
        }
        return lines;
    }

    /**
     * Builds a single row of run scores for player tooltip (horizontal news-ticker scroll).
     * Joins icon+time for each run with a separator.
     */
    public static String buildPlayerScoresRow(List<GSRRunSaveState> playerRuns) {
        if (playerRuns == null || playerRuns.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            long elapsed = r.endMs() - r.startMs();
            String icon = GSRRunRecord.STATUS_VICTORY.equals(r.status()) ? GSRRunHistoryParameters.STATUS_ICON_VICTORY
                    : GSRRunRecord.STATUS_FAIL.equals(r.status()) ? GSRRunHistoryParameters.STATUS_ICON_FAIL
                    : GSRRunHistoryParameters.STATUS_ICON_ACTIVE;
            sb.append(sep).append(icon).append(" §f").append(GSRFormatUtil.formatRunTimePlainEnglish(elapsed));
            sep = "  ";
        }
        return sb.toString();
    }

    /**
     * Builds player stats summary for tooltip: runs count, victories, fails, avg run time, best success run time.
     * Best run time is the fastest victorious run only, not the best across all runs.
     *
     * @param playerName Player name.
     * @param playerRuns Runs where this player participated (from run.participants or snapshots).
     * @return Formatted summary string with § color codes.
     */
    public static String buildPlayerStatsSummary(String playerName, List<GSRRunSaveState> playerRuns) {
        if (playerName == null || playerRuns == null || playerRuns.isEmpty()) return "";
        int n = playerRuns.size();
        long totalElapsed = 0;
        long bestSuccessElapsed = Long.MAX_VALUE;
        int victories = 0, fails = 0;
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            long elapsed = r.endMs() - r.startMs();
            totalElapsed += elapsed;
            if (GSRRunRecord.STATUS_VICTORY.equals(r.status()) && elapsed > 0 && elapsed < bestSuccessElapsed) {
                bestSuccessElapsed = elapsed;
            }
            if (GSRRunRecord.STATUS_VICTORY.equals(r.status())) victories++;
            else if (GSRRunRecord.STATUS_FAIL.equals(r.status())) fails++;
        }
        long avgElapsed = totalElapsed / n;
        StringBuilder sb = new StringBuilder();
        sb.append("§7Runs: §f").append(n);
        if (victories > 0 || fails > 0) {
            sb.append(" §7| §a").append(victories).append(" success §7| §c").append(fails).append(" fail");
        }
        sb.append("\n§7Avg Run Time: §f").append(GSRFormatUtil.formatRunTimePlainEnglish(avgElapsed));
        if (bestSuccessElapsed != Long.MAX_VALUE) {
            sb.append("\n§7Best Success: §f").append(GSRFormatUtil.formatRunTimePlainEnglish(bestSuccessElapsed));
        }
        return sb.toString();
    }

    /**
     * Builds average run info for multiple runs (Export CSV preview).
     * Shows: run count, average run time, average splits, avg party size.
     */
    public static String buildAverageRunInfo(List<GSRRunSaveState> runs) {
        if (runs == null || runs.isEmpty()) return GSRUiParameters.MSG_PREFIX + "No runs selected.";
        int n = runs.size();
        long totalElapsed = 0;
        long totalNether = 0, totalBastion = 0, totalFortress = 0, totalEnd = 0, totalDragon = 0;
        int totalParticipants = 0;
        int victories = 0, fails = 0;
        for (GSRRunSaveState s : runs) {
            GSRRunRecord r = s.record();
            totalElapsed += r.endMs() - r.startMs();
            totalNether += r.timeNether();
            totalBastion += r.timeBastion();
            totalFortress += r.timeFortress();
            totalEnd += r.timeEnd();
            totalDragon += r.timeDragon();
            totalParticipants += r.participantCount();
            if (GSRRunRecord.STATUS_VICTORY.equals(r.status())) victories++;
            else if (GSRRunRecord.STATUS_FAIL.equals(r.status())) fails++;
        }
        long avgElapsed = totalElapsed / n;
        String timeStr = GSRFormatUtil.formatRunTimePlainEnglish(avgElapsed);
        StringBuilder sb = new StringBuilder();
        sb.append("§7Export selection: §f").append(n).append(" run(s)");
        sb.append("\n§7Avg Run Time: §f").append(timeStr);
        sb.append("\n§7Avg Party: §f").append(GSRFormatUtil.formatNumber((double) totalParticipants / n)).append(" players");
        sb.append("\n§7Results: §a").append(victories).append(" success §7| §c").append(fails).append(" fail");
        long avgNether = totalNether / n, avgBastion = totalBastion / n, avgFortress = totalFortress / n, avgEnd = totalEnd / n, avgDragon = totalDragon / n;
        long latest = Math.max(avgNether, Math.max(avgBastion, Math.max(avgFortress, Math.max(avgEnd, avgDragon))));
        sb.append("\n§6Avg Splits:");
        sb.append("\n  ").append(formatSplitLine("Nether", avgNether, latest));
        sb.append("\n  ").append(formatSplitLine("Bastion", avgBastion, latest));
        sb.append("\n  ").append(formatSplitLine("Fortress", avgFortress, latest));
        sb.append("\n  ").append(formatSplitLine("The End", avgEnd, latest));
        sb.append("\n  ").append(formatSplitLine("Dragon", avgDragon, latest));
        return sb.toString();
    }

    private static void addLeaderboardFromSnapshots(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots) {
        addLineInt(sb, snapshots, GSRStatTrackerParameters.PEARL_JAM_NAME, GSRRunPlayerSnapshot::enderPearls, GSRStatTrackerParameters.PEARL_JAM_UNIT);
        addLineInt(sb, snapshots, GSRStatTrackerParameters.POG_CHAMP_NAME, GSRRunPlayerSnapshot::blazeRods, GSRStatTrackerParameters.POG_CHAMP_UNIT);
        addLineFloatWithMost(sb, snapshots, GSRStatTrackerParameters.ADC_NAME, GSRRunPlayerSnapshot::damageDealt, GSRStatTrackerParameters.ADC_UNIT, GSRRunPlayerSnapshot::mostDamageDealtTypeId, GSRRunPlayerSnapshot::mostDamageDealtTypeAmount);
        addLineFloatWithMostHearts(sb, snapshots, GSRStatTrackerParameters.TANK_NAME, GSRRunPlayerSnapshot::damageTaken, GSRRunPlayerSnapshot::mostDamageTakenTypeId, GSRRunPlayerSnapshot::mostDamageTakenTypeAmount);
        addLineFloatWithMost(sb, snapshots, GSRStatTrackerParameters.SIGHTSEER_NAME, GSRRunPlayerSnapshot::distanceMoved, GSRStatTrackerParameters.SIGHTSEER_UNIT, GSRRunPlayerSnapshot::mostTraveledTypeId, s -> (double) s.mostTraveledTypeAmount(), false);
        addLineIntWithMost(sb, snapshots, GSRStatTrackerParameters.BUILDER_PLACED_NAME, GSRRunPlayerSnapshot::blocksPlaced, GSRStatTrackerParameters.BUILDER_PLACED_UNIT, GSRRunPlayerSnapshot::mostPlacedBlockId, GSRRunPlayerSnapshot::mostPlacedBlockCount);
        addLineIntWithMost(sb, snapshots, GSRStatTrackerParameters.BUILDER_BROKEN_NAME, GSRRunPlayerSnapshot::blocksBroken, GSRStatTrackerParameters.BUILDER_BROKEN_UNIT, GSRRunPlayerSnapshot::mostBrokenBlockId, GSRRunPlayerSnapshot::mostBrokenBlockCount);
        addLineIntWithMost(sb, snapshots, GSRStatTrackerParameters.KILLER_NAME, GSRRunPlayerSnapshot::entityKills, GSRStatTrackerParameters.KILLER_UNIT, GSRRunPlayerSnapshot::mostKilledEntityId, GSRRunPlayerSnapshot::mostKilledEntityCount);
        addLineIntWithMost(sb, snapshots, GSRStatTrackerParameters.FOOD_EATER_NAME, GSRRunPlayerSnapshot::foodEaten, GSRStatTrackerParameters.FOOD_EATER_UNIT, GSRRunPlayerSnapshot::mostEatenFoodId, GSRRunPlayerSnapshot::mostEatenFoodCount);
        addLineLongWithMost(sb, snapshots, GSRStatTrackerParameters.SCREEN_ADDICT_NAME, GSRRunPlayerSnapshot::screenTimeTicks, GSRStatTrackerParameters.SCREEN_ADDICT_UNIT, GSRRunPlayerSnapshot::mostUsedScreenId, GSRRunPlayerSnapshot::mostUsedScreenTicks);
        addLineFloatHearts(sb, snapshots, GSRStatTrackerParameters.FALL_DAMAGE_NAME, GSRRunPlayerSnapshot::fallDamageTaken);
    }

    private static void addLineLongWithMost(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToLongFunction<GSRRunPlayerSnapshot> accessor, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToLongFunction<GSRRunPlayerSnapshot> mostTicks) {
        GSRRunPlayerSnapshot best = snapshots.stream().max(Comparator.comparingLong(accessor::applyAsLong)).orElse(null);
        if (best == null || accessor.applyAsLong(best) <= 0) return;
        long val = accessor.applyAsLong(best);
        String suffix = GSRFormatUtil.formatNumber(val) + unit;
        String mid = mostId.apply(best);
        long mticks = mostTicks.applyAsLong(best);
        if (mid != null && mticks > 0) {
            String screenDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = GSRFormatUtil.formatNumber(val) + unit + " §8(" + GSRFormatUtil.formatNumber(mticks) + " " + screenDisplay + ")";
        }
        sb.append("\n  §7").append(label).append(": §b").append(best.playerName()).append(" §7(").append(suffix).append(")");
    }

    private static void addLineInt(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToIntFunction<GSRRunPlayerSnapshot> accessor, String unit) {
        addLineIntWithMost(sb, snapshots, label, accessor, unit, s -> null, s -> 0);
    }

    private static void addLineIntWithMost(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToIntFunction<GSRRunPlayerSnapshot> accessor, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToIntFunction<GSRRunPlayerSnapshot> mostCount) {
        GSRRunPlayerSnapshot best = snapshots.stream().max(Comparator.comparingInt(accessor::applyAsInt)).orElse(null);
        if (best == null || accessor.applyAsInt(best) <= 0) return;
        int val = accessor.applyAsInt(best);
        String suffix = GSRFormatUtil.formatNumber(val) + unit;
        String mid = mostId.apply(best);
        int mcount = mostCount.applyAsInt(best);
        if (mid != null && mcount > 0) {
            String blockDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = GSRFormatUtil.formatNumber(val) + unit + " §8(" + GSRFormatUtil.formatNumber(mcount) + " " + blockDisplay + ")";
        }
        sb.append("\n  §7").append(label).append(": §b").append(best.playerName()).append(" §7(").append(suffix).append(")");
    }

    private static void addLineFloat(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> accessor, String unit) {
        addLineFloatWithMost(sb, snapshots, label, accessor, unit, s -> null, s -> 0f);
    }

    private static void addLineFloatHearts(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> accessor) {
        GSRRunPlayerSnapshot best = snapshots.stream().max(Comparator.comparingDouble(accessor::applyAsDouble)).orElse(null);
        if (best == null || accessor.applyAsDouble(best) < 0.001) return;
        double val = accessor.applyAsDouble(best);
        String suffix = GSRFormatUtil.formatDamageAsHearts(val);
        sb.append("\n  §7").append(label).append(": §b").append(best.playerName()).append(" §7(").append(suffix).append(")");
    }

    private static void addLineFloatWithMostHearts(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> accessor, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> mostAmount) {
        GSRRunPlayerSnapshot best = snapshots.stream().max(Comparator.comparingDouble(accessor::applyAsDouble)).orElse(null);
        if (best == null || accessor.applyAsDouble(best) < 0.001) return;
        double val = accessor.applyAsDouble(best);
        String suffix = GSRFormatUtil.formatDamageAsHearts(val);
        String mid = mostId.apply(best);
        double mamt = mostAmount.applyAsDouble(best);
        if (mid != null && mamt > 0.001) {
            String typeDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = suffix + " §8(" + GSRFormatUtil.formatNumber(mamt / 2.0) + " " + typeDisplay + ")";
        }
        sb.append("\n  §7").append(label).append(": §b").append(best.playerName()).append(" §7(").append(suffix).append(")");
    }

    private static void addLineFloatWithMost(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> accessor, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> mostAmount) {
        addLineFloatWithMost(sb, snapshots, label, accessor, unit, mostId, mostAmount, true);
    }

    private static void addLineFloatWithMost(StringBuilder sb, List<GSRRunPlayerSnapshot> snapshots, String label, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> accessor, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> mostAmount, boolean showMostAmountInParens) {
        GSRRunPlayerSnapshot best = snapshots.stream().max(Comparator.comparingDouble(accessor::applyAsDouble)).orElse(null);
        if (best == null || accessor.applyAsDouble(best) < 0.001) return;
        double val = accessor.applyAsDouble(best);
        String suffix = GSRFormatUtil.formatNumber(val) + unit;
        String mid = mostId.apply(best);
        double mamt = mostAmount.applyAsDouble(best);
        if (mid != null && mamt > 0.001) {
            String typeDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = GSRFormatUtil.formatNumber(val) + unit + " §8(" + (showMostAmountInParens ? GSRFormatUtil.formatNumber(mamt) + " " : "") + typeDisplay + ")";
        }
        sb.append("\n  §7").append(label).append(": §b").append(best.playerName()).append(" §7(").append(suffix).append(")");
    }
}
