package net.berkle.groupspeedrun.config;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

/**
 * Locator icon color options: Default (location-specific) or Minecraft ink (DyeColor) colors.
 * Used for single-selection in GSR Preferences. Each location uses this with its own default.
 * Display icons: Default = glow ink sac; each DyeColor = corresponding dye item.
 */
public enum GSRLocatorColorOption {
    DEFAULT(null),
    BLACK(DyeColor.BLACK),
    BROWN(DyeColor.BROWN),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    WHITE(DyeColor.WHITE),
    PINK(DyeColor.PINK),
    RED(DyeColor.RED),
    ORANGE(DyeColor.ORANGE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    GREEN(DyeColor.GREEN),
    CYAN(DyeColor.CYAN),
    BLUE(DyeColor.BLUE),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    MAGENTA(DyeColor.MAGENTA),
    PURPLE(DyeColor.PURPLE);

    private final DyeColor dye;

    GSRLocatorColorOption(DyeColor dye) {
        this.dye = dye;
    }

    /** Returns the display item for dropdown icons. DEFAULT = glow ink sac; others = corresponding dye. */
    public Item getDisplayItem() {
        if (this == DEFAULT || dye == null) return Items.GLOW_INK_SAC;
        return switch (dye) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }

    /** Returns ARGB color. For DEFAULT, use the provided defaultColor. */
    public int getValue(int defaultColor) {
        if (this == DEFAULT || dye == null) return defaultColor;
        int rgb = dye.getSignColor();
        return 0xFF000000 | rgb;
    }

    /** Returns display name. DEFAULT shows hex code; others show dye name. */
    public Text getDisplayName(int defaultColor) {
        if (this == DEFAULT) return Text.literal("#" + String.format("%06X", defaultColor & 0x00FFFFFF));
        return Text.literal(dye.name().toLowerCase().replace("_", " "));
    }

    /** Resolves stored color to closest option for the given location default. */
    public static GSRLocatorColorOption from(int storedColor, int defaultColor) {
        if (storedColor == defaultColor) return DEFAULT;
        int storedRgb = storedColor & 0x00FFFFFF;
        GSRLocatorColorOption closest = DEFAULT;
        int closestDist = Integer.MAX_VALUE;
        for (GSRLocatorColorOption opt : values()) {
            if (opt == DEFAULT) continue;
            int optRgb = opt.dye.getSignColor();
            int dr = ((storedRgb >> 16) & 0xFF) - ((optRgb >> 16) & 0xFF);
            int dg = ((storedRgb >> 8) & 0xFF) - ((optRgb >> 8) & 0xFF);
            int db = (storedRgb & 0xFF) - (optRgb & 0xFF);
            int dist = dr * dr + dg * dg + db * db;
            if (dist < closestDist) {
                closestDist = dist;
                closest = opt;
            }
        }
        return closest;
    }
}
