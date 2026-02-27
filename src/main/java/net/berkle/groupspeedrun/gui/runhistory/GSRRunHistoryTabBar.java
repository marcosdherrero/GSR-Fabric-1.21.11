package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Renders the three-tab bar (Run Info, Run Graphs, Player Graphs) for the detail panel.
 * Uses vanilla button texture style.
 */
public final class GSRRunHistoryTabBar {

    private static final String[] TAB_LABELS = { "Run Info", "Run Graphs", "Player Graphs" };

    public GSRRunHistoryTabBar() {}

    /**
     * Renders the tab row.
     *
     * @param context      Draw context.
     * @param textRenderer Text renderer.
     * @param detailLeft   Left edge of detail panel.
     * @param detailRight  Right edge of detail panel.
     * @param detailTop    Top edge of detail panel.
     * @param selectedTab  Index of selected tab (0–2).
     * @param mouseX       Current mouse X for hover.
     * @param mouseY       Current mouse Y for hover.
     */
    public void render(DrawContext context, TextRenderer textRenderer,
                      int detailLeft, int detailRight, int detailTop,
                      int selectedTab, int mouseX, int mouseY) {
        int tabY = detailTop + GSRRunHistoryParameters.TAB_TOP_OFFSET;
        int tabRowWidth = detailRight - detailLeft - 2 * GSRRunHistoryParameters.CONTENT_PADDING;
        int tabWidth = (tabRowWidth - 2 * GSRRunHistoryParameters.TAB_GAP) / 3;
        int tabHeight = GSRRunHistoryParameters.SELECTOR_BAR_HEIGHT;
        int tab1X = detailLeft + GSRRunHistoryParameters.CONTENT_PADDING;
        int tab2X = tab1X + tabWidth + GSRRunHistoryParameters.TAB_GAP;
        int tab3X = tab2X + tabWidth + GSRRunHistoryParameters.TAB_GAP;

        var textures = GSRPressableWidgetAccessor.gsr$getTextures();
        for (int i = 0; i < 3; i++) {
            int x = i == 0 ? tab1X : (i == 1 ? tab2X : tab3X);
            boolean hovered = mouseX >= x && mouseX < x + tabWidth && mouseY >= tabY && mouseY < tabY + tabHeight;
            boolean focused = selectedTab == i || hovered;
            var texture = textures.get(true, focused);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, tabY, tabWidth, tabHeight);
            int textInset = GSRRunHistoryParameters.CONTAINER_INSET + GSRRunHistoryParameters.LIST_TEXT_INSET;
            context.drawTextWithShadow(textRenderer, Text.literal(TAB_LABELS[i]), x + textInset,
                    tabY + (tabHeight - textRenderer.fontHeight) / 2, GSRRunHistoryParameters.TEXT_COLOR);
        }
    }

    /**
     * Returns the tab index (0–2) at (mouseX, mouseY), or -1 if none.
     */
    public int getTabIndexAt(int detailLeft, int detailRight, int detailTop, int mouseX, int mouseY) {
        int tabY = detailTop + GSRRunHistoryParameters.TAB_TOP_OFFSET;
        int tabRowWidth = detailRight - detailLeft - 2 * GSRRunHistoryParameters.CONTENT_PADDING;
        int tabWidth = (tabRowWidth - 2 * GSRRunHistoryParameters.TAB_GAP) / 3;
        int tabHeight = GSRRunHistoryParameters.SELECTOR_BAR_HEIGHT;
        int tab1X = detailLeft + GSRRunHistoryParameters.CONTENT_PADDING;
        int tab2X = tab1X + tabWidth + GSRRunHistoryParameters.TAB_GAP;
        int tab3X = tab2X + tabWidth + GSRRunHistoryParameters.TAB_GAP;

        if (mouseY < tabY || mouseY >= tabY + tabHeight) return -1;
        if (mouseX >= tab1X && mouseX < tab1X + tabWidth) return 0;
        if (mouseX >= tab2X && mouseX < tab2X + tabWidth) return 1;
        if (mouseX >= tab3X && mouseX < tab3X + tabWidth) return 2;
        return -1;
    }
}
