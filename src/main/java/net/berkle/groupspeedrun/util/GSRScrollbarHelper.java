package net.berkle.groupspeedrun.util;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.berkle.groupspeedrun.mixin.accessors.GSRScrollableWidgetAccessor;

/**
 * Draws scrollbars and scroll-edge fades. Scrollbars match Minecraft's keybinds screen style.
 */
public final class GSRScrollbarHelper {

    private static final Identifier FALLBACK_TRACK = Identifier.of("minecraft", "widget/scroller_background");
    private static final Identifier FALLBACK_THUMB = Identifier.of("minecraft", "widget/scroller");
    private static final int FALLBACK_WIDTH = 6;

    /** Extra pixels for scrollbar hit area on each side of the track. Makes scrolling easier to click. */
    public static final int SCROLLBAR_HIT_EXTEND = 5;

    private GSRScrollbarHelper() {}

    /**
     * Returns true if mouseX is within the scrollbar hit area (track plus extend on each side).
     * Use for hit testing so users can more easily click to scroll.
     */
    public static boolean isInScrollbarHitArea(int mouseX, int trackX, int trackWidth) {
        return mouseX >= trackX - SCROLLBAR_HIT_EXTEND && mouseX < trackX + trackWidth + SCROLLBAR_HIT_EXTEND;
    }

    /**
     * Returns true if mouseY is within the horizontal scrollbar hit area (track plus extend above/below).
     * Use for hit testing so users can more easily click to scroll.
     */
    public static boolean isInHorizontalScrollbarHitArea(int mouseY, int trackY, int trackHeight) {
        return mouseY >= trackY - SCROLLBAR_HIT_EXTEND && mouseY < trackY + trackHeight + SCROLLBAR_HIT_EXTEND;
    }

    /**
     * Draws alpha fade gradients at top and bottom of scrollable content when content overflows.
     * Top fade only when scroll &gt; 0; bottom fade only when scroll &lt; maxScroll.
     *
     * @param context   Draw context.
     * @param left      Left edge of content area.
     * @param top       Top of visible content area.
     * @param right     Right edge of content area.
     * @param bottom    Bottom of visible content area.
     * @param scroll    Current scroll offset (0 when at top).
     * @param maxScroll Maximum scroll (positive when content overflows).
     * @param fadeHeight Height of each fade in pixels.
     * @param bgColor   Background color (ARGB) for the fade. Use container/screen bg.
     */
    public static void drawScrollFade(DrawContext context, int left, int top, int right, int bottom,
                                     int scroll, int maxScroll, int fadeHeight, int bgColor) {
        if (maxScroll <= 0) return;
        int rgb = bgColor & 0x00FFFFFF;
        int opaque = rgb | 0xFF000000;
        int transparent = rgb & 0x00FFFFFF;
        if (scroll > 0 && fadeHeight > 0) {
            int fadeBottom = Math.min(top + fadeHeight, bottom);
            if (fadeBottom > top) {
                context.fillGradient(left, top, right, fadeBottom, transparent, opaque);
            }
        }
        if (scroll < maxScroll && fadeHeight > 0) {
            int fadeTop = Math.max(bottom - fadeHeight, top);
            if (bottom > fadeTop) {
                context.fillGradient(left, fadeTop, right, bottom, opaque, transparent);
            }
        }
    }

    /** Returns the vanilla scrollbar width for layout. Uses accessor when available. */
    public static int getScrollbarWidth() {
        try {
            return GSRScrollableWidgetAccessor.gsr$getScrollbarWidth();
        } catch (Throwable t) {
            return FALLBACK_WIDTH;
        }
    }

    /**
     * Draws a vertical scrollbar matching the keybinds screen style.
     *
     * @param context draw context
     * @param trackX left edge of scrollbar track
     * @param trackTop top of track (y)
     * @param trackHeight height of track
     * @param scroll current scroll offset (0 or positive)
     * @param maxScroll maximum scroll (positive when content overflows)
     * @param minThumbHeight minimum thumb height in pixels
     */
    public static void drawScrollbar(DrawContext context, int trackX, int trackTop, int trackHeight,
            int scroll, int maxScroll, int minThumbHeight) {
        if (maxScroll <= 0) return;
        Identifier trackTex;
        Identifier thumbTex;
        int width = getScrollbarWidth();
        try {
            trackTex = GSRScrollableWidgetAccessor.gsr$getScrollerBackgroundTexture();
            thumbTex = GSRScrollableWidgetAccessor.gsr$getScrollerTexture();
        } catch (Throwable t) {
            trackTex = FALLBACK_TRACK;
            thumbTex = FALLBACK_THUMB;
        }
        int thumbHeight = Math.max(minThumbHeight, (int) (trackHeight * (trackHeight / (double) (trackHeight + maxScroll))));
        int thumbY = trackTop + (int) ((trackHeight - thumbHeight) * (scroll / (double) maxScroll));
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, trackTex, trackX, trackTop, width, trackHeight);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, thumbTex, trackX, thumbY, width, thumbHeight);
    }

    /**
     * Draws a horizontal scrollbar below content (track + thumb, left-to-right).
     *
     * @param context draw context
     * @param trackLeft left edge of track
     * @param trackTop top of track (y)
     * @param trackWidth width of track
     * @param scroll current scroll offset (0 or positive)
     * @param maxScroll maximum scroll (positive when content overflows)
     * @param minThumbWidth minimum thumb width in pixels
     */
    public static void drawHorizontalScrollbar(DrawContext context, int trackLeft, int trackTop, int trackWidth,
            int scroll, int maxScroll, int minThumbWidth) {
        if (maxScroll <= 0) return;
        Identifier trackTex;
        Identifier thumbTex;
        int height = getScrollbarWidth();
        try {
            trackTex = GSRScrollableWidgetAccessor.gsr$getScrollerBackgroundTexture();
            thumbTex = GSRScrollableWidgetAccessor.gsr$getScrollerTexture();
        } catch (Throwable t) {
            trackTex = FALLBACK_TRACK;
            thumbTex = FALLBACK_THUMB;
        }
        int thumbWidth = Math.max(minThumbWidth, (int) (trackWidth * (trackWidth / (double) (trackWidth + maxScroll))));
        int thumbX = trackLeft + (int) ((trackWidth - thumbWidth) * (scroll / (double) maxScroll));
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, trackTex, trackLeft, trackTop, trackWidth, height);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, thumbTex, thumbX, trackTop, thumbWidth, height);
    }

    /**
     * Maps mouse X to scroll value for horizontal scrollbar (thumb center follows mouse).
     *
     * @param mouseX Mouse X position.
     * @param trackLeft Left edge of track.
     * @param trackRight Right edge of track (trackLeft + trackWidth).
     * @param thumbWidth Thumb width in pixels.
     * @param maxScroll Maximum scroll value.
     * @return Scroll value in [0, maxScroll].
     */
    public static int scrollFromMouseX(double mouseX, int trackLeft, int trackRight, int thumbWidth, int maxScroll) {
        int trackWidth = trackRight - trackLeft;
        int thumbRange = trackWidth - thumbWidth;
        if (thumbRange <= 0 || maxScroll <= 0) return 0;
        double frac = (mouseX - trackLeft - thumbWidth / 2.0) / thumbRange;
        return (int) Math.max(0, Math.min(maxScroll, frac * maxScroll));
    }
}
