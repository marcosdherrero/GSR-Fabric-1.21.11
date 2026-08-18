package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Exposes CATEGORIES list so GSR can find the GSR category index for scroll-to-category. */
@Mixin(KeyMapping.Category.class)
public interface GSRKeyBindingCategoryAccessor {
    @Accessor("CATEGORIES")
    static List<KeyMapping.Category> gsr$getCategories() {
        throw new AssertionError("Mixin failed to apply");
    }
}
