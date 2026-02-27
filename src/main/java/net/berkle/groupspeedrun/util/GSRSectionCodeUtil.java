package net.berkle.groupspeedrun.util;

import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * Minecraft § (section sign) color code parsing. Shared by Status screen and Run History.
 */
public final class GSRSectionCodeUtil {

    private GSRSectionCodeUtil() {}

    /** Minecraft § color codes to ARGB (0xAARRGGBB). */
    public static int sectionCodeToArgb(char code) {
        return switch (code) {
            case '0' -> 0xFF000000;
            case '1' -> 0xFF0000AA;
            case '2' -> 0xFF00AA00;
            case '3' -> 0xFF00AAAA;
            case '4' -> 0xFFAA0000;
            case '5' -> 0xFFAA00AA;
            case '6' -> 0xFFFFAA00;
            case '7' -> 0xFFAAAAAA;
            case '8' -> 0xFF555555;
            case '9' -> 0xFF5555FF;
            case 'a' -> 0xFF55FF55;
            case 'b' -> 0xFF55FFFF;
            case 'c' -> 0xFFFF5555;
            case 'd' -> 0xFFFF55FF;
            case 'e' -> 0xFFFFFF55;
            case 'f' -> 0xFFFFFFFF;
            default -> GSRUiParameters.STATUS_DEFAULT_TEXT_COLOR;
        };
    }

    /** Draw one line, parsing § color codes and drawing each segment with the correct color. */
    public static void drawLineWithSectionColors(DrawContext context, TextRenderer textRenderer, String line, int x, int y) {
        int defaultColor = GSRUiParameters.STATUS_DEFAULT_TEXT_COLOR;
        int currentColor = defaultColor;
        StringBuilder segment = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '§' && i + 1 < line.length()) {
                if (segment.length() > 0) {
                    OrderedText ordered = Text.literal(segment.toString()).asOrderedText();
                    context.drawTextWithShadow(textRenderer, ordered, x, y, currentColor);
                    x += textRenderer.getWidth(ordered);
                    segment.setLength(0);
                }
                char code = line.charAt(++i);
                currentColor = sectionCodeToArgb(code);
                continue;
            }
            segment.append(c);
        }
        if (segment.length() > 0) {
            OrderedText ordered = Text.literal(segment.toString()).asOrderedText();
            context.drawTextWithShadow(textRenderer, ordered, x, y, currentColor);
        }
    }
}
