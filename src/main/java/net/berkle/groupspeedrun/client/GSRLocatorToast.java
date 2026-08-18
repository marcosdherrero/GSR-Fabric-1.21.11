package net.berkle.groupspeedrun.client;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

/**
 * Advancement-style toast for locator misses, matching Fair Structure Loot's chest-break toast
 * (ToastManager + toast/advancement sprite), not a hover tooltip.
 */
public final class GSRLocatorToast implements Toast {

    public static final Object TOKEN = new Object();

    private static final Identifier BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "toast/advancement");
    private static final int HORIZONTAL_PADDING = 6;
    private static final float TEXT_SCALE = 0.75f;
    private static final int LINE_GAP = 2;
    private static final int VERTICAL_PADDING = 8;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int BODY_COLOR = 0xFFD8D8D8;
    private static final int DISPLAY_TIME_MS = 8000;

    private Component title;
    private Component body;
    private List<FormattedCharSequence> wrappedBody;
    private int toastHeight = SLOT_HEIGHT;
    private Toast.Visibility wantedVisibility = Toast.Visibility.SHOW;
    private long lastShownAt = -1L;
    private boolean restartRequested;

    public GSRLocatorToast(Font font, String titleText, String bodyText) {
        applyCopy(font, titleText, bodyText);
    }

    public static void show(Minecraft client, String titleText, String bodyText) {
        if (client == null) return;
        ToastManager manager = client.getToastManager();
        GSRLocatorToast existing = manager.getToast(GSRLocatorToast.class, TOKEN);
        if (existing != null) {
            existing.reset(client.font, titleText, bodyText);
            return;
        }
        manager.addToast(new GSRLocatorToast(client.font, titleText, bodyText));
    }

    private void reset(Font font, String titleText, String bodyText) {
        applyCopy(font, titleText, bodyText);
        restartRequested = true;
        wantedVisibility = Toast.Visibility.SHOW;
    }

    private void applyCopy(Font font, String titleText, String bodyText) {
        this.title = Component.literal(titleText != null ? titleText : "Nothing found");
        this.body = Component.literal(bodyText != null ? bodyText : "").withStyle(ChatFormatting.GRAY);
        int textAreaWidth = DEFAULT_WIDTH - HORIZONTAL_PADDING * 2;
        this.wrappedBody = font.split(this.body, (int) (textAreaWidth / TEXT_SCALE));
        this.toastHeight = Math.max(SLOT_HEIGHT, textBlockHeight(font) + VERTICAL_PADDING * 2);
    }

    @Override
    public Object getToken() {
        return TOKEN;
    }

    @Override
    public int width() {
        return DEFAULT_WIDTH;
    }

    @Override
    public int height() {
        return toastHeight;
    }

    @Override
    public int occcupiedSlotCount() {
        return (height() + SLOT_HEIGHT - 1) / SLOT_HEIGHT;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long timeSinceVisible) {
        if (restartRequested) {
            lastShownAt = timeSinceVisible;
            restartRequested = false;
        } else if (lastShownAt < 0L) {
            lastShownAt = timeSinceVisible;
        }
        long elapsed = timeSinceVisible - lastShownAt;
        wantedVisibility = elapsed >= DISPLAY_TIME_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.UI_TOAST_IN;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeSinceVisible) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), toastHeight);

        int textX = HORIZONTAL_PADDING;
        int y = VERTICAL_PADDING;
        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(font, title, 0, 0, TITLE_COLOR, false);
        int step = font.lineHeight + Math.round(LINE_GAP / TEXT_SCALE);
        int lineY = font.lineHeight + Math.round(LINE_GAP / TEXT_SCALE);
        for (FormattedCharSequence line : wrappedBody) {
            graphics.text(font, line, 0, lineY, BODY_COLOR, false);
            lineY += step;
        }
        graphics.pose().popMatrix();
    }

    private int scaledLineHeight(Font font) {
        return Math.max(6, Math.round(font.lineHeight * TEXT_SCALE));
    }

    private int textBlockHeight(Font font) {
        int bodyLines = wrappedBody == null ? 0 : wrappedBody.size();
        int lineCount = 1 + bodyLines;
        return lineCount * scaledLineHeight(font) + (lineCount - 1) * LINE_GAP;
    }
}
