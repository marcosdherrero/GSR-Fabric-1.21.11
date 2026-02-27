package net.berkle.groupspeedrun.gui;

import net.berkle.groupspeedrun.parameter.GSRUiParameters;

/**
 * Root style object for GSR menus. Holds common colors, dimensions, and layout values.
 * Screens and components receive this to ensure consistent styling built from a single source.
 *
 * <p>Currently delegates to {@link GSRUiParameters}. Future: extend with fonts, scales, or
 * theme variants (e.g. compact, full).
 */
public record GSRMenuStyle(
        int buttonWidth,
        int buttonHeight,
        int titleColor,
        int backgroundColor,
        int footerYOffset
) {
    /** Default style matching GSRUiParameters. Used by all GSR screens. */
    public static final GSRMenuStyle DEFAULT = new GSRMenuStyle(
            GSRUiParameters.CONTROLS_HALF_BUTTON_WIDTH,
            GSRUiParameters.CONTROLS_BUTTON_HEIGHT,
            GSRUiParameters.TITLE_COLOR,
            GSRUiParameters.SCREEN_BG_DARK,
            GSRUiParameters.CONTROLS_FOOTER_Y_OFFSET
    );
}
