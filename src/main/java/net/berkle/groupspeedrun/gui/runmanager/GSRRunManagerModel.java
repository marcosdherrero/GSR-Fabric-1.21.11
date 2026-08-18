package net.berkle.groupspeedrun.gui.runmanager;

import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Model for Run Manager screen: players, participant sets, dropdown state.
 * Group Death: selection = excluded; empty = no exclusions (all in). Group Health: empty = all out.
 */
public final class GSRRunManagerModel {

    public record PlayerEntry(UUID uuid, String name) {}

    public final List<PlayerEntry> allPlayers = new ArrayList<>();
    public final Set<UUID> groupDeathParticipants = new HashSet<>();
    public final Set<UUID> sharedHealthParticipants = new HashSet<>();
    public final Set<UUID> excluded = new HashSet<>();

    /** Pending selection while dropdown is open. */
    public final Set<UUID> pendingGroupDeathParticipants = new HashSet<>();
    public final Set<UUID> pendingSharedHealthParticipants = new HashSet<>();

    /** Last time a click was handled (ms). Used for anti-spam cooldown. */
    public long lastClickHandledTimeMs = 0;

    public boolean deathDropdownOpen;
    public boolean healthDropdownOpen;
    public int deathDropdownScroll;
    public int healthDropdownScroll;
    public long deathSelectionTimeMs;
    public long healthSelectionTimeMs;

    /** Load from NBT (from server). */
    public void loadFrom(CompoundTag nbt) {
        allPlayers.clear();
        ListTag playersList = nbt.getList("players").orElse(new ListTag());
        for (int i = 0; i < playersList.size(); i++) {
            Tag el = playersList.get(i);
            if (!(el instanceof CompoundTag c)) continue;
            String uuidStr = c.getString("uuid").orElse("");
            String name = c.getString("name").orElse("Unknown");
            if (!uuidStr.isEmpty()) {
                try {
                    allPlayers.add(new PlayerEntry(UUID.fromString(uuidStr), name));
                } catch (Exception ignored) {}
            }
        }
        groupDeathParticipants.clear();
        readUuidSet(nbt, GSRWorldConfigParameters.K_GROUP_DEATH_PARTICIPANTS, groupDeathParticipants);
        sharedHealthParticipants.clear();
        readUuidSet(nbt, GSRWorldConfigParameters.K_SHARED_HEALTH_PARTICIPANTS, sharedHealthParticipants);
        excluded.clear();
        readUuidSet(nbt, GSRWorldConfigParameters.K_EXCLUDED_FROM_RUN, excluded);
    }

    private static void readUuidSet(CompoundTag nbt, String key, Set<UUID> out) {
        out.clear();
        ListTag list = nbt.getList(key).orElse(new ListTag());
        for (int i = 0; i < list.size(); i++) {
            try {
                Tag el = list.get(i);
                if (el instanceof StringTag nbtStr) {
                    String s = nbtStr.asString().orElse("");
                    if (!s.isEmpty()) out.add(UUID.fromString(s));
                }
            } catch (Exception ignored) {}
        }
    }

    /** Players available for selection (excludes excluded-from-run). */
    public List<PlayerEntry> getSelectablePlayers() {
        List<PlayerEntry> out = new ArrayList<>();
        for (PlayerEntry e : allPlayers) {
            if (!excluded.contains(e.uuid)) out.add(e);
        }
        return out;
    }

    /** Init pending from confirmed when opening. Empty = no exclusions (all in group death). */
    public void initPendingDeath() {
        pendingGroupDeathParticipants.clear();
        pendingGroupDeathParticipants.addAll(groupDeathParticipants);
    }

    /** For Group Health: empty = all out. Init pending from confirmed when opening. */
    public void initPendingHealth() {
        pendingSharedHealthParticipants.clear();
        pendingSharedHealthParticipants.addAll(sharedHealthParticipants);
    }

    /** Apply pending death selection. */
    public void applyPendingDeath() {
        groupDeathParticipants.clear();
        groupDeathParticipants.addAll(pendingGroupDeathParticipants);
        deathSelectionTimeMs = System.currentTimeMillis();
    }

    /** Apply pending health selection. */
    public void applyPendingHealth() {
        sharedHealthParticipants.clear();
        sharedHealthParticipants.addAll(pendingSharedHealthParticipants);
        healthSelectionTimeMs = System.currentTimeMillis();
    }

    /** Toggle player at index in death dropdown. */
    public void toggleDeathIndex(int index) {
        List<PlayerEntry> selectable = getSelectablePlayers();
        if (index < 0 || index >= selectable.size()) return;
        UUID u = selectable.get(index).uuid;
        if (pendingGroupDeathParticipants.contains(u)) {
            pendingGroupDeathParticipants.remove(u);
        } else {
            pendingGroupDeathParticipants.add(u);
        }
    }

    /** Toggle player at index in health dropdown. */
    public void toggleHealthIndex(int index) {
        List<PlayerEntry> selectable = getSelectablePlayers();
        if (index < 0 || index >= selectable.size()) return;
        UUID u = selectable.get(index).uuid;
        if (pendingSharedHealthParticipants.contains(u)) {
            pendingSharedHealthParticipants.remove(u);
        } else {
            pendingSharedHealthParticipants.add(u);
        }
    }

    /** Select all players in death dropdown. */
    public void selectAllDeath() {
        for (PlayerEntry e : getSelectablePlayers()) {
            pendingGroupDeathParticipants.add(e.uuid);
        }
    }

    /** Deselect all players in death dropdown. */
    public void deselectAllDeath() {
        pendingGroupDeathParticipants.clear();
    }

    /** Select all players in health dropdown. */
    public void selectAllHealth() {
        for (PlayerEntry e : getSelectablePlayers()) {
            pendingSharedHealthParticipants.add(e.uuid);
        }
    }

    /** Deselect all players in health dropdown. */
    public void deselectAllHealth() {
        pendingSharedHealthParticipants.clear();
    }

    public boolean hasPendingDeathChanges() {
        return !pendingGroupDeathParticipants.equals(groupDeathParticipants);
    }

    public boolean hasPendingHealthChanges() {
        return !pendingSharedHealthParticipants.equals(sharedHealthParticipants);
    }
}
