package net.berkle.groupspeedrun.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

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
    public static void writeNbtAsJson(Path path, NbtCompound nbt) throws IOException {
        JsonObject json = nbtCompoundToJson(nbt);
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(json));
    }

    /** Reads JSON from path and returns as NBT. Returns empty compound if file missing or invalid. */
    public static NbtCompound readNbtFromJson(Path path) throws IOException {
        if (!Files.exists(path)) return new NbtCompound();
        String content = Files.readString(path);
        JsonElement el = GSON.fromJson(content, JsonElement.class);
        if (el != null && el.isJsonObject()) return (NbtCompound) jsonToNbt(el.getAsJsonObject());
        return new NbtCompound();
    }

    /**
     * Reads NBT from path. Supports .json (preferred) and .nbt (legacy) for backward compatibility.
     */
    public static NbtCompound readNbtFromFile(Path path) throws IOException {
        if (!Files.exists(path)) return new NbtCompound();
        String name = path.getFileName().toString();
        if (name.endsWith(".json")) return readNbtFromJson(path);
        if (name.endsWith(".nbt")) return net.minecraft.nbt.NbtIo.readCompressed(path, net.minecraft.nbt.NbtSizeTracker.ofUnlimitedBytes());
        return readNbtFromJson(path);
    }

    /** Converts NBT element to JSON element. */
    public static JsonElement nbtToJson(NbtElement nbt) {
        if (nbt == null) return com.google.gson.JsonNull.INSTANCE;
        return switch (nbt.getType()) {
            case NbtElement.COMPOUND_TYPE -> nbtCompoundToJson((NbtCompound) nbt);
            case NbtElement.LIST_TYPE -> nbtListToJson((NbtList) nbt);
            case NbtElement.STRING_TYPE -> new JsonPrimitive(((NbtString) nbt).asString().orElse(""));
            case NbtElement.BYTE_TYPE -> new JsonPrimitive(((NbtByte) nbt).byteValue());
            case NbtElement.SHORT_TYPE -> new JsonPrimitive(((NbtShort) nbt).shortValue());
            case NbtElement.INT_TYPE -> new JsonPrimitive(((NbtInt) nbt).intValue());
            case NbtElement.LONG_TYPE -> new JsonPrimitive(((NbtLong) nbt).longValue());
            case NbtElement.FLOAT_TYPE -> new JsonPrimitive(((NbtFloat) nbt).floatValue());
            case NbtElement.DOUBLE_TYPE -> new JsonPrimitive(((NbtDouble) nbt).doubleValue());
            case NbtElement.BYTE_ARRAY_TYPE -> nbtByteArrayToJson((NbtByteArray) nbt);
            case NbtElement.INT_ARRAY_TYPE -> nbtIntArrayToJson((NbtIntArray) nbt);
            case NbtElement.LONG_ARRAY_TYPE -> nbtLongArrayToJson((NbtLongArray) nbt);
            default -> com.google.gson.JsonNull.INSTANCE;
        };
    }

    private static JsonObject nbtCompoundToJson(NbtCompound c) {
        JsonObject out = new JsonObject();
        for (String key : c.getKeys()) {
            NbtElement el = c.get(key);
            if (el != null) out.add(key, nbtToJson(el));
        }
        return out;
    }

    private static JsonArray nbtListToJson(NbtList list) {
        JsonArray out = new JsonArray();
        for (int i = 0; i < list.size(); i++) out.add(nbtToJson(list.get(i)));
        return out;
    }

    private static JsonArray nbtByteArrayToJson(NbtByteArray arr) {
        JsonArray out = new JsonArray();
        for (byte b : arr.getByteArray()) out.add(b);
        return out;
    }

    private static JsonArray nbtIntArrayToJson(NbtIntArray arr) {
        JsonArray out = new JsonArray();
        for (int v : arr.getIntArray()) out.add(v);
        return out;
    }

    private static JsonArray nbtLongArrayToJson(NbtLongArray arr) {
        JsonArray out = new JsonArray();
        for (long v : arr.getLongArray()) out.add(v);
        return out;
    }

    /** Converts JSON element to NBT element. */
    public static NbtElement jsonToNbt(JsonElement json) {
        if (json == null || json.isJsonNull()) return new NbtCompound();
        if (json.isJsonObject()) return jsonObjectToNbt(json.getAsJsonObject());
        if (json.isJsonArray()) return jsonArrayToNbt(json.getAsJsonArray());
        if (json.isJsonPrimitive()) return jsonPrimitiveToNbt(json.getAsJsonPrimitive());
        return new NbtCompound();
    }

    private static NbtCompound jsonObjectToNbt(JsonObject o) {
        NbtCompound c = new NbtCompound();
        for (String key : o.keySet()) c.put(key, jsonToNbt(o.get(key)));
        return c;
    }

    private static NbtList jsonArrayToNbt(JsonArray a) {
        NbtList list = new NbtList();
        for (JsonElement el : a) list.add(jsonToNbt(el));
        return list;
    }

    private static NbtElement jsonPrimitiveToNbt(JsonPrimitive p) {
        if (p.isBoolean()) return NbtByte.of(p.getAsBoolean());
        if (p.isNumber()) {
            Number n = p.getAsNumber();
            long l = n.longValue();
            // Numbers outside int range must use NbtLong to avoid 32-bit truncation (e.g. Unix timestamps)
            if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) return NbtLong.of(l);
            if (n instanceof Long || l != n.doubleValue()) return NbtLong.of(l);
            if (n instanceof Double || n instanceof Float) return NbtDouble.of(n.doubleValue());
            return NbtInt.of(n.intValue());
        }
        if (p.isString()) return NbtString.of(p.getAsString());
        return new NbtCompound();
    }
}
