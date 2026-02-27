package net.berkle.groupspeedrun.config;

import net.minecraft.text.Text;

/**
 * When non-admins can use locator bar commands: Never, Always, or 30 minutes after the relevant split.
 */
public enum GSRLocatorNonAdminMode {
    NEVER(0, "Never"),
    ALWAYS(1, "Always"),
    POST_SPLIT_30MIN(2, "Post 30 Mins");

    private final int value;
    private final String label;

    GSRLocatorNonAdminMode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRLocatorNonAdminMode from(int value) {
        return switch (value) {
            case 0 -> NEVER;
            case 1 -> ALWAYS;
            default -> POST_SPLIT_30MIN;
        };
    }
}
