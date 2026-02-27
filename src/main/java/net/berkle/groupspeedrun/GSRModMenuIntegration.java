package net.berkle.groupspeedrun;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen;

/**
 * Mod Menu integration: Group Speed Run → Configure opens the GSR Preferences screen (per-player UUID settings).
 * Uses custom dropdowns matching Run History and Locators.
 */
public class GSRModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GSRPreferencesScreen::new;
    }
}
