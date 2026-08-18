package net.berkle.groupspeedrun.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Builds {@link ItemStack}s safely on Minecraft 26.2. Item component maps are not bound
 * until a world/datapack context exists, so {@code new ItemStack(item)} on the title
 * screen throws {@code NullPointerException: Components not bound yet}.
 */
public final class GSRItemStacks {

    private GSRItemStacks() {}

    /** Returns a stack for {@code item}, or {@link ItemStack#EMPTY} if components are unbound. */
    public static ItemStack of(ItemLike item) {
        if (item == null) return ItemStack.EMPTY;
        try {
            return new ItemStack(item);
        } catch (NullPointerException e) {
            return ItemStack.EMPTY;
        }
    }
}
