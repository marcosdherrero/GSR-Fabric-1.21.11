package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Fortress locator icon options. Used for single-selection in GSR Preferences. */
public enum GSRFortressIconOption {
    DEFAULT(GSRLocatorParameters.DEFAULT_FORTRESS_ITEM, "Default"),
    BLAZE_ROD("minecraft:blaze_rod", "Blaze Rod"),
    BLAZE_POWDER("minecraft:blaze_powder", "Blaze Powder"),
    MAGMA_CREAM("minecraft:magma_cream", "Magma Cream"),
    FIRE_CHARGE("minecraft:fire_charge", "Fire Charge"),
    NETHER_WART("minecraft:nether_wart", "Nether Wart"),
    NETHER_BRICK("minecraft:nether_brick", "Nether Brick"),
    GHAST_TEAR("minecraft:ghast_tear", "Ghast Tear"),
    GLOWSTONE_DUST("minecraft:glowstone_dust", "Glowstone Dust");

    private final String registryId;
    private final String label;

    GSRFortressIconOption(String registryId, String label) {
        this.registryId = registryId;
        this.label = label;
    }

    public String getRegistryId() {
        return registryId;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRFortressIconOption from(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT;
        String s = stored.trim().toLowerCase();
        for (GSRFortressIconOption opt : values()) {
            if (opt.registryId.equals(s)) return opt;
        }
        return DEFAULT;
    }
}
