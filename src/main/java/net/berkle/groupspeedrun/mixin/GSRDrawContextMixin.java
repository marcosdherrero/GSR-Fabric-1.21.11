package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.gui.GSRTooltipRenderer;
import net.berkle.groupspeedrun.mixin.accessors.GSRDrawContextAccessor;
import net.berkle.groupspeedrun.mixin.accessors.GSROrderedTextTooltipComponentAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts GuiGraphicsExtractor tooltip entry points to apply GSR tooltip style:
 * 35% max screen size, cyclical scroll, and deferred rendering so tooltips appear on top.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GSRDrawContextMixin {

    @Inject(method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private void gsr$onTooltip(Font textRenderer, List<ClientTooltipComponent> components,
                               int x, int y, ClientTooltipPositioner positioner, Identifier texture,
                               CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$componentsToOrderedText(components);
        if (lines == null) return;
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.getWindow() != null) {
            GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                    (GuiGraphicsExtractor) (Object) this, textRenderer, lines, positioner,
                    x, y, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        }
        ci.cancel();
    }

    @Inject(method = "setTooltipForNextFrameInternal", at = @At("HEAD"), cancellable = true)
    private void gsr$onSetTooltipForNextFrameInternal(Font textRenderer, List<ClientTooltipComponent> components,
                                                      int x, int y, ClientTooltipPositioner positioner,
                                                      Identifier texture, boolean focused, CallbackInfo ci) {
        List<FormattedCharSequence> lines = gsr$componentsToOrderedText(components);
        if (lines == null) return;
        gsr$deferTooltip((GuiGraphicsExtractor) (Object) this, textRenderer, lines, positioner, x, y);
        ci.cancel();
    }

    private List<FormattedCharSequence> gsr$componentsToOrderedText(List<ClientTooltipComponent> components) {
        if (components == null || components.isEmpty()) return null;
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (ClientTooltipComponent comp : components) {
            if (!(comp instanceof ClientTextTooltip)) return null;
            lines.add(((GSROrderedTextTooltipComponentAccessor) comp).gsr$getText());
        }
        return lines;
    }

    private void gsr$deferTooltip(GuiGraphicsExtractor context, Font textRenderer,
                                 List<FormattedCharSequence> lines, ClientTooltipPositioner positioner,
                                 int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        ((GSRDrawContextAccessor) context).gsr$setDeferredTooltip(() ->
                GSRTooltipRenderer.drawTooltipWithMaxSizeAndScroll(
                        context, textRenderer, lines, positioner,
                        mouseX, mouseY, screenWidth, screenHeight));
    }
}
