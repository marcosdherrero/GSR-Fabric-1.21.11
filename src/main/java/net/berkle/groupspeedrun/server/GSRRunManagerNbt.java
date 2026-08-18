package net.berkle.groupspeedrun.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.Set;
import java.util.UUID;

/**
 * NBT read/write helpers for Run Manager UUID sets (group death, shared health, excluded).
 */
public final class GSRRunManagerNbt {

    private GSRRunManagerNbt() {}

    public static void writeUuidSet(CompoundTag nbt, String key, Set<UUID> set) {
        ListTag list = new ListTag();
        for (UUID u : set) {
            if (u != null) list.add(StringTag.valueOf(u.toString()));
        }
        nbt.put(key, list);
    }

    public static void readUuidSet(CompoundTag nbt, String key, Set<UUID> out) {
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
}
