package net.berkle.groupspeedrun.util;

import net.berkle.groupspeedrun.data.GSRRunParticipant;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.parameter.GSRStorageParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exports melted run history to CSV for loading into a transactional database.
 * One row per (run, participant) with run-level, participant, and snapshot fields.
 */
public final class GSRRunHistoryCsvExport {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")
            .withZone(ZoneId.systemDefault());

    private GSRRunHistoryCsvExport() {}

    /**
     * Exports all runs to a CSV file in gsr_folder/export.
     *
     * @param runs All runs to export (e.g. from GSRSharedRunLoader.loadAll()).
     * @return Path to the written file, or null on failure.
     */
    public static Path exportToCsv(List<GSRRunSaveState> runs) throws IOException {
        Path exportDir = GSRStoragePaths.getGsrRoot().resolve(GSRStorageParameters.EXPORT_DIR);
        Files.createDirectories(exportDir);
        String filename = "run_history_" + TIMESTAMP_FORMAT.format(Instant.now()) + ".csv";
        Path path = exportDir.resolve(filename);

        StringBuilder sb = new StringBuilder();
        appendHeader(sb);
        for (GSRRunSaveState state : runs) {
            appendRunRows(sb, state);
        }
        Files.writeString(path, sb.toString());
        return path;
    }

    private static void appendHeader(StringBuilder sb) {
        sb.append("run_id,world_name,start_ms,end_ms,start_date_iso,end_date_iso,status,failed_by_player_name,failed_by_death_message,");
        sb.append("participant_count,time_nether,time_bastion,time_fortress,time_end,time_dragon,deranked,");
        sb.append("player_uuid,player_name,");
        sb.append("damage_dealt,damage_taken,most_damage_dealt_type_id,most_damage_dealt_type_amount,most_damage_taken_type_id,most_damage_taken_type_amount,dragon_damage,ender_pearls,blaze_rods,blocks_placed,blocks_broken,");
        sb.append("most_placed_block_id,most_placed_block_count,most_broken_block_id,most_broken_block_count,distance_moved,most_traveled_type_id,");
        sb.append("entity_kills,most_killed_entity_id,most_killed_entity_count,food_eaten,most_eaten_food_id,most_eaten_food_count,");
        sb.append("screen_time_ticks,most_used_screen_id,most_used_screen_ticks,fall_damage_taken\n");
    }

    private static void appendRunRows(StringBuilder sb, GSRRunSaveState state) {
        GSRRunRecord r = state.record();
        Map<String, GSRRunPlayerSnapshot> snapshotByUuid = new HashMap<>();
        for (GSRRunPlayerSnapshot s : state.snapshots()) {
            snapshotByUuid.put(s.playerUuid(), s);
        }

        if (state.participants().isEmpty()) {
            appendRunOnlyRow(sb, r);
            return;
        }
        for (GSRRunParticipant p : state.participants()) {
            sb.append(escape(r.runId())).append(',');
            sb.append(escape(r.worldName())).append(',');
            sb.append(r.startMs()).append(',');
            sb.append(r.endMs()).append(',');
            sb.append(escape(r.startDateIso())).append(',');
            sb.append(escape(r.endDateIso())).append(',');
            sb.append(escape(r.status())).append(',');
            sb.append(escape(r.failedByPlayerName())).append(',');
            sb.append(escape(r.failedByDeathMessage())).append(',');
            sb.append(r.participantCount()).append(',');
            sb.append(r.timeNether()).append(',');
            sb.append(r.timeBastion()).append(',');
            sb.append(r.timeFortress()).append(',');
            sb.append(r.timeEnd()).append(',');
            sb.append(r.timeDragon()).append(',');
            sb.append(r.deranked()).append(',');
            sb.append(escape(p.playerUuid())).append(',');
            sb.append(escape(p.playerName())).append(',');

            GSRRunPlayerSnapshot snap = snapshotByUuid.get(p.playerUuid());
            if (snap != null) {
                sb.append(snap.damageDealt()).append(',');
                sb.append(snap.damageTaken()).append(',');
                sb.append(escape(snap.mostDamageDealtTypeId())).append(',');
                sb.append(snap.mostDamageDealtTypeAmount()).append(',');
                sb.append(escape(snap.mostDamageTakenTypeId())).append(',');
                sb.append(snap.mostDamageTakenTypeAmount()).append(',');
                sb.append(snap.dragonDamage()).append(',');
                sb.append(snap.enderPearls()).append(',');
                sb.append(snap.blazeRods()).append(',');
                sb.append(snap.blocksPlaced()).append(',');
                sb.append(snap.blocksBroken()).append(',');
                sb.append(escape(snap.mostPlacedBlockId())).append(',');
                sb.append(snap.mostPlacedBlockCount()).append(',');
                sb.append(escape(snap.mostBrokenBlockId())).append(',');
                sb.append(snap.mostBrokenBlockCount()).append(',');
                sb.append(snap.distanceMoved()).append(',');
                sb.append(escape(snap.mostTraveledTypeId())).append(',');
                sb.append(snap.entityKills()).append(',');
                sb.append(escape(snap.mostKilledEntityId())).append(',');
                sb.append(snap.mostKilledEntityCount()).append(',');
                sb.append(snap.foodEaten()).append(',');
                sb.append(escape(snap.mostEatenFoodId())).append(',');
                sb.append(snap.mostEatenFoodCount()).append(',');
                sb.append(snap.screenTimeTicks()).append(',');
                sb.append(escape(snap.mostUsedScreenId())).append(',');
                sb.append(snap.mostUsedScreenTicks()).append(',');
                sb.append(snap.fallDamageTaken());
            } else {
                sb.append(",,,,,,,0,0,0,0,0,,0,,0,0,0,,0,,0,0,0,,0,0,,0,0,,0,0");
            }
            sb.append('\n');
        }
    }

    /** One row when run has no participants. */
    private static void appendRunOnlyRow(StringBuilder sb, GSRRunRecord r) {
        sb.append(escape(r.runId())).append(',');
        sb.append(escape(r.worldName())).append(',');
        sb.append(r.startMs()).append(',');
        sb.append(r.endMs()).append(',');
        sb.append(escape(r.startDateIso())).append(',');
        sb.append(escape(r.endDateIso())).append(',');
        sb.append(escape(r.status())).append(',');
        sb.append(escape(r.failedByPlayerName())).append(',');
        sb.append(escape(r.failedByDeathMessage())).append(',');
        sb.append(r.participantCount()).append(',');
        sb.append(r.timeNether()).append(',');
        sb.append(r.timeBastion()).append(',');
        sb.append(r.timeFortress()).append(',');
        sb.append(r.timeEnd()).append(',');
        sb.append(r.timeDragon()).append(',');
        sb.append(r.deranked()).append(',');
        sb.append(",,");  // player_uuid, player_name
        sb.append(",,,0,0,0,0,0,,0,,0,0,0,,0,0,,0,0,,0,0\n");  // snapshot fields
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
