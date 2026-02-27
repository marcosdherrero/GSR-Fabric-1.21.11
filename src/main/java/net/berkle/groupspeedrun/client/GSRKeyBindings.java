package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.parameter.GSRKeyBindingParameters;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/**
 * GSR key bindings (client). Translation keys and default keys in {@link GSRKeyBindingParameters}.
 * Uses category "key.category.gsr" so they appear under "GSR" in Options → Controls → Key Binds.
 */
public final class GSRKeyBindings {

    public static final String NEW_WORLD_KEY = GSRKeyBindingParameters.NEW_WORLD_KEY;
    public static final String OPEN_OPTIONS_KEY = GSRKeyBindingParameters.OPEN_OPTIONS_KEY;
    public static final String OPEN_CONFIG_KEY = GSRKeyBindingParameters.OPEN_CONFIG_KEY;
    public static final String TOGGLE_HUD_KEY = GSRKeyBindingParameters.TOGGLE_HUD_KEY;
    public static final String PRESS_TO_SHOW_HUD_KEY = GSRKeyBindingParameters.PRESS_TO_SHOW_HUD_KEY;

    /** Category for Options → Controls so GSR keybinds appear in a "GSR" section. */
    private static final KeyBinding.Category GSR_CATEGORY = KeyBinding.Category.create(Identifier.of("gsr", "category"));

    public static KeyBinding newGsrWorldKey;
    public static KeyBinding openGsrOptionsKey;
    /** Open GSR Config (default G+C: hold G, press C). */
    public static KeyBinding openGsrConfigKey;
    /** Toggle HUD on/off (default V). */
    public static KeyBinding toggleGsrHudKey;
    /** Hold to show HUD (default Tab). */
    public static KeyBinding pressToShowGsrHudKey;

    public static void register() {
        newGsrWorldKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                NEW_WORLD_KEY,
                InputUtil.Type.KEYSYM,
                GSRKeyBindingParameters.DEFAULT_NEW_WORLD,
                GSR_CATEGORY
        ));
        openGsrOptionsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                OPEN_OPTIONS_KEY,
                InputUtil.Type.KEYSYM,
                GSRKeyBindingParameters.DEFAULT_OPEN_OPTIONS,
                GSR_CATEGORY
        ));
        openGsrConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                OPEN_CONFIG_KEY,
                InputUtil.Type.KEYSYM,
                GSRKeyBindingParameters.DEFAULT_OPEN_CONFIG,
                GSR_CATEGORY
        ));
        toggleGsrHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                TOGGLE_HUD_KEY,
                InputUtil.Type.KEYSYM,
                GSRKeyBindingParameters.DEFAULT_TOGGLE_HUD,
                GSR_CATEGORY
        ));
        pressToShowGsrHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                PRESS_TO_SHOW_HUD_KEY,
                InputUtil.Type.KEYSYM,
                GSRKeyBindingParameters.DEFAULT_PRESS_TO_SHOW_HUD,
                GSR_CATEGORY
        ));
    }
}
