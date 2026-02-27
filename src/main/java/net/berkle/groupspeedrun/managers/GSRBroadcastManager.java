package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.data.GSRRunSaveStateNbt;
import net.berkle.groupspeedrun.parameter.GSRTimerConfig;
import net.berkle.groupspeedrun.util.GSRFormatUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import net.berkle.groupspeedrun.network.GSRRunCompletePayload;
import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.berkle.groupspeedrun.parameter.GSRStatTrackerParameters;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Run-end broadcast using the run's saved state: Success/Fail, party size, who died (on fail),
 * Dragon Warrior (on victory), splits, and leaderboard from the run's player stat snapshots.
 * Also provides broadcast-to-run-participants for run start and split messages.
 */
@SuppressWarnings("null")
public final class GSRBroadcastManager {

    private GSRBroadcastManager() {}

    /**
     * Sends a chat message to all players in the active run (not excluded).
     *
     * @param server Minecraft server
     * @param message Chat message (supports § color codes)
     */
    public static void broadcastToRunParticipants(MinecraftServer server, Text message) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!config.excludedFromRun.contains(p.getUuid())) {
                p.sendMessage(message, false);
            }
        }
    }

    /**
     * Broadcast run-end summary using the given save state (record + snapshots).
     * All leaderboard and run statistics are taken from this state for consistency.
     */
    public static void broadcastRunEnd(MinecraftServer server, GSRRunSaveState state) {
        if (server == null || state == null) return;

        GSRRunRecord r = state.record();
        List<GSRRunPlayerSnapshot> snapshots = state.snapshots();

        int partySize = r.participantCount() > 0 ? r.participantCount() : snapshots.size();
        boolean victorious = GSRRunRecord.STATUS_VICTORY.equals(r.status());

        String sepSuccess = "§a§m" + "─".repeat(GSRBroadcastParameters.BAR_LENGTH);
        String sepFail = "§c§m" + "─".repeat(GSRBroadcastParameters.BAR_LENGTH);

        long elapsedMs = r.endMs() - r.startMs();
        String timerLabel = victorious
                ? GSRTimerConfig.COLOR_GOLD + GSRTimerConfig.STYLE_BOLD + "GSR " + GSRTimerConfig.ICON_DRAGON + " " + GSRTimerConfig.LABEL_VICTORY
                : GSRTimerConfig.COLOR_RED + GSRTimerConfig.STYLE_BOLD + "GSR " + GSRTimerConfig.ICON_SKULL + " " + GSRTimerConfig.LABEL_FAIL;
        String timeStr = GSRFormatUtil.formatTime(elapsedMs);

        if (victorious) {
            server.getPlayerManager().broadcast(Text.literal(sepSuccess), false);
            server.getPlayerManager().broadcast(Text.literal(timerLabel + " §f" + timeStr), false);
        } else {
            server.getPlayerManager().broadcast(Text.literal(sepFail), false);
            server.getPlayerManager().broadcast(Text.literal(GSRBroadcastParameters.FAIL_QUIP), false);
            server.getPlayerManager().broadcast(Text.literal(timerLabel + " §f" + timeStr), false);
            if (r.failedByPlayerName() != null && !r.failedByPlayerName().isEmpty()) {
                server.getPlayerManager().broadcast(Text.literal("§c" + r.failedByPlayerName() + " §7- §f" + (r.failedByDeathMessage() != null ? r.failedByDeathMessage() : "died")), false);
            }
        }

        server.getPlayerManager().broadcast(Text.literal("§7Party of §f" + GSRFormatUtil.formatNumber(partySize)), false);

        if (victorious) {
            GSRRunPlayerSnapshot dragon = bestSnapshot(snapshots, GSRRunPlayerSnapshot::dragonDamage);
            if (dragon != null && dragon.dragonDamage() > GSRBroadcastParameters.STAT_EPSILON) {
                server.getPlayerManager().broadcast(Text.literal(GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.DRAGON_WARRIOR_COLOR, GSRStatTrackerParameters.DRAGON_WARRIOR_ICON, GSRStatTrackerParameters.DRAGON_WARRIOR_NAME) + ": §b" + dragon.playerName() + " §7(" + GSRFormatUtil.formatNumber(dragon.dragonDamage()) + GSRStatTrackerParameters.DRAGON_WARRIOR_UNIT + ")"), false);
            }
        }

        StringBuilder splits = new StringBuilder("§7Splits: ");
        if (r.timeNether() > 0) splits.append("§bNether §f").append(GSRFormatUtil.formatTime(r.timeNether())).append(" §7");
        if (r.timeBastion() > 0) splits.append("§bBastion §f").append(GSRFormatUtil.formatTime(r.timeBastion())).append(" §7");
        if (r.timeFortress() > 0) splits.append("§bFortress §f").append(GSRFormatUtil.formatTime(r.timeFortress())).append(" §7");
        if (r.timeEnd() > 0) splits.append("§bEnd §f").append(GSRFormatUtil.formatTime(r.timeEnd())).append(" §7");
        if (r.timeDragon() > 0) splits.append("§bDragon §f").append(GSRFormatUtil.formatTime(r.timeDragon()));
        if (splits.length() > 9) server.getPlayerManager().broadcast(Text.literal(splits.toString().trim()), false);

        addStatLineInt(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.PEARL_JAM_COLOR, GSRStatTrackerParameters.PEARL_JAM_ICON, GSRStatTrackerParameters.PEARL_JAM_NAME), GSRRunPlayerSnapshot::enderPearls, GSRStatTrackerParameters.PEARL_JAM_UNIT);
        addStatLineInt(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.POG_CHAMP_COLOR, GSRStatTrackerParameters.POG_CHAMP_ICON, GSRStatTrackerParameters.POG_CHAMP_NAME), GSRRunPlayerSnapshot::blazeRods, GSRStatTrackerParameters.POG_CHAMP_UNIT);
        addStatLineFloatWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.ADC_COLOR, GSRStatTrackerParameters.ADC_ICON, GSRStatTrackerParameters.ADC_NAME), GSRRunPlayerSnapshot::damageDealt, GSRStatTrackerParameters.ADC_UNIT, GSRRunPlayerSnapshot::mostDamageDealtTypeId, GSRRunPlayerSnapshot::mostDamageDealtTypeAmount);
        addStatLineFloatWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.TANK_COLOR, GSRStatTrackerParameters.TANK_ICON, GSRStatTrackerParameters.TANK_NAME), GSRRunPlayerSnapshot::damageTaken, GSRStatTrackerParameters.TANK_UNIT, GSRRunPlayerSnapshot::mostDamageTakenTypeId, GSRRunPlayerSnapshot::mostDamageTakenTypeAmount);
        addStatLineFloatWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.SIGHTSEER_COLOR, GSRStatTrackerParameters.SIGHTSEER_ICON, GSRStatTrackerParameters.SIGHTSEER_NAME), GSRRunPlayerSnapshot::distanceMoved, GSRStatTrackerParameters.SIGHTSEER_UNIT, GSRRunPlayerSnapshot::mostTraveledTypeId, s -> (double) s.mostTraveledTypeAmount(), false);
        addStatLineIntWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.BUILDER_PLACED_COLOR, GSRStatTrackerParameters.BUILDER_PLACED_ICON, GSRStatTrackerParameters.BUILDER_PLACED_NAME), GSRRunPlayerSnapshot::blocksPlaced, GSRStatTrackerParameters.BUILDER_PLACED_UNIT, GSRRunPlayerSnapshot::mostPlacedBlockId, GSRRunPlayerSnapshot::mostPlacedBlockCount);
        addStatLineIntWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.BUILDER_BROKEN_COLOR, GSRStatTrackerParameters.BUILDER_BROKEN_ICON, GSRStatTrackerParameters.BUILDER_BROKEN_NAME), GSRRunPlayerSnapshot::blocksBroken, GSRStatTrackerParameters.BUILDER_BROKEN_UNIT, GSRRunPlayerSnapshot::mostBrokenBlockId, GSRRunPlayerSnapshot::mostBrokenBlockCount);
        addStatLineIntWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.KILLER_COLOR, GSRStatTrackerParameters.KILLER_ICON, GSRStatTrackerParameters.KILLER_NAME), GSRRunPlayerSnapshot::entityKills, GSRStatTrackerParameters.KILLER_UNIT, GSRRunPlayerSnapshot::mostKilledEntityId, GSRRunPlayerSnapshot::mostKilledEntityCount);
        addStatLineIntWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.FOOD_EATER_COLOR, GSRStatTrackerParameters.FOOD_EATER_ICON, GSRStatTrackerParameters.FOOD_EATER_NAME), GSRRunPlayerSnapshot::foodEaten, GSRStatTrackerParameters.FOOD_EATER_UNIT, GSRRunPlayerSnapshot::mostEatenFoodId, GSRRunPlayerSnapshot::mostEatenFoodCount);
        addStatLineLongWithMost(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.SCREEN_ADDICT_COLOR, GSRStatTrackerParameters.SCREEN_ADDICT_ICON, GSRStatTrackerParameters.SCREEN_ADDICT_NAME), GSRRunPlayerSnapshot::screenTimeTicks, GSRStatTrackerParameters.SCREEN_ADDICT_UNIT, GSRRunPlayerSnapshot::mostUsedScreenId, GSRRunPlayerSnapshot::mostUsedScreenTicks);
        addStatLineFloat(snapshots, server, GSRStatTrackerParameters.broadcastLabel(GSRStatTrackerParameters.FALL_DAMAGE_COLOR, GSRStatTrackerParameters.FALL_DAMAGE_ICON, GSRStatTrackerParameters.FALL_DAMAGE_NAME), GSRRunPlayerSnapshot::fallDamageTaken, GSRStatTrackerParameters.FALL_DAMAGE_UNIT);

        server.getPlayerManager().broadcast(Text.literal(victorious ? sepSuccess : sepFail), false);

        // Send run data to all clients for shared run history
        var payload = new GSRRunCompletePayload(GSRRunSaveStateNbt.toNbt(state));
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    /** Best snapshot by dragon damage (for storing Dragon Warrior on world config). */
    public static GSRRunPlayerSnapshot getDragonWarriorSnapshot(List<GSRRunPlayerSnapshot> snapshots) {
        return bestSnapshot(snapshots, GSRRunPlayerSnapshot::dragonDamage);
    }

    private static GSRRunPlayerSnapshot bestSnapshot(List<GSRRunPlayerSnapshot> snapshots, ToDoubleFunction<GSRRunPlayerSnapshot> value) {
        return snapshots.stream()
            .filter(s -> value.applyAsDouble(s) > GSRBroadcastParameters.STAT_EPSILON)
            .max(Comparator.comparingDouble(value))
            .orElse(null);
    }

    private static void addStatLineFloat(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToDoubleFunction<GSRRunPlayerSnapshot> value, String unit) {
        addStatLineFloatWithMost(snapshots, server, label, value, unit, s -> null, s -> 0f);
    }

    private static void addStatLineFloatWithMost(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToDoubleFunction<GSRRunPlayerSnapshot> value, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> mostAmount) {
        addStatLineFloatWithMost(snapshots, server, label, value, unit, mostId, mostAmount, true);
    }

    private static void addStatLineFloatWithMost(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToDoubleFunction<GSRRunPlayerSnapshot> value, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, java.util.function.ToDoubleFunction<GSRRunPlayerSnapshot> mostAmount, boolean showMostAmountInParens) {
        GSRRunPlayerSnapshot best = bestSnapshot(snapshots, value);
        if (best == null) return;
        double v = value.applyAsDouble(best);
        boolean asHearts = GSRStatTrackerParameters.TANK_UNIT.equals(unit) || GSRStatTrackerParameters.FALL_DAMAGE_UNIT.equals(unit);
        String suffix = asHearts ? GSRFormatUtil.formatDamageAsHearts(v) : GSRFormatUtil.formatNumber(v) + unit;
        String mid = mostId.apply(best);
        double mamt = mostAmount.applyAsDouble(best);
        if (mid != null && mamt > GSRBroadcastParameters.STAT_EPSILON) {
            String typeDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            String mamtStr = asHearts ? GSRFormatUtil.formatNumber(mamt / 2.0) + " " : GSRFormatUtil.formatNumber(mamt) + " ";
            suffix = suffix + " §8(" + (showMostAmountInParens ? mamtStr : "") + typeDisplay + ")";
        }
        server.getPlayerManager().broadcast(Text.literal(label + ": §b" + best.playerName() + " §7(" + suffix + ")"), false);
    }

    private static void addStatLineInt(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToIntFunction<GSRRunPlayerSnapshot> value, String unit) {
        addStatLineIntWithMost(snapshots, server, label, value, unit, s -> null, s -> 0);
    }

    private static void addStatLineIntWithMost(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToIntFunction<GSRRunPlayerSnapshot> value, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, ToIntFunction<GSRRunPlayerSnapshot> mostCount) {
        GSRRunPlayerSnapshot best = snapshots.stream()
            .filter(s -> value.applyAsInt(s) > 0)
            .max(Comparator.comparingInt(value))
            .orElse(null);
        if (best == null) return;
        int v = value.applyAsInt(best);
        String suffix = GSRFormatUtil.formatNumber(v) + unit;
        String mid = mostId.apply(best);
        int mcount = mostCount.applyAsInt(best);
        if (mid != null && mcount > 0) {
            String blockDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = GSRFormatUtil.formatNumber(v) + unit + " §8(" + GSRFormatUtil.formatNumber(mcount) + " " + blockDisplay + ")";
        }
        server.getPlayerManager().broadcast(Text.literal(label + ": §b" + best.playerName() + " §7(" + suffix + ")"), false);
    }

    private static void addStatLineLongWithMost(List<GSRRunPlayerSnapshot> snapshots, MinecraftServer server, String label, ToLongFunction<GSRRunPlayerSnapshot> value, String unit, java.util.function.Function<GSRRunPlayerSnapshot, String> mostId, ToLongFunction<GSRRunPlayerSnapshot> mostTicks) {
        GSRRunPlayerSnapshot best = snapshots.stream()
            .filter(s -> value.applyAsLong(s) > 0)
            .max(Comparator.comparingLong(value))
            .orElse(null);
        if (best == null) return;
        long v = value.applyAsLong(best);
        String suffix = GSRFormatUtil.formatNumber(v) + unit;
        String mid = mostId.apply(best);
        long mticks = mostTicks.applyAsLong(best);
        if (mid != null && mticks > 0) {
            String screenDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
            suffix = GSRFormatUtil.formatNumber(v) + unit + " §8(" + GSRFormatUtil.formatNumber(mticks) + " " + screenDisplay + ")";
        }
        server.getPlayerManager().broadcast(Text.literal(label + ": §b" + best.playerName() + " §7(" + suffix + ")"), false);
    }
}
