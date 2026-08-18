package net.berkle.groupspeedrun.gui.widget;

import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

/**
 * Square item-face toggle: vanilla button sprite, green ON / red OFF tint, item icon.
 */
public class GSRItemTintToggleButton extends Button {

    public static final int SIZE = GSRUiParameters.PREFERENCES_SEED_FILTER_BUTTON_SIZE;

    private final ItemStack icon;
    private final BooleanSupplier on;

    public GSRItemTintToggleButton(int x, int y, ItemStack icon, BooleanSupplier on, Button.OnPress onPress) {
        super(x, y, SIZE, SIZE, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.on = on;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractFace(context, getX(), getY(), getWidth(), on.getAsBoolean(), isHovered(), icon);
    }

    public static void extractFace(GuiGraphicsExtractor context, int x, int y, int size, boolean on, boolean hovered, ItemStack icon) {
        var textures = GSRPressableWidgetAccessor.gsr$getTextures();
        context.blitSprite(RenderPipelines.GUI_TEXTURED, textures.get(true, hovered), x, y, size, size);
        int tint = on ? GSRUiParameters.PREFERENCES_SEED_FILTER_ON : GSRUiParameters.PREFERENCES_SEED_FILTER_OFF;
        context.fill(x + 1, y + 1, x + size - 1, y + size - 1, GSRColorHelper.applyAlpha(tint, 0.55f));
        context.fill(x, y, x + size, y + 1, tint);
        context.fill(x, y + size - 1, x + size, y + size, tint);
        context.fill(x, y, x + 1, y + size, tint);
        context.fill(x + size - 1, y, x + size, y + size, tint);
        if (icon == null || icon.isEmpty()) return;
        int iconSize = GSRUiParameters.PREFERENCES_TOGGLE_ICON_SIZE;
        int margin = GSRUiParameters.PREFERENCES_TOGGLE_ICON_MARGIN;
        int inner = Math.max(1, iconSize - 2 * margin);
        float scale = inner / 16f;
        int ix = x + (size - iconSize) / 2;
        int iy = y + (size - iconSize) / 2;
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(ix + margin, iy + margin);
        matrices.scale(scale, scale);
        context.item(icon, 0, 0);
        matrices.popMatrix();
    }
}
