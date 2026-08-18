package net.berkle.groupspeedrun.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Resolves locator icon items from registry ID strings. Used by the locator HUD and preview.
 */
public final class GSRLocatorIconHelper {

    private GSRLocatorIconHelper() {}

    /**
     * Returns an ItemStack for the given registry ID (e.g. "minecraft:blaze_rod").
     * Falls back to the default item if the ID is invalid.
     */
    public static ItemStack getItemStack(String registryId, Item defaultItem) {
        ItemStack fromId = lookup(registryId);
        if (GSRItemStacks.isUsable(fromId)) return fromId;
        ItemStack fromDefault = GSRItemStacks.of(defaultItem);
        return GSRItemStacks.isUsable(fromDefault) ? fromDefault : ItemStack.EMPTY;
    }

    private static ItemStack lookup(String registryId) {
        if (registryId == null || registryId.isBlank()) return ItemStack.EMPTY;
        try {
            String s = registryId.trim();
            if ("minecraft:air".equalsIgnoreCase(s) || "air".equalsIgnoreCase(s)) return ItemStack.EMPTY;
            String[] parts = s.split(":", 2);
            Identifier id = parts.length == 2 ? Identifier.fromNamespaceAndPath(parts[0], parts[1]) : Identifier.fromNamespaceAndPath("minecraft", s);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null || item == Items.AIR) return ItemStack.EMPTY;
            return GSRItemStacks.of(item);
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }
}
