package net.berkle.groupspeedrun.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Shared NBT serialization for GSRRunSaveState. Used by server (payload) and client (shared run loader).
 */
public final class GSRRunSaveStateNbt {

    private GSRRunSaveStateNbt() {}

    public static NbtCompound toNbt(GSRRunSaveState state) {
        if (state == null) return new NbtCompound();
        NbtCompound root = new NbtCompound();
        root.put("run", runToNbt(state.record()));
        NbtList parts = new NbtList();
        for (GSRRunParticipant p : state.participants()) parts.add(participantToNbt(p));
        root.put("participants", parts);
        NbtList snaps = new NbtList();
        for (GSRRunPlayerSnapshot s : state.snapshots()) snaps.add(snapshotToNbt(s));
        root.put("snapshots", snaps);
        return root;
    }

    public static GSRRunSaveState fromNbt(NbtCompound root) {
        GSRRunRecord run = runFromNbt(root.getCompound("run").orElse(new NbtCompound()));
        NbtList partsList = root.getList("participants").orElse(new NbtList());
        java.util.List<GSRRunParticipant> participants = new java.util.ArrayList<>();
        for (int i = 0; i < partsList.size(); i++) {
            NbtElement el = partsList.get(i);
            if (el instanceof NbtCompound c) participants.add(participantFromNbt(c));
        }
        NbtList snapsList = root.getList("snapshots").orElse(new NbtList());
        java.util.List<GSRRunPlayerSnapshot> snapshots = new java.util.ArrayList<>();
        for (int i = 0; i < snapsList.size(); i++) {
            NbtElement el = snapsList.get(i);
            if (el instanceof NbtCompound c) snapshots.add(snapshotFromNbt(c));
        }
        return new GSRRunSaveState(run, participants, snapshots);
    }

    private static NbtCompound runToNbt(GSRRunRecord r) {
        NbtCompound c = new NbtCompound();
        c.putString("runId", r.runId());
        c.putString("worldName", r.worldName());
        c.putLong("startMs", r.startMs());
        c.putLong("endMs", r.endMs());
        c.putString("startDateIso", r.startDateIso());
        c.putString("endDateIso", r.endDateIso());
        c.putString("status", r.status());
        c.putString("failedByPlayerName", r.failedByPlayerName());
        c.putString("failedByDeathMessage", r.failedByDeathMessage());
        c.putInt("participantCount", r.participantCount());
        c.putLong("timeNether", r.timeNether());
        c.putLong("timeBastion", r.timeBastion());
        c.putLong("timeFortress", r.timeFortress());
        c.putLong("timeEnd", r.timeEnd());
        c.putLong("timeDragon", r.timeDragon());
        c.putBoolean("deranked", r.deranked());
        c.putString("runDifficulty", r.runDifficulty() != null ? r.runDifficulty() : "");
        return c;
    }

    private static GSRRunRecord runFromNbt(NbtCompound c) {
        return new GSRRunRecord(
            c.getString("runId").orElse(""),
            c.getString("worldName").orElse(""),
            c.getLong("startMs").orElse(0L),
            c.getLong("endMs").orElse(0L),
            c.getString("startDateIso").orElse(""),
            c.getString("endDateIso").orElse(""),
            c.getString("status").orElse(""),
            c.getString("failedByPlayerName").orElse(""),
            c.getString("failedByDeathMessage").orElse(""),
            c.getInt("participantCount").orElse(0),
            c.getLong("timeNether").orElse(0L),
            c.getLong("timeBastion").orElse(0L),
            c.getLong("timeFortress").orElse(0L),
            c.getLong("timeEnd").orElse(0L),
            c.getLong("timeDragon").orElse(0L),
            c.getBoolean("deranked").orElse(false),
            c.getString("runDifficulty").filter(s -> s != null && !s.isEmpty()).orElse("—")
        );
    }

    private static NbtCompound participantToNbt(GSRRunParticipant p) {
        NbtCompound c = new NbtCompound();
        c.putString("runId", p.runId());
        c.putString("playerUuid", p.playerUuid());
        c.putString("playerName", p.playerName());
        return c;
    }

    private static GSRRunParticipant participantFromNbt(NbtCompound c) {
        return new GSRRunParticipant(
            c.getString("runId").orElse(""),
            c.getString("playerUuid").orElse(""),
            c.getString("playerName").orElse("")
        );
    }

    private static NbtCompound snapshotToNbt(GSRRunPlayerSnapshot s) {
        NbtCompound c = new NbtCompound();
        c.putString("runId", s.runId());
        c.putString("playerUuid", s.playerUuid());
        c.putString("playerName", s.playerName());
        c.putFloat("damageDealt", s.damageDealt());
        c.putFloat("damageTaken", s.damageTaken());
        if (s.mostDamageDealtTypeId() != null) c.putString("mostDamageDealtTypeId", s.mostDamageDealtTypeId());
        c.putFloat("mostDamageDealtTypeAmount", s.mostDamageDealtTypeAmount());
        if (s.mostDamageTakenTypeId() != null) c.putString("mostDamageTakenTypeId", s.mostDamageTakenTypeId());
        c.putFloat("mostDamageTakenTypeAmount", s.mostDamageTakenTypeAmount());
        c.putFloat("dragonDamage", s.dragonDamage());
        c.putInt("enderPearls", s.enderPearls());
        c.putInt("blazeRods", s.blazeRods());
        c.putInt("blocksPlaced", s.blocksPlaced());
        c.putInt("blocksBroken", s.blocksBroken());
        if (s.mostPlacedBlockId() != null) c.putString("mostPlacedBlockId", s.mostPlacedBlockId());
        c.putInt("mostPlacedBlockCount", s.mostPlacedBlockCount());
        if (s.mostBrokenBlockId() != null) c.putString("mostBrokenBlockId", s.mostBrokenBlockId());
        c.putInt("mostBrokenBlockCount", s.mostBrokenBlockCount());
        c.putFloat("distanceMoved", s.distanceMoved());
        if (s.mostTraveledTypeId() != null) c.putString("mostTraveledTypeId", s.mostTraveledTypeId());
        c.putFloat("mostTraveledTypeAmount", s.mostTraveledTypeAmount());
        c.putInt("entityKills", s.entityKills());
        if (s.mostKilledEntityId() != null) c.putString("mostKilledEntityId", s.mostKilledEntityId());
        c.putInt("mostKilledEntityCount", s.mostKilledEntityCount());
        c.putInt("foodEaten", s.foodEaten());
        if (s.mostEatenFoodId() != null) c.putString("mostEatenFoodId", s.mostEatenFoodId());
        c.putInt("mostEatenFoodCount", s.mostEatenFoodCount());
        c.putLong("screenTimeTicks", s.screenTimeTicks());
        if (s.mostUsedScreenId() != null) c.putString("mostUsedScreenId", s.mostUsedScreenId());
        c.putLong("mostUsedScreenTicks", s.mostUsedScreenTicks());
        c.putFloat("fallDamageTaken", s.fallDamageTaken());
        return c;
    }

    private static GSRRunPlayerSnapshot snapshotFromNbt(NbtCompound c) {
        return new GSRRunPlayerSnapshot(
            c.getString("runId").orElse(""),
            c.getString("playerUuid").orElse(""),
            c.getString("playerName").orElse(""),
            c.getFloat("damageDealt").orElse(0f),
            c.getFloat("damageTaken").orElse(0f),
            c.getString("mostDamageDealtTypeId").orElse(null),
            c.getFloat("mostDamageDealtTypeAmount").orElse(0f),
            c.getString("mostDamageTakenTypeId").orElse(null),
            c.getFloat("mostDamageTakenTypeAmount").orElse(0f),
            c.getFloat("dragonDamage").orElse(0f),
            c.getInt("enderPearls").orElse(0),
            c.getInt("blazeRods").orElse(0),
            c.getInt("blocksPlaced").orElse(0),
            c.getInt("blocksBroken").orElse(0),
            c.getString("mostPlacedBlockId").orElse(null),
            c.getInt("mostPlacedBlockCount").orElse(0),
            c.getString("mostBrokenBlockId").orElse(null),
            c.getInt("mostBrokenBlockCount").orElse(0),
            c.getFloat("distanceMoved").orElse(0f),
            c.getString("mostTraveledTypeId").orElse(null),
            c.getFloat("mostTraveledTypeAmount").orElse(0f),
            c.getInt("entityKills").orElse(0),
            c.getString("mostKilledEntityId").orElse(null),
            c.getInt("mostKilledEntityCount").orElse(0),
            c.getInt("foodEaten").orElse(0),
            c.getString("mostEatenFoodId").orElse(null),
            c.getInt("mostEatenFoodCount").orElse(0),
            c.getLong("screenTimeTicks").orElse(0L),
            c.getString("mostUsedScreenId").orElse(null),
            c.getLong("mostUsedScreenTicks").orElse(0L),
            c.getFloat("fallDamageTaken").orElse(0f)
        );
    }
}
