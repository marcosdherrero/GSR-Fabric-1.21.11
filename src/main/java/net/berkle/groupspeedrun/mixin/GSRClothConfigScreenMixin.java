package net.berkle.groupspeedrun.mixin;

import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * When the GSR config screen is open, enum/dropdown selections apply immediately on change
 * instead of requiring the user to click Done.
 */
@Mixin(AbstractConfigScreen.class)
public abstract class GSRClothConfigScreenMixin {

    @Unique
    private static final String GSR_TITLE = "GSR Preferences";

    @Unique
    private final Map<AbstractConfigEntry<?>, Object> gsr$lastEnumValues = new HashMap<>();

    /** Injects at end of tick to detect enum changes and save immediately for GSR config. */
    @Inject(method = "tick", at = @At("TAIL"))
    private void gsr$tickImmediateEnumApply(CallbackInfo ci) {
        AbstractConfigScreen self = (AbstractConfigScreen) (Object) this;
        Text title = self.getTitle();
        if (title == null || !GSR_TITLE.equals(title.getString())) return;

        for (var category : self.getCategorizedEntries().values()) {
            for (AbstractConfigEntry<?> entry : category) {
                if (!(entry instanceof SelectionListEntry<?> sel)) continue;
                Object current = sel.getValue();
                Object prev = gsr$lastEnumValues.get(entry);
                if (prev != null && !prev.equals(current)) {
                    self.saveAll(false);
                    gsr$lastEnumValues.put(entry, current);
                    return;
                }
                gsr$lastEnumValues.put(entry, current);
            }
        }
    }
}
