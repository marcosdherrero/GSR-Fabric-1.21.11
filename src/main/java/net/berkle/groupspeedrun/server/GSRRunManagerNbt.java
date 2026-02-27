package net.berkle.groupspeedrun.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Set;
import java.util.UUID;

/**
 * NBT read/write helpers for Run Manager UUID sets (group death, shared health, excluded).
 */
public final class GSRRunManagerNbt {

    private GSRRunManagerNbt() {}

    public static void writeUuidSet(NbtCompound nbt, String key, Set<UUID> set) {
        NbtList list = new NbtList();
        for (UUID u : set) {
            if (u != null) list.add(NbtString.of(u.toString()));
        }
        nbt.put(key, list);
    }

    public static void readUuidSet(NbtCompound nbt, String key, Set<UUID> out) {
        out.clear();
        NbtList list = nbt.getList(key).orElse(new NbtList());
        for (int i = 0; i < list.size(); i++) {
            try {
                NbtElement el = list.get(i);
                if (el instanceof NbtString nbtStr) {
                    String s = nbtStr.asString().orElse("");
                    if (!s.isEmpty()) out.add(UUID.fromString(s));
                }
            } catch (Exception ignored) {}
        }
    }
}
