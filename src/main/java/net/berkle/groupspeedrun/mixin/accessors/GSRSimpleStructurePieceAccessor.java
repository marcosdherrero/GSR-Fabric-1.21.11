package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.structure.SimpleStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes templateIdString so ship piece can be detected by path (minecraft:end_city/ship). */
@Mixin(SimpleStructurePiece.class)
public interface GSRSimpleStructurePieceAccessor {
    @Accessor("templateIdString")
    String gsr$getTemplateIdString();
}
