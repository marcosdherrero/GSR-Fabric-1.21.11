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
        if (registryId == null || registryId.isBlank()) return GSRItemStacks.of(defaultItem);
        try {
            String s = registryId.trim();
            String[] parts = s.split(":", 2);
            Identifier id = parts.length == 2 ? Identifier.fromNamespaceAndPath(parts[0], parts[1]) : Identifier.fromNamespaceAndPath("minecraft", s);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null && item != Items.AIR) return GSRItemStacks.of(item);
        } catch (Exception ignored) {}
        return GSRItemStacks.of(defaultItem);
    }
}
