package net.berkle.groupspeedrun.gui.preferences;

import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRBastionIconOption;
import net.berkle.groupspeedrun.config.GSRFortressIconOption;
import net.berkle.groupspeedrun.config.GSRLocatorColorOption;
import net.berkle.groupspeedrun.config.GSRShipIconOption;
import net.berkle.groupspeedrun.config.GSRStrongholdIconOption;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import net.berkle.groupspeedrun.util.GSRLocatorIconHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

/**
 * Renders the locator bar preview in the Preferences screen "Locator HUD" category.
 * Uses pending dropdown values when a locator option dropdown is open.
 */
public final class GSRPreferencesPreviewRenderer {

    private GSRPreferencesPreviewRenderer() {}

    /** Slot IDs for icon/color dropdowns; must match GSRPreferencesScreen. */
    private static final int ID_FORTRESS_ICON = 11;
    private static final int ID_FORTRESS_COLOR = 12;
    private static final int ID_BASTION_ICON = 13;
    private static final int ID_BASTION_COLOR = 14;
    private static final int ID_STRONGHOLD_ICON = 15;
    private static final int ID_STRONGHOLD_COLOR = 16;
    private static final int ID_SHIP_ICON = 17;
    private static final int ID_SHIP_COLOR = 18;

    /**
     * Draws the mini locator bar preview next to the "Locator HUD" category title.
     *
     * @param context Draw context
     * @param model   Screen model (openDropdownId, pendingIndex for pending preview)
     * @param barLeft Left X of preview area
     * @param barTop  Top Y of preview area
     * @param categoryHeaderHeight Height to fit preview within
     */
    public static void drawLocatorPreview(DrawContext context, GSRPreferencesScreenModel model,
            int barLeft, int barTop, int categoryHeaderHeight) {
        GSRConfigPlayer pc = GSRClient.PLAYER_CONFIG;
        float scale = pc.locateScale;
        int halfW = (int) ((GSRLocatorParameters.DEFAULT_BAR_WIDTH / 2.0) * scale);
        int barW = halfW * 2;
        int barH = Math.max(1, (int) (GSRLocatorParameters.DEFAULT_BAR_HEIGHT * scale));
        int r = GSRLocatorParameters.ICON_MARGIN;
        int inner = GSRLocatorParameters.ICON_INNER_RADIUS;
        float naturalHeight = GSRLocatorParameters.BAR_Y_OFFSET + barH + 1 + 2 * r * scale;
        float fitScale = Math.min(1f, categoryHeaderHeight / naturalHeight);
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(barLeft, barTop);
        matrices.scale(fitScale, fitScale);
        barLeft = 0;
        barTop = 0;
        int centerX = barLeft + barW / 2;
        int barY1 = barTop + GSRLocatorParameters.BAR_Y_OFFSET;

        context.fill(barLeft, barY1, barLeft + barW, barY1 + barH, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BG, GSRLocatorParameters.BAR_BG_ALPHA));
        for (int i = barLeft; i < barLeft + barW; i++) {
            float ratio = (float) (i - barLeft) / barW;
            int c = lerpColor(GSRLocatorParameters.BAR_GRADIENT_START, GSRLocatorParameters.BAR_GRADIENT_END, ratio);
            context.fill(i, barY1, i + 1, barY1 + barH, GSRColorHelper.applyAlpha(c, 1f));
        }
        for (int i = 0; i < 5; i++) {
            float rel = (i / 4.0f) * 2 - 1;
            int tx = centerX + (int) (rel * halfW);
            context.fill(tx, barY1, tx + 1, barY1 + barH, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_TICK_COLOR, GSRLocatorParameters.BAR_TICK_ALPHA));
        }
        context.fill(barLeft, barY1 - 1, barLeft + barW, barY1, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_TOP_BORDER, 1f));
        context.fill(barLeft, barY1 + barH, barLeft + barW, barY1 + barH + 1, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BOTTOM_BORDER, 1f));

        float iconCenterY = barY1 + barH / 2f;
        float[] positions = { -0.75f, -0.25f, 0.25f, 0.75f };
        ItemStack[] stacks = {
            GSRLocatorIconHelper.getItemStack(previewItem(0, pc, model), Items.BLAZE_ROD),
            GSRLocatorIconHelper.getItemStack(previewItem(1, pc, model), Items.PIGLIN_HEAD),
            GSRLocatorIconHelper.getItemStack(previewItem(2, pc, model), Items.ENDER_EYE),
            GSRLocatorIconHelper.getItemStack(previewItem(3, pc, model), Items.ELYTRA)
        };
        int[] colors = { previewColor(0, pc, model), previewColor(1, pc, model), previewColor(2, pc, model), previewColor(3, pc, model) };
        float maxOff = ((pc.barWidth / 2.0f) * scale) - ((float) r * scale);
        float iconScale = MathHelper.lerp(0.5f, pc.minIconScale, pc.maxIconScale) * scale;
        for (int i = 0; i < 4; i++) {
            float xOff = -positions[i] * maxOff;
            int iconX = centerX + (int) xOff;
            int iconYInt = (int) iconCenterY;
            matrices.pushMatrix();
            matrices.translate(iconX, iconYInt);
            matrices.scale(scale, scale);
            context.fill(-r, -r, r, r, GSRColorHelper.applyAlpha(colors[i] & 0x00FFFFFF, 1f));
            context.fill(-inner, -inner, inner, inner, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BG, 1f));
            matrices.scale(iconScale / scale, iconScale / scale);
            context.drawItem(stacks[i], -inner, -inner);
            matrices.popMatrix();
        }
        matrices.popMatrix();
    }

    private static String previewItem(int slot, GSRConfigPlayer pc, GSRPreferencesScreenModel model) {
        int iconId = slot == 0 ? ID_FORTRESS_ICON : slot == 1 ? ID_BASTION_ICON : slot == 2 ? ID_STRONGHOLD_ICON : slot == 3 ? ID_SHIP_ICON : -1;
        if (model.openDropdownId == iconId && model.pendingIndex >= 0) {
            if (slot == 0 && model.pendingIndex < GSRFortressIconOption.values().length) return GSRFortressIconOption.values()[model.pendingIndex].getRegistryId();
            if (slot == 1 && model.pendingIndex < GSRBastionIconOption.values().length) return GSRBastionIconOption.values()[model.pendingIndex].getRegistryId();
            if (slot == 2 && model.pendingIndex < GSRStrongholdIconOption.values().length) return GSRStrongholdIconOption.values()[model.pendingIndex].getRegistryId();
            if (slot == 3 && model.pendingIndex < GSRShipIconOption.values().length) return GSRShipIconOption.values()[model.pendingIndex].getRegistryId();
        }
        if (slot == 0) return pc.fortressItem;
        if (slot == 1) return pc.bastionItem;
        if (slot == 2) return pc.strongholdItem;
        if (slot == 3) return pc.shipItem;
        return pc.fortressItem;
    }

    private static int previewColor(int slot, GSRConfigPlayer pc, GSRPreferencesScreenModel model) {
        int colorId = slot == 0 ? ID_FORTRESS_COLOR : slot == 1 ? ID_BASTION_COLOR : slot == 2 ? ID_STRONGHOLD_COLOR : slot == 3 ? ID_SHIP_COLOR : -1;
        if (model.openDropdownId == colorId && model.pendingIndex >= 0 && model.pendingIndex < GSRLocatorColorOption.values().length) {
            int def = slot == 0 ? GSRLocatorParameters.DEFAULT_FORTRESS_COLOR : slot == 1 ? GSRLocatorParameters.DEFAULT_BASTION_COLOR : slot == 2 ? GSRLocatorParameters.DEFAULT_STRONGHOLD_COLOR : slot == 3 ? GSRLocatorParameters.DEFAULT_SHIP_COLOR : 0xFF555555;
            return GSRLocatorColorOption.values()[model.pendingIndex].getValue(def);
        }
        if (slot == 0) return pc.fortressColor;
        if (slot == 1) return pc.bastionColor;
        if (slot == 2) return pc.strongholdColor;
        if (slot == 3) return pc.shipColor;
        return pc.fortressColor;
    }

    private static int lerpColor(int c1, int c2, float ratio) {
        int r = (int) MathHelper.lerp(ratio, (c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF);
        int g = (int) MathHelper.lerp(ratio, (c1 >> 8) & 0xFF, (c2 >> 8) & 0xFF);
        int b = (int) MathHelper.lerp(ratio, c1 & 0xFF, c2 & 0xFF);
        return (r << 16) | (g << 8) | b;
    }
}
