package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes boundKey so GSR keybind screen can set keys like vanilla. */
@Mixin(KeyMapping.class)
public interface GSRKeyBindingAccessor {
    @Accessor("key")
    InputConstants.Key getBoundKey();

    @Accessor("key")
    void setBoundKey(InputConstants.Key key);
}
