package net.berkle.groupspeedrun.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base screen for GSR menus. Provides common parent handling and back navigation.
 * Screens extending this get consistent footer layout and goBack behavior.
 *
 * <p>Screens can override {@link #goBack()} for custom behavior. Use
 * {@link GSRMenuStyle#DEFAULT} for styling.
 */
public abstract class GSRBaseScreen extends Screen {

    /** Parent screen for back navigation. Null when this is the root. */
    protected final Screen parent;

    protected GSRBaseScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    /** Returns the parent screen for re-opening after config sync or back navigation. */
    public Screen getParent() {
        return parent;
    }

    /** Returns to parent or closes screen if root. Override for custom back behavior. */
    protected void goBack() {
        if (minecraft != null) {
            if (parent != null) {
                minecraft.setScreen(parent);
            } else {
                minecraft.setScreen(null);
            }
        }
    }
}
