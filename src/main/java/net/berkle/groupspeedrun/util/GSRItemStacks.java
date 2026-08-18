package net.berkle.groupspeedrun.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Builds {@link ItemStack}s that can be drawn in GUI on Minecraft 26.2.
 * Registry item holders do not have data components until a world/datapack context exists,
 * so {@code new ItemStack(item)} throws {@code NullPointerException: Components not bound yet}
 * on the title screen. When that happens, a direct holder with {@code ITEM_MODEL} is used so
 * {@code GuiGraphicsExtractor.item} can still resolve the texture.
 */
public final class GSRItemStacks {

    private GSRItemStacks() {}

    /** Returns a drawable stack for {@code item}, or {@link ItemStack#EMPTY} for air/null. */
    public static ItemStack of(ItemLike item) {
        if (item == null) return ItemStack.EMPTY;
        Item resolved = item.asItem();
        if (resolved == null || resolved == Items.AIR) return ItemStack.EMPTY;
        try {
            Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(resolved);
            if (holder != null && holder.areComponentsBound()) {
                return new ItemStack(holder);
            }
        } catch (RuntimeException ignored) {
        }
        return ofUnbound(resolved);
    }

    /**
     * Title-screen / unbound-registry stack: {@link Holder#direct} with {@link DataComponents#ITEM_MODEL}
     * so item rendering does not need the frozen datapack component map.
     */
    private static ItemStack ofUnbound(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return ItemStack.EMPTY;
        DataComponentMap map = DataComponentMap.builder()
                .set(DataComponents.ITEM_MODEL, id)
                .set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()))
                .set(DataComponents.MAX_STACK_SIZE, 64)
                .build();
        return new ItemStack(Holder.direct(item, map), 1);
    }

    /** True when the stack is a real (non-air) item that GUI code can submit for drawing. */
    public static boolean isUsable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item != null && item != Items.AIR;
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
            } catch (RuntimeException ignored) {
            }
        }
        String fromId = prettyId(registryId);
        if (fromId != null) return fromId;
        if (isUsable(stack)) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String fromItem = prettyId(id != null ? id.toString() : null);
            if (fromItem != null) return fromItem;
        }
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
