package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Exposes CATEGORIES list so GSR can find the GSR category index for scroll-to-category. */
@Mixin(KeyBinding.Category.class)
public interface GSRKeyBindingCategoryAccessor {
    @Accessor("CATEGORIES")
    static List<KeyBinding.Category> gsr$getCategories() {
        throw new AssertionError("Mixin failed to apply");
    }
}
