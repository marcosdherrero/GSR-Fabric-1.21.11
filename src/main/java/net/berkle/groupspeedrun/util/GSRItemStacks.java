package net.berkle.groupspeedrun.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Builds {@link ItemStack}s safely on Minecraft 26.2. Item component maps are not bound
 * until a world/datapack context exists, so {@code new ItemStack(item)} and
 * {@link ItemStack#getHoverName()} on the title screen throw
 * {@code NullPointerException: Components not bound yet} (including {@code ItemStack.EMPTY} / Air).
 */
public final class GSRItemStacks {

    private GSRItemStacks() {}

    /** Returns a stack for {@code item}, or {@link ItemStack#EMPTY} if air or components are unbound. */
    public static ItemStack of(ItemLike item) {
        if (item == null) return ItemStack.EMPTY;
        Item resolved = item.asItem();
        if (resolved == null || resolved == Items.AIR) return ItemStack.EMPTY;
        try {
            ItemStack stack = new ItemStack(resolved);
            return isUsable(stack) ? stack : ItemStack.EMPTY;
        } catch (NullPointerException e) {
            return ItemStack.EMPTY;
        }
    }

    /** True when the stack is a real (non-air) item with bound data components. */
    public static boolean isUsable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == null || item == Items.AIR) return false;
        try {
            stack.getHoverName();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Hover name for a stack when components are bound; otherwise a readable registry id
     * ({@code minecraft:blaze_rod} → {@code Blaze Rod}) or {@code fallback}.
     */
    public static String displayName(ItemStack stack, String registryId, String fallback) {
        if (isUsable(stack)) {
            try {
                String name = stack.getHoverName().getString();
                if (name != null && !name.isBlank() && !"Air".equalsIgnoreCase(name)) {
                    return name;
                }
            } catch (NullPointerException ignored) {
            }
        }
        String fromId = prettyId(registryId);
        if (fromId != null) return fromId;
        return fallback != null && !fallback.isBlank() ? fallback : "Default";
    }

    /** {@code minecraft:blaze_rod} → {@code Blaze Rod}. Null/blank → null. */
    public static String prettyId(String registryId) {
        if (registryId == null || registryId.isBlank()) return null;
        String s = registryId.trim();
        int colon = s.indexOf(':');
        if (colon >= 0) s = s.substring(colon + 1);
        if (s.isBlank() || "air".equalsIgnoreCase(s)) return null;
        String[] parts = s.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
