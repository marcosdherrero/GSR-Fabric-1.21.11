package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes templateName so ship piece can be detected by path (minecraft:end_city/ship). */
@Mixin(TemplateStructurePiece.class)
public interface GSRSimpleStructurePieceAccessor {
    @Accessor("templateName")
    String gsr$getTemplateName();
}
