package net.berkle.groupspeedrun.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
        if (registryId == null || registryId.isBlank()) return new ItemStack(defaultItem);
        try {
            String s = registryId.trim();
            String[] parts = s.split(":", 2);
            Identifier id = parts.length == 2 ? Identifier.of(parts[0], parts[1]) : Identifier.of("minecraft", s);
            Item item = Registries.ITEM.get(id);
            if (item != null && item != Items.AIR) return new ItemStack(item);
        } catch (Exception ignored) {}
        return new ItemStack(defaultItem);
    }
}
