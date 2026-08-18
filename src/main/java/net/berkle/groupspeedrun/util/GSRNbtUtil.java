package net.berkle.groupspeedrun.util;

import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * JSON round-trip stores booleans as 0/1 numbers and small longs as ints.
 * Minecraft 26.x Optional NBT getters are type-strict, so world config would
 * otherwise load as primed (startTime=-1, isVictorious=false, splits=0).
 */
public final class GSRNbtUtil {

    private GSRNbtUtil() {}

    public static OptionalLong getLong(CompoundTag nbt, String key) {
        if (nbt == null || key == null) return OptionalLong.empty();
        var asLong = nbt.getLong(key);
        if (asLong.isPresent()) return OptionalLong.of(asLong.get());
        var asInt = nbt.getInt(key);
        if (asInt.isPresent()) return OptionalLong.of(asInt.get().longValue());
        var asByte = nbt.getByte(key);
        if (asByte.isPresent()) return OptionalLong.of(asByte.get().longValue());
        var asDouble = nbt.getDouble(key);
        if (asDouble.isPresent()) return OptionalLong.of(asDouble.get().longValue());
        var asFloat = nbt.getFloat(key);
        if (asFloat.isPresent()) return OptionalLong.of(asFloat.get().longValue());
        return OptionalLong.empty();
    }

    public static OptionalInt getInt(CompoundTag nbt, String key) {
        OptionalLong v = getLong(nbt, key);
        return v.isPresent() ? OptionalInt.of((int) v.getAsLong()) : OptionalInt.empty();
    }

    public static Optional<Boolean> getBoolean(CompoundTag nbt, String key) {
        if (nbt == null || key == null) return Optional.empty();
        var asBool = nbt.getBoolean(key);
        if (asBool.isPresent()) return asBool;
        var asByte = nbt.getByte(key);
        if (asByte.isPresent()) return Optional.of(asByte.get() != 0);
        var asInt = nbt.getInt(key);
        if (asInt.isPresent()) return Optional.of(asInt.get() != 0);
        var asLong = nbt.getLong(key);
        if (asLong.isPresent()) return Optional.of(asLong.get() != 0L);
        var asDouble = nbt.getDouble(key);
        if (asDouble.isPresent()) return Optional.of(asDouble.get() != 0d);
        return Optional.empty();
    }

    public static Optional<Float> getFloat(CompoundTag nbt, String key) {
        if (nbt == null || key == null) return Optional.empty();
        var asFloat = nbt.getFloat(key);
        if (asFloat.isPresent()) return asFloat;
        var asDouble = nbt.getDouble(key);
        if (asDouble.isPresent()) return Optional.of(asDouble.get().floatValue());
        var asLong = getLong(nbt, key);
        if (asLong.isPresent()) return Optional.of((float) asLong.getAsLong());
        return Optional.empty();
    }

    public static String getString(CompoundTag nbt, String key, String fallback) {
        if (nbt == null || key == null) return fallback;
        return nbt.getString(key).orElse(fallback);
    }
}
