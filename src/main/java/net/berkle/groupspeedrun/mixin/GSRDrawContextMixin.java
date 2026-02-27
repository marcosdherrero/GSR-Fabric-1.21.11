package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.gui.GSRTooltipRenderer;
import net.berkle.groupspeedrun.mixin.accessors.GSRDrawContextAccessor;
import net.berkle.groupspeedrun.mixin.accessors.GSROrderedTextTooltipComponentAccessor;
import net.berkle.groupspeedrun.parameter.GSRTooltipParameters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Intercepts DrawContext.drawTooltip overloads to apply standardized GSR tooltip style:
 * 35% max screen size, cyclical scroll, and deferred rendering so tooltips appear on top.
 * Affects options menus, config screens (Cloth Config), and button tooltips.
 */
@Mixin(DrawContext.class)
public abstract class GSRDrawContextMixin {

    /** Intercepts drawTooltipImmediately – used by Cloth Config. Draws immediately (no defer) since deferred phase may not run in scrollable lists. */
    @Inject(method = "drawTooltipImmediately(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipImmediately(TextRenderer textRenderer, List<TooltipComponent> components,
                                              int x, int y, TooltipPositioner positioner, Identifier texture,
                                              CallbackInfo ci) {
        List<OrderedText> lines = gsr$componentsToOrderedText(components);
        if (lines != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
                GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                        (DrawContext) (Object) this, textRenderer, lines, positioner,
                        x, y, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            }
            ci.cancel();
        }
    }

    /** Intercepts full drawTooltip to apply GSR style and defer for correct z-order. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Lnet/minecraft/client/gui/tooltip/TooltipPositioner;IIZ)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltip(TextRenderer textRenderer, List<OrderedText> lines,
                                   TooltipPositioner positioner, int mouseX, int mouseY, boolean focused,
                                   CallbackInfo ci) {
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines, positioner, mouseX, mouseY);
        ci.cancel();
    }

    /** Intercepts simple drawTooltip(List, x, y) overload. */
    @Inject(method = "drawTooltip(Ljava/util/List;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipSimple(List<OrderedText> lines, int x, int y, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;
        TextRenderer textRenderer = client.textRenderer;
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, List<Text>, x, y) used by Cloth Config and others. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextList(TextRenderer textRenderer, List<Text> textLines, int x, int y, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Text, x, y) overload. */
    @Inject(method = "drawTooltip(Lnet/minecraft/text/Text;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipText(Text text, int x, int y, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;
        TextRenderer textRenderer = client.textRenderer;
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, Text, x, y) overload. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextRenderer(TextRenderer textRenderer, Text text, int x, int y, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, List<Text>, Optional<TooltipData>, x, y) – e.g. item tooltips in config. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithOptional(TextRenderer textRenderer, List<Text> textLines,
                                               Optional<?> data, int x, int y, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, List<Text>, x, y, Identifier) – custom background texture. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithTexture(TextRenderer textRenderer, List<Text> textLines,
                                             int x, int y, Identifier texture, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, List<Text>, Optional, x, y, Identifier). */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithOptionalAndTexture(TextRenderer textRenderer, List<Text> textLines,
                                                         Optional<?> data, int x, int y, Identifier texture, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(TextRenderer, Text, x, y, Identifier). */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IILnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextWithTexture(TextRenderer textRenderer, Text text,
                                                   int x, int y, Identifier texture, CallbackInfo ci) {
        List<OrderedText> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((DrawContext) (Object) this, textRenderer, lines,
                HoveredTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /**
     * Converts List&lt;TooltipComponent&gt; to List&lt;OrderedText&gt; when all components are OrderedTextTooltipComponent.
     * Returns null if any component is not text-only (e.g. BundleTooltipComponent), so we let vanilla handle it.
     */
    private List<OrderedText> gsr$componentsToOrderedText(List<TooltipComponent> components) {
        if (components == null || components.isEmpty()) return null;
        List<OrderedText> lines = new ArrayList<>();
        for (TooltipComponent comp : components) {
            if (!(comp instanceof OrderedTextTooltipComponent)) return null;
            lines.add(((GSROrderedTextTooltipComponentAccessor) comp).gsr$getText());
        }
        return lines;
    }

    /**
     * Wraps Text lines at 35% screen width so tooltips wrap instead of staying on one line.
     */
    private List<OrderedText> gsr$wrapTextLines(TextRenderer textRenderer, List<Text> textLines) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return textLines.stream()
                    .flatMap(t -> textRenderer.wrapLines(t, 200).stream())
                    .collect(Collectors.toList());
        }
        int maxBoxW = Math.max(GSRTooltipParameters.MIN_BOX_WIDTH,
                (int) (client.getWindow().getScaledWidth() * GSRTooltipParameters.MAX_SCREEN_FRACTION));
        int maxContentW = Math.max(1, maxBoxW - 2 * GSRTooltipParameters.PADDING);
        return textLines.stream()
                .flatMap(t -> textRenderer.wrapLines(t, maxContentW).stream())
                .collect(Collectors.toList());
    }

    /** Defers tooltip drawing via tooltipDrawer so it renders on top (drawDeferredElements phase). */
    private void gsr$deferTooltip(DrawContext context, TextRenderer textRenderer,
                                 List<OrderedText> lines, TooltipPositioner positioner,
                                 int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        ((GSRDrawContextAccessor) context).gsr$setTooltipDrawer(() ->
                GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                        context, textRenderer, lines, positioner,
                        mouseX, mouseY, screenWidth, screenHeight));
    }
}
