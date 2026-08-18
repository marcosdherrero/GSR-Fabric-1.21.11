package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.gui.GSRTooltipRenderer;
import net.berkle.groupspeedrun.mixin.accessors.GSRDrawContextAccessor;
import net.berkle.groupspeedrun.mixin.accessors.GSROrderedTextTooltipComponentAccessor;
import net.berkle.groupspeedrun.parameter.GSRTooltipParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Intercepts GuiGraphicsExtractor.drawTooltip overloads to apply standardized GSR tooltip style:
 * 35% max screen size, cyclical scroll, and deferred rendering so tooltips appear on top.
 * Affects options menus, config screens (Cloth Config), and button tooltips.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GSRDrawContextMixin {

    /** Intercepts drawTooltipImmediately – used by Cloth Config. Draws immediately (no defer) since deferred phase may not run in scrollable lists. */
    @Inject(method = "drawTooltipImmediately(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipImmediately(Font textRenderer, List<ClientTooltipComponent> components,
                                              int x, int y, ClientTooltipPositioner positioner, Identifier texture,
                                              CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$componentsToOrderedText(components);
        if (lines != null) {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.getWindow() != null) {
                GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                        (GuiGraphicsExtractor) (Object) this, textRenderer, lines, positioner,
                        x, y, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            }
            ci.cancel();
        }
    }

    /** Intercepts full drawTooltip to apply GSR style and defer for correct z-order. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;IIZ)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltip(Font textRenderer, List<FormattedCharSequence> lines,
                                   ClientTooltipPositioner positioner, int mouseX, int mouseY, boolean focused,
                                   CallbackInfo ci) {
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines, positioner, mouseX, mouseY);
        ci.cancel();
    }

    /** Intercepts simple drawTooltip(List, x, y) overload. */
    @Inject(method = "drawTooltip(Ljava/util/List;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipSimple(List<FormattedCharSequence> lines, int x, int y, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;
        Font textRenderer = client.font;
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, List<Text>, x, y) used by Cloth Config and others. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextList(Font textRenderer, List<Text> textLines, int x, int y, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Text, x, y) overload. */
    @Inject(method = "drawTooltip(Lnet/minecraft/network/chat/Component;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipText(Text text, int x, int y, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;
        Font textRenderer = client.font;
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, Text, x, y) overload. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextRenderer(Font textRenderer, Text text, int x, int y, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, List<Text>, Optional<TooltipData>, x, y) – e.g. item tooltips in config. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithOptional(Font textRenderer, List<Text> textLines,
                                               Optional<?> data, int x, int y, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, List<Text>, x, y, Identifier) – custom background texture. */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithTexture(Font textRenderer, List<Text> textLines,
                                             int x, int y, Identifier texture, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, List<Text>, Optional, x, y, Identifier). */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipWithOptionalAndTexture(Font textRenderer, List<Text> textLines,
                                                         Optional<?> data, int x, int y, Identifier texture, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, textLines);
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /** Intercepts drawTooltip(Font, Text, x, y, Identifier). */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IILnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onDrawTooltipTextWithTexture(Font textRenderer, Text text,
                                                   int x, int y, Identifier texture, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$wrapTextLines(textRenderer, List.of(text));
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines,
                ClientTooltipPositioner.INSTANCE, x, y);
        ci.cancel();
    }

    /**
     * Converts List&lt;ClientTooltipComponent&gt; to List&lt;FormattedCharSequence&gt; when all components are ClientTextTooltip.
     * Returns null if any component is not text-only (e.g. BundleTooltipComponent), so we let vanilla handle it.
     */
    private List<FormattedCharSequence> gsr$componentsToOrderedText(List<ClientTooltipComponent> components) {
        if (components == null || components.isEmpty()) return null;
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (ClientTooltipComponent comp : components) {
            if (!(comp instanceof ClientTextTooltip)) return null;
            lines.add(((GSROrderedTextTooltipComponentAccessor) comp).gsr$getText());
        }
        return lines;
    }

    /**
     * Wraps Text lines at 35% screen width so tooltips wrap instead of staying on one line.
     */
    private List<FormattedCharSequence> gsr$wrapTextLines(Font textRenderer, List<Text> textLines) {
        Minecraft client = Minecraft.getInstance();
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
    private void gsr$deferTooltip(GuiGraphicsExtractor context, Font textRenderer,
                                 List<FormattedCharSequence> lines, ClientTooltipPositioner positioner,
                                 int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        ((GSRDrawContextAccessor) context).gsr$setTooltipDrawer(() ->
                GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                        context, textRenderer, lines, positioner,
                        mouseX, mouseY, screenWidth, screenHeight));
    }
}
