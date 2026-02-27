package net.berkle.groupspeedrun.gui.components;

import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.util.GSRScrollbarHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;

/**
 * Generic multi-select dropdown: trigger bar + overlay list.
 * Uses same layout and styling as Run History dropdowns for consistency.
 * Built from {@link GSRMultiSelectDropdownBehavior} so any screen model can supply data.
 *
 * @param <M> Screen model type.
 */
public final class GSRMultiSelectDropdown<M> {

    /** Confirmation button label. Same for all dropdowns. */
    public static final String CONFIRM_BUTTON_TEXT = "Make selection";

    /** Suffix for default option; when present, drawn in white while prefix uses label color. */
    private static final String DEFAULT_SUFFIX = " (Default)";

    private static final String ICON_UNSELECTED = "§7○ §f";
    private static final String ICON_SELECTED = "§a✔ §f";

    private final String label;
    private final String header;
    private final String tickerKeyPrefix;
    private final boolean multiSelect;
    private final GSRMultiSelectDropdownBehavior<M> behavior;

    public GSRMultiSelectDropdown(String label, String header, String tickerKeyPrefix, boolean multiSelect,
                                  GSRMultiSelectDropdownBehavior<M> behavior) {
        this.label = label;
        this.header = header;
        this.tickerKeyPrefix = tickerKeyPrefix;
        this.multiSelect = multiSelect;
        this.behavior = behavior;
    }

    /** Renders the trigger bar. */
    public void renderTrigger(M model, DrawContext context, TextRenderer textRenderer,
                              GSRTickerState tickerState, int sectionLeft, int sectionTop, int barTop,
                              int barWidth, int barHeight, float labelScale, boolean isOpen, boolean hovered) {
        if (label == null || behavior == null) return;
        int labelX = sectionLeft + GSRRunHistoryParameters.LIST_TEXT_INSET;
        int labelY = sectionTop + GSRRunHistoryParameters.LIST_VERTICAL_GAP;
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(labelX, labelY);
        matrices.scale(labelScale, labelScale);
        context.drawTextWithShadow(textRenderer, Text.literal(label), 0, 0, GSRRunHistoryParameters.LABEL_COLOR);
        matrices.popMatrix();
        int barLeft = sectionLeft + GSRRunHistoryParameters.CONTAINER_INSET;
        int barRight = sectionLeft + barWidth - GSRRunHistoryParameters.CONTAINER_INSET;
        int barDrawWidth = barRight - barLeft;
        boolean focused = isOpen || hovered;
        var texture = GSRPressableWidgetAccessor.gsr$getTextures().get(true, focused);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, barLeft, barTop, barDrawWidth, barHeight);
        int textLeft = barLeft + GSRRunHistoryParameters.LIST_TEXT_INSET;
        var selectedIndices = behavior.getSelectedIndices(model);
        int selectedIdx = selectedIndices.isEmpty() ? -1 : selectedIndices.iterator().next();
        ItemStack triggerIcon = selectedIdx >= 0 ? behavior.getItemIcon(model, selectedIdx) : ItemStack.EMPTY;
        Integer triggerTint = selectedIdx >= 0 ? behavior.getItemIconTint(model, selectedIdx) : null;
        if (!triggerIcon.isEmpty()) {
            int iconSize = GSRRunHistoryParameters.TRIGGER_ICON_SIZE;
            int iconMargin = GSRRunHistoryParameters.TRIGGER_ICON_MARGIN;
            int iconX = barLeft + iconMargin;
            int iconY = barTop + (barHeight - iconSize) / 2;
            drawScaledItem(context, triggerIcon, iconX, iconY, iconSize, iconMargin, triggerTint);
            textLeft = barLeft + iconMargin + iconSize + iconMargin + GSRRunHistoryParameters.DROPDOWN_ITEM_ICON_TEXT_GAP;
        }
        Integer labelColor = behavior.getDisplayLabelColor(model);
        /* Force full alpha: locator colors are 0xRRGGBB; without alpha text renders invisible. */
        int textColor = labelColor != null ? (labelColor & 0x00FFFFFF) | 0xFF000000 : GSRRunHistoryParameters.TEXT_COLOR;
        String fullLabel = behavior.getDisplayLabel(model);
        if (fullLabel.endsWith(DEFAULT_SUFFIX) && labelColor != null) {
            String prefix = fullLabel.substring(0, fullLabel.length() - DEFAULT_SUFFIX.length());
            int prefixColor = (labelColor & 0x00FFFFFF) | 0xFF000000;
            drawTriggerLabel(context, textRenderer, tickerState, textLeft, barTop, barRight, barTop + barHeight,
                    prefix, DEFAULT_SUFFIX, labelScale, hovered || isOpen, behavior.getSelectionTimeMs(model),
                    prefixColor, GSRRunHistoryParameters.TEXT_COLOR);
        } else {
            drawTriggerLabel(context, textRenderer, tickerState, textLeft, barTop, barRight, barTop + barHeight,
                    fullLabel, labelScale, hovered || isOpen, behavior.getSelectionTimeMs(model),
                    textColor);
        }
    }

    /** Draws an item with margin and scale inside the given bounds. When tint is non-null, draws a color mask overlay. */
    private static void drawScaledItem(DrawContext context, ItemStack stack, int x, int y, int size, int margin,
                                       Integer tint) {
        int inner = size - 2 * margin;
        if (inner <= 0) return;
        float scale = inner / 16f;
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x + margin, y + margin);
        matrices.scale(scale, scale);
        context.drawItem(stack, 0, 0);
        matrices.popMatrix();
        if (tint != null) {
            int tintAlpha = GSRRunHistoryParameters.DEFAULT_COLOR_ICON_TINT_ALPHA;
            int overlayColor = (tint & 0x00FFFFFF) | (tintAlpha << 24);
            context.fill(x + margin, y + margin, x + margin + inner, y + margin + inner, overlayColor);
        }
    }

    public boolean isBarHovered(int barLeft, int barTop, int barWidth, int barHeight, int mouseX, int mouseY) {
        return mouseX >= barLeft && mouseX < barLeft + barWidth && mouseY >= barTop && mouseY < barTop + barHeight;
    }

    public String getHeader() {
        return header;
    }

    /** Item count including Select All and Deselect All when multiSelect. */
    public int getItemCount(M model) {
        if (behavior == null) return 0;
        int n = behavior.getItems(model).size();
        return multiSelect ? n + 2 : n;
    }

    public void renderOverlay(M model, DrawContext context, TextRenderer textRenderer,
                              GSRTickerState tickerState, int listLeft, int listTop, int listBottom, int listWidth,
                              int scroll, int mouseX, int mouseY) {
        renderOverlay(model, context, textRenderer, tickerState, listLeft, listTop, listBottom, listWidth, scroll, mouseX, mouseY, listLeft + listWidth);
    }

    /**
     * Renders overlay with optional scrollbar position. When scrollbarX >= 0, draws scrollbar outside
     * the list (at scrollbarX) instead of inside; content uses full listWidth. Pass -1 for inside scrollbar.
     */
    public void renderOverlay(M model, DrawContext context, TextRenderer textRenderer,
                              GSRTickerState tickerState, int listLeft, int listTop, int listBottom, int listWidth,
                              int scroll, int mouseX, int mouseY, int scrollbarX) {
        if (behavior == null) return;
        List<String> items = behavior.getItems(model);
        Set<Integer> selectedIndices = behavior.getSelectedIndices(model);
        render(model, context, textRenderer, tickerState, listLeft, listTop, listBottom, listWidth,
                items, selectedIndices, scroll, mouseX, mouseY, scrollbarX);
    }

    private void drawTriggerLabel(DrawContext context, TextRenderer textRenderer, GSRTickerState tickerState,
                                  int textLeft, int barTop, int barRight, int barBottom, String labelText,
                                  float scale, boolean hoveredOrOpen, long selectionTimeMs, int textColor) {
        drawTriggerLabel(context, textRenderer, tickerState, textLeft, barTop, barRight, barBottom,
                labelText, null, scale, hoveredOrOpen, selectionTimeMs, textColor, textColor);
    }

    private void drawTriggerLabel(DrawContext context, TextRenderer textRenderer, GSRTickerState tickerState,
                                  int textLeft, int barTop, int barRight, int barBottom, String prefix, String suffix,
                                  float scale, boolean hoveredOrOpen, long selectionTimeMs, int prefixColor, int suffixColor) {
        String labelText = suffix != null ? prefix + suffix : prefix;
        int barWidth = barRight - textLeft;
        int margin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int innerLeft = textLeft;
        int innerRight = barRight - margin;
        int maxWidth = Math.max(1, (int) ((barWidth - margin) / scale));
        int textWidth = textRenderer.getWidth(labelText);
        int scaledFontHeight = (int) (textRenderer.fontHeight * scale);
        int textY = barTop + (barBottom - barTop - scaledFontHeight) / 2;

        context.enableScissor(innerLeft, barTop, innerRight, barBottom);
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(textLeft, textY);
        matrices.scale(scale, scale);
        if (textWidth > maxWidth) {
            if (hoveredOrOpen) {
                String loopingLabel = labelText + "    " + labelText;
                int loopWidth = textRenderer.getWidth(loopingLabel);
                int maxScroll = loopWidth / 2;
                long elapsed = tickerState.getElapsedMs(tickerKeyPrefix, System.currentTimeMillis());
                int scrollOffset = (int) ((elapsed / (double) GSRRunHistoryParameters.TICKER_CYCLE_MS) * maxScroll);
                matrices.translate(-scrollOffset / scale, 0);
                context.drawTextWithShadow(textRenderer, Text.literal(loopingLabel), 0, 0, prefixColor);
            } else {
                String truncated = truncateWithPeriod(textRenderer, labelText, maxWidth);
                context.drawTextWithShadow(textRenderer, Text.literal(truncated), 0, 0, prefixColor);
            }
        } else if (suffix != null) {
            int prefixWidth = textRenderer.getWidth(prefix);
            context.drawTextWithShadow(textRenderer, Text.literal(prefix), 0, 0, prefixColor);
            context.drawTextWithShadow(textRenderer, Text.literal(suffix), prefixWidth, 0, suffixColor);
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal(prefix), 0, 0, prefixColor);
        }
        matrices.popMatrix();
        context.disableScissor();
    }

    private static String truncateWithPeriod(TextRenderer textRenderer, String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        int periodWidth = textRenderer.getWidth(".");
        int availableWidth = maxWidth - periodWidth;
        for (int len = text.length(); len > 0; len--) {
            String s = text.substring(0, len);
            if (textRenderer.getWidth(s) <= availableWidth) return s + ".";
        }
        return ".";
    }

    private void render(M model, DrawContext context, TextRenderer textRenderer, GSRTickerState tickerState,
                        int listLeft, int listTop, int listBottom, int listWidth,
                        List<String> items, Set<Integer> selectedIndices, int scroll,
                        int mouseX, int mouseY, int scrollbarX) {
        if (items.isEmpty()) return;

        Set<Integer> effectiveSelection = multiSelect ? selectedIndices
                : (selectedIndices.isEmpty() ? Set.of() : Set.of(selectedIndices.iterator().next()));

        int ddHeaderMaxWidth = listWidth - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET;
        int headerHeight = drawHeader(context, textRenderer, header,
                listLeft + GSRRunHistoryParameters.LIST_TEXT_INSET,
                listTop + GSRRunHistoryParameters.LIST_VERTICAL_GAP, ddHeaderMaxWidth);
        int overlayListTop = listTop + GSRRunHistoryParameters.LIST_VERTICAL_GAP + headerHeight + GSRRunHistoryParameters.LIST_VERTICAL_GAP;
        int listAreaBottom = listBottom;
        int listAreaHeight = listAreaBottom - overlayListTop;

        int displayCount = items.size() + (multiSelect ? 2 : 0);
        int ddContentH = displayCount * GSRRunHistoryParameters.ROW_HEIGHT + 8;
        int maxScroll = Math.max(0, ddContentH - listAreaHeight + GSRRunHistoryParameters.BOTTOM_SCROLL_PADDING);
        int clampedScroll = Math.min(scroll, maxScroll);

        boolean scrollbarInGap = scrollbarX >= 0 && maxScroll > 0;
        int itemWidth = listWidth - 2 * GSRRunHistoryParameters.CONTAINER_INSET
                - (maxScroll > 0 && !scrollbarInGap ? GSRScrollbarHelper.getScrollbarWidth() : 0);
        int rowHeight = GSRRunHistoryParameters.ROW_HEIGHT - GSRRunHistoryParameters.LIST_VERTICAL_GAP;
        var textures = GSRPressableWidgetAccessor.gsr$getTextures();
        context.enableScissor(listLeft, overlayListTop, listLeft + listWidth, listAreaBottom);
        int rowY = overlayListTop + 4 - clampedScroll;
        for (int i = 0; i < displayCount; i++) {
            if (rowY + GSRRunHistoryParameters.ROW_HEIGHT > overlayListTop && rowY < listAreaBottom) {
                boolean isSelectAll = multiSelect && i == GSRRunHistoryParameters.MULTISELECT_SELECT_ALL_INDEX;
                boolean isDeselectAll = multiSelect && i == GSRRunHistoryParameters.MULTISELECT_DESELECT_ALL_INDEX;
                int dataIndex = multiSelect ? (i - GSRRunHistoryParameters.MULTISELECT_DATA_START_INDEX) : i;
                boolean selected = !isSelectAll && !isDeselectAll && dataIndex >= 0 && effectiveSelection.contains(dataIndex);
                boolean hovered = mouseX >= listLeft && mouseX < listLeft + listWidth
                        && mouseY >= rowY && mouseY < rowY + GSRRunHistoryParameters.ROW_HEIGHT;
                boolean focused = selected || hovered;
                var rowTexture = textures.get(true, focused);
                int rowLeft = listLeft + GSRRunHistoryParameters.CONTAINER_INSET;
                int rowDrawWidth = listLeft + listWidth - GSRRunHistoryParameters.CONTAINER_INSET - rowLeft;
                int itemRight = rowLeft + itemWidth;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, rowTexture, rowLeft, rowY, rowDrawWidth, rowHeight);
                ItemStack itemIcon = !isSelectAll && !isDeselectAll ? behavior.getItemIcon(model, dataIndex) : ItemStack.EMPTY;
                Integer itemTint = !isSelectAll && !isDeselectAll ? behavior.getItemIconTint(model, dataIndex) : null;
                int textLeft = rowLeft + GSRRunHistoryParameters.LIST_TEXT_INSET;
                if (!itemIcon.isEmpty()) {
                    int iconSize = GSRRunHistoryParameters.DROPDOWN_ITEM_ICON_SIZE;
                    int iconMargin = GSRRunHistoryParameters.DROPDOWN_ITEM_ICON_MARGIN;
                    int iconX = rowLeft + GSRRunHistoryParameters.LIST_TEXT_INSET;
                    int iconY = rowY + (rowHeight - iconSize) / 2;
                    drawScaledItem(context, itemIcon, iconX, iconY, iconSize, iconMargin, itemTint);
                    int iconTotalWidth = iconSize + GSRRunHistoryParameters.DROPDOWN_ITEM_ICON_TEXT_GAP;
                    textLeft = rowLeft + GSRRunHistoryParameters.LIST_TEXT_INSET + iconTotalWidth;
                }
                String label;
                if (isSelectAll) {
                    label = GSRRunHistoryParameters.SELECT_ALL_LABEL;
                } else if (isDeselectAll) {
                    label = GSRRunHistoryParameters.DESELECT_ALL_LABEL;
                } else {
                    String icon = itemIcon.isEmpty() ? (selected ? ICON_SELECTED : ICON_UNSELECTED) : "";
                    label = icon + items.get(dataIndex);
                }
                drawItemLabel(context, textRenderer, tickerState, tickerKeyPrefix + "-list", label,
                        textLeft, itemRight, rowY, rowY + rowHeight, GSRRunHistoryParameters.TEXT_COLOR, hovered);
            }
            rowY += GSRRunHistoryParameters.ROW_HEIGHT;
        }
        context.disableScissor();

        if (maxScroll > 0) {
            int sbWidth = GSRScrollbarHelper.getScrollbarWidth();
            int trackX = scrollbarInGap ? scrollbarX : listLeft + listWidth - sbWidth;
            GSRScrollbarHelper.drawScrollbar(context, trackX, overlayListTop, listAreaBottom - overlayListTop,
                    clampedScroll, maxScroll, GSRRunHistoryParameters.SCROLLBAR_MIN_THUMB_HEIGHT);
        }
    }

    private int drawHeader(DrawContext context, TextRenderer textRenderer, String text, int left, int top, int maxWidth) {
        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(text), Math.max(1, maxWidth));
        int y = top;
        for (OrderedText line : lines) {
            context.drawText(textRenderer, line, left, y, GSRRunHistoryParameters.LABEL_COLOR, false);
            y += textRenderer.fontHeight;
        }
        return lines.size() * textRenderer.fontHeight;
    }

    private void drawItemLabel(DrawContext context, TextRenderer textRenderer, GSRTickerState tickerState, String tickerKey,
                              String fullLabel, int itemLeft, int itemRight, int rowTop, int rowBottom, int color, boolean hovered) {
        int itemWidth = itemRight - itemLeft;
        int margin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int innerLeft = itemLeft + margin;
        int innerRight = itemRight - margin;
        int textLeft = itemLeft + GSRRunHistoryParameters.LIST_TEXT_INSET;
        int maxWidth = Math.max(1, itemWidth - 2 * margin - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET);
        int textWidth = textRenderer.getWidth(fullLabel);
        int rowHeight = rowBottom - rowTop;

        context.enableScissor(innerLeft, rowTop, innerRight, rowBottom);
        if (textWidth > maxWidth) {
            int textY = rowTop + (rowHeight - textRenderer.fontHeight) / 2;
            if (hovered) {
                String loopingLabel = fullLabel + "    " + fullLabel;
                int loopWidth = textRenderer.getWidth(loopingLabel);
                int maxScroll = loopWidth / 2;
                long elapsed = tickerState.getElapsedMs(tickerKey, System.currentTimeMillis());
                int scrollOffset = (int) ((elapsed / (double) GSRRunHistoryParameters.TICKER_CYCLE_MS) * maxScroll);
                context.drawTextWithShadow(textRenderer, Text.literal(loopingLabel), textLeft - scrollOffset, textY, color);
            } else {
                String truncated = truncateWithPeriod(textRenderer, fullLabel, maxWidth);
                context.drawTextWithShadow(textRenderer, Text.literal(truncated), textLeft, textY, color);
            }
        } else {
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(fullLabel), maxWidth);
            if (!lines.isEmpty()) {
                int maxLines = Math.max(1, (rowHeight - GSRRunHistoryParameters.LIST_VERTICAL_GAP) / textRenderer.fontHeight);
                int linesToDraw = Math.min(lines.size(), maxLines);
                int totalHeight = linesToDraw * textRenderer.fontHeight;
                int startY = rowTop + (rowHeight - totalHeight) / 2;
                for (int i = 0; i < linesToDraw; i++) {
                    context.drawTextWithShadow(textRenderer, lines.get(i), textLeft, startY + i * textRenderer.fontHeight, color);
                }
            }
        }
        context.disableScissor();
    }

    /** Geometry for scrollbar and confirm button hit testing. */
    public record DropdownGeometry(
            int listTop,
            int listBottom,
            int listAreaHeight,
            int maxScroll,
            int confirmButtonTop,
            int overlayListBottom
    ) {
        public boolean isConfirmButtonAt(int listLeft, int listWidth, int mouseX, int mouseY) {
            int confirmBottom = confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT;
            return mouseX >= listLeft && mouseX < listLeft + listWidth && mouseY >= confirmButtonTop && mouseY < confirmBottom;
        }
    }

    public static DropdownGeometry computeGeometry(TextRenderer textRenderer, String header, int listLeft, int listTop, int listBottom, int listWidth, int itemCount) {
        int ddHeaderMaxWidth = listWidth - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET;
        int headerHeight = textRenderer.wrapLines(Text.literal(header), Math.max(1, ddHeaderMaxWidth)).size() * textRenderer.fontHeight;
        int overlayListTop = listTop + GSRRunHistoryParameters.LIST_VERTICAL_GAP + headerHeight + GSRRunHistoryParameters.LIST_VERTICAL_GAP;
        int listAreaBottom = listBottom;
        int confirmButtonTop = listBottom + GSRRunHistoryParameters.SELECTION_DELIMITER_BUTTON_GAP;
        int overlayListBottom = listBottom;
        int listAreaHeight = listAreaBottom - overlayListTop;
        int ddContentH = itemCount * GSRRunHistoryParameters.ROW_HEIGHT + 8;
        int maxScroll = Math.max(0, ddContentH - listAreaHeight + GSRRunHistoryParameters.BOTTOM_SCROLL_PADDING);
        return new DropdownGeometry(overlayListTop, listAreaBottom, listAreaHeight, maxScroll, confirmButtonTop, overlayListBottom);
    }

    public static int getItemIndexAt(DropdownGeometry geom, int itemCount, int scroll, int listLeft, int listWidth, int mouseX, int mouseY) {
        if (mouseX < listLeft || mouseX >= listLeft + listWidth || mouseY < geom.listTop() || mouseY >= geom.listBottom()) {
            return -1;
        }
        int clampedScroll = Math.min(scroll, geom.maxScroll());
        int rowY = geom.listTop() + 4 - clampedScroll;
        for (int i = 0; i < itemCount; i++) {
            if (mouseY >= rowY && mouseY < rowY + GSRRunHistoryParameters.ROW_HEIGHT) {
                return i;
            }
            rowY += GSRRunHistoryParameters.ROW_HEIGHT;
        }
        return -1;
    }

    public static int scrollFromMouseY(double mouseY, int trackTop, int trackBottom, int thumbHeight, int maxScroll) {
        int trackHeight = trackBottom - trackTop;
        int thumbRange = trackHeight - thumbHeight;
        if (thumbRange <= 0 || maxScroll <= 0) return 0;
        double frac = (mouseY - trackTop - thumbHeight / 2.0) / thumbRange;
        return (int) Math.max(0, Math.min(maxScroll, frac * maxScroll));
    }
}
