package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Bastion locator icon options. Used for single-selection in GSR Preferences. */
public enum GSRBastionIconOption {
    DEFAULT(GSRLocatorParameters.DEFAULT_BASTION_ITEM, "Default"),
    PIGLIN_HEAD("minecraft:piglin_head", "Piglin Head"),
    GOLD_INGOT("minecraft:gold_ingot", "Gold Ingot"),
    GOLD_NUGGET("minecraft:gold_nugget", "Gold Nugget"),
    GOLDEN_SWORD("minecraft:golden_sword", "Golden Sword"),
    GOLDEN_APPLE("minecraft:golden_apple", "Golden Apple"),
    CRIMSON_FUNGUS("minecraft:crimson_fungus", "Crimson Fungus"),
    WARPED_FUNGUS("minecraft:warped_fungus", "Warped Fungus"),
    SOUL_LANTERN("minecraft:soul_lantern", "Soul Lantern");

    private final String registryId;
    private final String label;

    GSRBastionIconOption(String registryId, String label) {
        this.registryId = registryId;
        this.label = label;
    }

    public String getRegistryId() {
        return registryId;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRBastionIconOption from(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT;
        String s = stored.trim().toLowerCase();
        for (GSRBastionIconOption opt : values()) {
            if (opt.registryId.equals(s)) return opt;
        }
        return DEFAULT;
    }
}
