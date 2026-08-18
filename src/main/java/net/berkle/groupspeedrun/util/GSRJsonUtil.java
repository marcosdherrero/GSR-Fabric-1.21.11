package net.berkle.groupspeedrun.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts between NBT and JSON for file persistence.
 * Standardizes GSR file formats: JSON for structured data, CSV for tabular export.
 */
public final class GSRJsonUtil {

    private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

    private GSRJsonUtil() {}

    /** Writes NBT to path as JSON. */
    public static void writeNbtAsJson(Path path, CompoundTag nbt) throws IOException {
        JsonObject json = nbtCompoundToJson(nbt);
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(json));
    }

    /** Reads JSON from path and returns as NBT. Returns empty compound if file missing or invalid. */
    public static CompoundTag readNbtFromJson(Path path) throws IOException {
        if (!Files.exists(path)) return new CompoundTag();
        String content = Files.readString(path);
        JsonElement el = GSON.fromJson(content, JsonElement.class);
        if (el != null && el.isJsonObject()) return (CompoundTag) jsonToNbt(el.getAsJsonObject());
        return new CompoundTag();
    }

    /**
     * Reads NBT from path. Supports .json (preferred) and .nbt (legacy) for backward compatibility.
     */
    public static CompoundTag readNbtFromFile(Path path) throws IOException {
        if (!Files.exists(path)) return new CompoundTag();
        String name = path.getFileName().toString();
        if (name.endsWith(".json")) return readNbtFromJson(path);
        if (name.endsWith(".nbt")) return net.minecraft.nbt.NbtIo.readCompressed(path, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
        return readNbtFromJson(path);
    }

    /** Converts NBT element to JSON element. */
    public static JsonElement nbtToJson(Tag nbt) {
        if (nbt == null) return com.google.gson.JsonNull.INSTANCE;
        return switch (nbt.getType()) {
            case Tag.COMPOUND_TYPE -> nbtCompoundToJson((CompoundTag) nbt);
            case Tag.LIST_TYPE -> nbtListToJson((ListTag) nbt);
            case Tag.STRING_TYPE -> new JsonPrimitive(((StringTag) nbt).asString().orElse(""));
            case Tag.BYTE_TYPE -> new JsonPrimitive(((ByteTag) nbt).byteValue());
            case Tag.SHORT_TYPE -> new JsonPrimitive(((ShortTag) nbt).shortValue());
            case Tag.INT_TYPE -> new JsonPrimitive(((IntTag) nbt).intValue());
            case Tag.LONG_TYPE -> new JsonPrimitive(((LongTag) nbt).longValue());
            case Tag.FLOAT_TYPE -> new JsonPrimitive(((FloatTag) nbt).floatValue());
            case Tag.DOUBLE_TYPE -> new JsonPrimitive(((DoubleTag) nbt).doubleValue());
            case Tag.BYTE_ARRAY_TYPE -> nbtByteArrayToJson((ByteArrayTag) nbt);
            case Tag.INT_ARRAY_TYPE -> nbtIntArrayToJson((IntArrayTag) nbt);
            case Tag.LONG_ARRAY_TYPE -> nbtLongArrayToJson((LongArrayTag) nbt);
            default -> com.google.gson.JsonNull.INSTANCE;
        };
    }

    private static JsonObject nbtCompoundToJson(CompoundTag c) {
        JsonObject out = new JsonObject();
        for (String key : c.keySet()) {
            Tag el = c.get(key);
            if (el != null) out.add(key, nbtToJson(el));
        }
        return out;
    }

    private static JsonArray nbtListToJson(ListTag list) {
        JsonArray out = new JsonArray();
        for (int i = 0; i < list.size(); i++) out.add(nbtToJson(list.get(i)));
        return out;
    }

    private static JsonArray nbtByteArrayToJson(ByteArrayTag arr) {
        JsonArray out = new JsonArray();
        for (byte b : arr.getByteArray()) out.add(b);
        return out;
    }

    private static JsonArray nbtIntArrayToJson(IntArrayTag arr) {
        JsonArray out = new JsonArray();
        for (int v : arr.getIntArray()) out.add(v);
        return out;
    }

    private static JsonArray nbtLongArrayToJson(LongArrayTag arr) {
        JsonArray out = new JsonArray();
        for (long v : arr.getLongArray()) out.add(v);
        return out;
    }

    /** Converts JSON element to NBT element. */
    public static Tag jsonToNbt(JsonElement json) {
        if (json == null || json.isJsonNull()) return new CompoundTag();
        if (json.isJsonObject()) return jsonObjectToNbt(json.getAsJsonObject());
        if (json.isJsonArray()) return jsonArrayToNbt(json.getAsJsonArray());
        if (json.isJsonPrimitive()) return jsonPrimitiveToNbt(json.getAsJsonPrimitive());
        return new CompoundTag();
    }

    private static CompoundTag jsonObjectToNbt(JsonObject o) {
        CompoundTag c = new CompoundTag();
        for (String key : o.keySet()) c.put(key, jsonToNbt(o.get(key)));
        return c;
    }

    private static ListTag jsonArrayToNbt(JsonArray a) {
        ListTag list = new ListTag();
        for (JsonElement el : a) list.add(jsonToNbt(el));
        return list;
    }

    private static Tag jsonPrimitiveToNbt(JsonPrimitive p) {
        if (p.isBoolean()) return ByteTag.of(p.getAsBoolean());
        if (p.isNumber()) {
            Number n = p.getAsNumber();
            long l = n.longValue();
            // Numbers outside int range must use LongTag to avoid 32-bit truncation (e.g. Unix timestamps)
            if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) return LongTag.of(l);
            if (n instanceof Long || l != n.doubleValue()) return LongTag.of(l);
            if (n instanceof Double || n instanceof Float) return DoubleTag.of(n.doubleValue());
            return IntTag.of(n.intValue());
        }
        if (p.isString()) return StringTag.valueOf(p.getAsString());
        return new CompoundTag();
    }
}
