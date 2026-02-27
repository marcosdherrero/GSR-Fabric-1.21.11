package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Stronghold locator icon options. Used for single-selection in GSR Preferences. */
public enum GSRStrongholdIconOption {
    DEFAULT(GSRLocatorParameters.DEFAULT_STRONGHOLD_ITEM, "Default"),
    ENDER_EYE("minecraft:ender_eye", "Ender Eye"),
    ENDER_PEARL("minecraft:ender_pearl", "Ender Pearl"),
    END_STONE("minecraft:end_stone", "End Stone"),
    BOOK("minecraft:book", "Book"),
    TORCH("minecraft:torch", "Torch"),
    IRON_BARS("minecraft:iron_bars", "Iron Bars");

    private final String registryId;
    private final String label;

    GSRStrongholdIconOption(String registryId, String label) {
        this.registryId = registryId;
        this.label = label;
    }

    public String getRegistryId() {
        return registryId;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRStrongholdIconOption from(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT;
        String s = stored.trim().toLowerCase();
        for (GSRStrongholdIconOption opt : values()) {
            if (opt.registryId.equals(s)) return opt;
        }
        return DEFAULT;
    }
}
