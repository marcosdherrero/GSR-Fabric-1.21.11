package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.gui.GSRControlsScreen;
import net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Opens GSR Config / Controls. Defers {@code setScreen} to the next client tick so
 * {@code getChildAt} (first overlapping widget wins) and pause-menu click dispatch cannot
 * replace the screen after {@code onPress}.
 * <p>The square pause/title GSR button uses {@link #openControls}. GSR Config (Preferences)
 * is {@link #openConfig}, including Mod Menu.
 */
public final class GSRScreens {

    private GSRScreens() {}

    /** Opens {@link GSRPreferencesScreen} (GSR Config). */
    public static void openConfig(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        Screen next = new GSRPreferencesScreen(parent);
        client.execute(() -> client.setScreen(next));
    }

    /** Opens {@link GSRControlsScreen} (run controls). */
    public static void openControls(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        Screen next = new GSRControlsScreen(parent);
        client.execute(() -> client.setScreen(next));
    }
}
