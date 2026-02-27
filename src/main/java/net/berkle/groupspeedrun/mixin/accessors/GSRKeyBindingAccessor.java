package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes boundKey so GSR keybind screen can set keys like vanilla. */
@Mixin(KeyBinding.class)
public interface GSRKeyBindingAccessor {
    @Accessor("boundKey")
    InputUtil.Key getBoundKey();

    @Accessor("boundKey")
    void setBoundKey(InputUtil.Key key);
}
