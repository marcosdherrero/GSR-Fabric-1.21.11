package net.berkle.groupspeedrun.gui;

import net.berkle.groupspeedrun.parameter.GSRTooltipParameters;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

/**
 * Standardized tooltip rendering for the entire GSR project.
 * Provides: 35% max screen size, 2.5s scroll delay, cyclical vertical scroll,
 * horizontal divider bar between content and repeat, and render-on-top behavior.
 */
public final class GSRStandardTooltip {

    private GSRStandardTooltip() {}

    /**
     * Draws a standardized tooltip from FormattedCharSequence lines.
     *
     * @param context Draw context.
     * @param textRenderer Component renderer.
     * @param lines Tooltip lines (may be wrapped if they exceed max width).
     * @param positioner Positions the tooltip on screen.
     * @param mouseX Mouse X.
     * @param mouseY Mouse Y.
     * @param screenWidth Screen width.
     * @param screenHeight Screen height.
     * @param elapsedMs Elapsed ms in scroll cycle (from state.getTooltipElapsedMs(key, now)).
     */
    public static void draw(GuiGraphicsExtractor context, Font textRenderer,
                            List<FormattedCharSequence> lines,
                            ClientTooltipPositioner positioner,
                            int mouseX, int mouseY,
                            int screenWidth, int screenHeight,
                            long elapsedMs) {
        if (lines == null || lines.isEmpty()) return;

        int maxBoxW = Math.max(GSRTooltipParameters.MIN_BOX_WIDTH, (int) (screenWidth * GSRTooltipParameters.MAX_SCREEN_FRACTION));
        int maxBoxH = Math.max(GSRTooltipParameters.MIN_BOX_HEIGHT, (int) (screenHeight * GSRTooltipParameters.MAX_SCREEN_FRACTION));
        int padding = GSRTooltipParameters.PADDING;
        int maxContentW = Math.max(1, maxBoxW - 2 * padding);
        int maxContentH = Math.max(1, maxBoxH - 2 * padding);

        List<FormattedCharSequence> wrapped = wrapLines(textRenderer, lines, maxContentW);
        if (wrapped.isEmpty()) return;

        int maxLineWidth = 0;
        for (FormattedCharSequence ot : wrapped) {
            maxLineWidth = Math.max(maxLineWidth, textRenderer.width(ot));
        }

        float scale = maxLineWidth > maxContentW ? (float) maxContentW / maxLineWidth : 1.0f;
        int lineHeightPx = (int) (textRenderer.lineHeight * scale);
        int contentHeightPx = wrapped.size() * lineHeightPx;

        int tooltipW = (int) (maxLineWidth * scale) + 2 * padding;
        int tooltipH = Math.min(maxBoxH, contentHeightPx + 2 * padding);
        if (contentHeightPx > maxContentH) {
            tooltipH = maxBoxH;
        }

        Vector2ic pos = positioner.positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipW, tooltipH);
        int tooltipX = pos.x();
        int tooltipY = pos.y();
        int edge = GSRTooltipParameters.EDGE_MARGIN;
        tooltipX = Math.max(edge, Math.min(tooltipX, screenWidth - tooltipW - edge));
        tooltipY = Math.max(edge, Math.min(tooltipY, screenHeight - tooltipH - edge));

        drawTooltipBox(context, tooltipX, tooltipY, tooltipW, tooltipH);

        int innerLeft = tooltipX + padding;
        int innerTop = tooltipY + padding;
        int innerRight = tooltipX + tooltipW - padding;
        int innerBottom = tooltipY + tooltipH - padding;
        int innerHeight = innerBottom - innerTop;
        context.enableScissor(innerLeft, innerTop, innerRight, innerBottom);

        int scrollRange = Math.max(0, contentHeightPx - innerHeight);
        int scrollOffset = computeScrollOffset(elapsedMs, scrollRange, contentHeightPx);

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(innerLeft, innerTop - scrollOffset);
        matrices.scale(scale, scale);

        int y = 0;
        for (FormattedCharSequence ot : wrapped) {
            context.text(textRenderer, ot, 0, y, GSRTooltipParameters.TEXT_COLOR, false);
            y += textRenderer.lineHeight;
        }
        if (scrollRange > 0) {
            for (FormattedCharSequence ot : wrapped) {
                context.text(textRenderer, ot, 0, y, GSRTooltipParameters.TEXT_COLOR, false);
                y += textRenderer.lineHeight;
            }
        }

        matrices.popMatrix();

        if (scrollRange > 0) {
            int dividerY = innerTop - scrollOffset + contentHeightPx;
            if (dividerY >= innerTop && dividerY < innerBottom) {
                int m = GSRTooltipParameters.SCROLL_DIVIDER_MARGIN;
                int dLeft = innerLeft + m;
                int dRight = Math.max(dLeft, innerRight - m);
                context.fill(dLeft, dividerY, dRight, dividerY + 1, GSRTooltipParameters.BORDER);
            }
        }

        context.disableScissor();
    }

    /** Positioner for cursor-offset tooltips (e.g. chart bars). */
    public static final ClientTooltipPositioner CURSOR_OFFSET = (sw, sh, mx, my, tw, th) -> {
        int offset = GSRTooltipParameters.CURSOR_OFFSET;
        int edge = GSRTooltipParameters.EDGE_MARGIN;
        int tx = mx + offset;
        int ty = my + offset;
        if (tx + tw > sw) tx = mx - tw - edge;
        if (ty + th > sh) ty = my - th - edge;
        return new Vector2i(Math.max(edge, tx), Math.max(edge, ty));
    };

    /**
     * Draws a standardized tooltip from String lines (e.g. chart bar tooltips).
     */
    public static void drawFromStrings(GuiGraphicsExtractor context, Font textRenderer,
                                       List<String> lines,
                                       ClientTooltipPositioner positioner,
                                       int mouseX, int mouseY,
                                       int screenWidth, int screenHeight,
                                       long elapsedMs) {
        if (lines == null || lines.isEmpty()) return;
        int maxBoxW = Math.max(GSRTooltipParameters.MIN_BOX_WIDTH, (int) (screenWidth * GSRTooltipParameters.MAX_SCREEN_FRACTION));
        int maxContentW = Math.max(1, maxBoxW - 2 * GSRTooltipParameters.PADDING);
        List<FormattedCharSequence> ordered = new ArrayList<>();
        for (String line : lines) {
            if (line == null) continue;
            for (FormattedCharSequence ot : textRenderer.split(Component.literal(line), maxContentW)) {
                ordered.add(ot);
            }
        }
        draw(context, textRenderer, ordered, positioner, mouseX, mouseY, screenWidth, screenHeight, elapsedMs);
    }

    /**
     * Draws a tooltip with player face and info. Convenience when no highlighted value; uses stats row and scores row.
     */
    public static void drawWithPlayerHeader(GuiGraphicsExtractor context, Font textRenderer,
                                           PlayerSkin skin, String playerName, String statsRow,
                                           String scoresRow,
                                           ClientTooltipPositioner positioner,
                                           int mouseX, int mouseY,
                                           int screenWidth, int screenHeight,
                                           long elapsedMs) {
        drawWithPlayerChartTooltip(context, textRenderer, skin, playerName, null, statsRow, scoresRow,
                positioner, mouseX, mouseY, screenWidth, screenHeight, elapsedMs);
    }

    /**
     * Draws a tooltip with: face+name, highlighted value (from hovered bar), stats row, scores row.
     * Stats and scores rows scroll horizontally when they overflow. No mini chart bar.
     *
     * @param highlightedValueLine Stat label + value + unit for the hovered bar (e.g. "Run Time: 12:34"); null to skip.
     * @param statsRow Single line for runs/win/loss/avg; scrolls horizontally when wider than tooltip.
     * @param scoresRow Single line of run values (icon+value per run); scrolls horizontally when wider.
     */
    public static void drawWithPlayerChartTooltip(GuiGraphicsExtractor context, Font textRenderer,
                                                  PlayerSkin skin, String playerName, String highlightedValueLine,
                                                  String statsRow, String scoresRow,
                                                  ClientTooltipPositioner positioner,
                                                  int mouseX, int mouseY,
                                                  int screenWidth, int screenHeight,
                                                  long elapsedMs) {
        if (playerName == null || playerName.isEmpty()) return;

        int padding = GSRTooltipParameters.PADDING;
        int faceSize = GSRTooltipParameters.PLAYER_FACE_SIZE;
        int faceGap = GSRTooltipParameters.PLAYER_FACE_NAME_GAP;
        int headerMargin = GSRTooltipParameters.PLAYER_HEADER_MARGIN;
        int statsRowGap = GSRTooltipParameters.PLAYER_STATS_ROW_GAP;
        int scoresGap = GSRTooltipParameters.PLAYER_SCORES_GAP;
        int maxBoxW = Math.max(GSRTooltipParameters.MIN_BOX_WIDTH, (int) (screenWidth * GSRTooltipParameters.PLAYER_TOOLTIP_MAX_WIDTH_FRACTION));
        int maxBoxH = Math.max(GSRTooltipParameters.MIN_BOX_HEIGHT, (int) (screenHeight * GSRTooltipParameters.MAX_SCREEN_FRACTION));
        if (GSRTooltipParameters.PLAYER_TOOLTIP_SWAP_ASPECT) {
            int swap = maxBoxW;
            maxBoxW = maxBoxH;
            maxBoxH = swap;
        }
        int maxContentW = Math.max(1, maxBoxW - 2 * padding);
        int lineHeight = textRenderer.lineHeight;

        int headerContentHeight = (skin != null) ? Math.max(faceSize, lineHeight) : lineHeight;
        int headerHeight = (skin != null) ? headerContentHeight + 2 * headerMargin : lineHeight;
        int highlightedH = (highlightedValueLine != null && !highlightedValueLine.isEmpty()) ? lineHeight + statsRowGap : 0;
        int statsRowH = (statsRow != null && !statsRow.isEmpty()) ? lineHeight + statsRowGap : 0;
        int scoresRowH = (scoresRow != null && !scoresRow.isEmpty()) ? lineHeight + scoresGap : 0;
        int staticSectionH = headerHeight + highlightedH + statsRowH + scoresRowH;

        int tooltipW = Math.max((int) (textRenderer.width(playerName) + 2 * padding), maxContentW);
        tooltipW = Math.min(tooltipW, maxBoxW);
        int tooltipH = Math.min(maxBoxH, staticSectionH + 2 * padding);

        Vector2ic pos = positioner.positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipW, tooltipH);
        int tooltipX = pos.x();
        int tooltipY = pos.y();
        int edge = GSRTooltipParameters.EDGE_MARGIN;
        tooltipX = Math.max(edge, Math.min(tooltipX, screenWidth - tooltipW - edge));
        tooltipY = Math.max(edge, Math.min(tooltipY, screenHeight - tooltipH - edge));

        drawTooltipBox(context, tooltipX, tooltipY, tooltipW, tooltipH);

        int innerLeft = tooltipX + padding;
        int innerTop = tooltipY + padding;
        int innerRight = tooltipX + tooltipW - padding;
        int innerBottom = tooltipY + tooltipH - padding;
        context.enableScissor(innerLeft, innerTop, innerRight, innerBottom);

        int y = innerTop;

        int headerTop = y;
        if (skin != null) {
            int faceX = innerLeft + headerMargin;
            int faceY = headerTop + headerMargin;
            PlayerFaceExtractor.extractRenderState(context, skin, faceX, faceY, faceSize);
            int nameX = faceX + faceSize + faceGap;
            int nameY = headerTop + headerMargin + (headerContentHeight - lineHeight) / 2;
            context.text(textRenderer, playerName, nameX, nameY, GSRTooltipParameters.TEXT_COLOR, false);
        } else {
            context.text(textRenderer, playerName, innerLeft, headerTop, GSRTooltipParameters.TEXT_COLOR, false);
        }
        y += headerHeight;

        if (highlightedValueLine != null && !highlightedValueLine.isEmpty()) {
            context.text(textRenderer, highlightedValueLine, innerLeft, y, GSRTooltipParameters.TEXT_COLOR, false);
            y += lineHeight + statsRowGap;
        }

        if (statsRow != null && !statsRow.isEmpty()) {
            drawHorizontalTickerRow(context, textRenderer, statsRow, innerLeft, innerRight, y, lineHeight, elapsedMs);
            y += lineHeight + statsRowGap;
            context.enableScissor(innerLeft, innerTop, innerRight, innerBottom);
        }

        if (scoresRow != null && !scoresRow.isEmpty()) {
            y += scoresGap;
            drawHorizontalTickerRow(context, textRenderer, scoresRow, innerLeft, innerRight, y, lineHeight, elapsedMs);
        }

        context.disableScissor();
    }

    /**
     * Draws a single row that scrolls horizontally like a news ticker when it overflows.
     * Both rows use the same scroll speed. Content repeats with " | " separator.
     * Scrolls the full segment (content + separator) before wrapping; restart aligns when text
     * returns to the beginning. Draws enough copies to cover the scroll range.
     */
    private static void drawHorizontalTickerRow(GuiGraphicsExtractor context, Font textRenderer,
                                                String text, int innerLeft, int innerRight, int y, int lineHeight,
                                                long elapsedMs) {
        int innerWidth = innerRight - innerLeft;
        int contentWidth = textRenderer.width(text);
        int separatorWidth = textRenderer.width(GSRTooltipParameters.TICKER_SEPARATOR);
        int segmentWidth = contentWidth + separatorWidth;
        boolean overflow = contentWidth > innerWidth;
        int scrollOffset = 0;
        if (overflow && segmentWidth > 0) {
            long afterDelay = Math.max(0, elapsedMs - GSRTooltipParameters.SCROLL_START_DELAY_MS);
            float scrolledPx = afterDelay * GSRTooltipParameters.TICKER_SPEED_PX_PER_MS;
            scrollOffset = (int) (scrolledPx % segmentWidth);
        }
        context.enableScissor(innerLeft, y, innerRight, y + lineHeight);
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(innerLeft - scrollOffset, y);
        if (overflow) {
            int totalNeeded = scrollOffset + innerWidth;
            int x = 0;
            while (x < totalNeeded) {
                context.text(textRenderer, text, x, 0, GSRTooltipParameters.TEXT_COLOR, false);
                x += contentWidth;
                if (x < totalNeeded) {
                    context.text(textRenderer, GSRTooltipParameters.TICKER_SEPARATOR, x, 0, GSRTooltipParameters.TEXT_COLOR, false);
                    x += separatorWidth;
                }
            }
        } else {
            context.text(textRenderer, text, 0, 0, GSRTooltipParameters.TEXT_COLOR, false);
        }
        matrices.popMatrix();
        context.disableScissor();
    }

    /** Draws the tooltip box (background and border). Renders on top via depth. */
    private static void drawTooltipBox(GuiGraphicsExtractor context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, GSRTooltipParameters.BG);
        context.fill(x, y, x + w, y + 1, GSRTooltipParameters.BORDER);
        context.fill(x, y + h - 1, x + w, y + h, GSRTooltipParameters.BORDER);
        context.fill(x, y, x + 1, y + h, GSRTooltipParameters.BORDER);
        context.fill(x + w - 1, y, x + w, y + h, GSRTooltipParameters.BORDER);
    }

    private static int computeScrollOffset(long elapsedMs, int scrollRange, int contentHeightPx) {
        if (scrollRange <= 0) return 0;
        if (elapsedMs < GSRTooltipParameters.SCROLL_START_DELAY_MS) return 0;
        int scrollDuration = GSRTooltipParameters.SCROLL_CYCLE_MS - GSRTooltipParameters.SCROLL_START_DELAY_MS;
        return Math.min(contentHeightPx, (int) (((elapsedMs - GSRTooltipParameters.SCROLL_START_DELAY_MS) / (double) scrollDuration) * contentHeightPx));
    }

    private static List<FormattedCharSequence> wrapLines(Font textRenderer, List<FormattedCharSequence> lines, int maxContentW) {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (FormattedCharSequence ot : lines) {
            if (ot == null) continue;
            int w = textRenderer.width(ot);
            if (w <= maxContentW) {
                wrapped.add(ot);
            } else {
                String plain = orderedTextToPlainString(ot);
                for (FormattedCharSequence sub : textRenderer.split(FormattedText.plain(plain), maxContentW)) {
                    wrapped.add(sub);
                }
            }
        }
        return wrapped;
    }

    /** Extracts plain text from FormattedCharSequence. Avoids FormattedText cast (lambdas are not FormattedText). */
    private static String orderedTextToPlainString(FormattedCharSequence ot) {
        StringBuilder sb = new StringBuilder();
        ot.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return TextVisitFactory.removeFormattingCodes(FormattedText.plain(sb.toString()));
    }

    /** Builds a stable key from tooltip content for scroll state. Never casts to FormattedText. */
    public static String buildTooltipKey(List<FormattedCharSequence> lines) {
        StringBuilder sb = new StringBuilder();
        for (FormattedCharSequence ot : lines) {
            if (ot != null) {
                sb.append(orderedTextToPlainString(ot));
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
