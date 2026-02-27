package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** End ship (Wings) locator icon options. Used for single-selection in GSR Preferences. */
public enum GSRShipIconOption {
    DEFAULT(GSRLocatorParameters.DEFAULT_SHIP_ITEM, "Default"),
    ELYTRA("minecraft:elytra", "Elytra"),
    ENDER_PEARL("minecraft:ender_pearl", "Ender Pearl"),
    CHORUS_FRUIT("minecraft:chorus_fruit", "Chorus Fruit"),
    PURPUR_BLOCK("minecraft:purpur_block", "Purpur Block"),
    SHULKER_SHELL("minecraft:shulker_shell", "Shulker Shell"),
    DRAGON_BREATH("minecraft:dragon_breath", "Dragon Breath"),
    TOTEM_OF_UNDYING("minecraft:totem_of_undying", "Totem of Undying");

    private final String registryId;
    private final String label;

    GSRShipIconOption(String registryId, String label) {
        this.registryId = registryId;
        this.label = label;
    }

    public String getRegistryId() {
        return registryId;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRShipIconOption from(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT;
        String s = stored.trim().toLowerCase();
        for (GSRShipIconOption opt : values()) {
            if (opt.registryId.equals(s)) return opt;
        }
        return DEFAULT;
    }
}
