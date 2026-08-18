package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.gui.GSRControlsScreen;
import net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Opens GSR Config / Controls. Defers {@code setScreen} to the next client tick so 26.2
 * {@code getChildAt} (first overlapping widget wins) and PauseScreen click dispatch cannot
 * replace the screen after {@code onPress}.
 */
public final class GSRScreens {

    private GSRScreens() {}

    /** Opens {@link GSRPreferencesScreen} (GSR Config). */
    public static void openConfig(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        Screen next = new GSRPreferencesScreen(parent);
        client.execute(() -> client.gui.setScreen(next));
    }

    /** Opens {@link GSRControlsScreen} (run controls). */
    public static void openControls(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        Screen next = new GSRControlsScreen(parent);
        client.execute(() -> client.gui.setScreen(next));
    }
}
