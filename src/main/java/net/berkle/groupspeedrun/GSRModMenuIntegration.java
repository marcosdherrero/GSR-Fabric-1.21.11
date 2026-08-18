package net.berkle.groupspeedrun;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen;

/**
 * Mod Menu 20 {@code modmenu} entrypoint. Group Speed Run → Configure opens GSR Preferences
 * (the same screen as the in-game GSR Config button). Uses {@link ModMenuApi#getModConfigScreenFactory()}
 * (Mod Menu 20 has no {@code getConfigScreen} override).
 */
public class GSRModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new GSRPreferencesScreen(parent);
    }
}
