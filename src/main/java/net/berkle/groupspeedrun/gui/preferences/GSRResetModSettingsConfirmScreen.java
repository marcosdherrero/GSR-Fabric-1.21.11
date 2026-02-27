package net.berkle.groupspeedrun.gui.preferences;

// Minecraft: screen, GUI, input, NBT, text
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

// Fabric: client networking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// GSR: config, network, parameters
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.config.GSRLocatorNonAdminMode;
import net.berkle.groupspeedrun.network.GSRWorldConfigPayload;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

/**
 * Confirmation popup before resetting Mod Settings to defaults.
 * Prevents accidental reset of HUD Scale, Visibility, Anti-Cheat, Locator Non-Admin, New World Before Run Ends.
 */
public class GSRResetModSettingsConfirmScreen extends Screen {

    private final Screen parent;

    public GSRResetModSettingsConfirmScreen(Screen parent) {
        super(Text.literal("Reset Mod Settings?"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = GSRUiParameters.NEW_WORLD_BUTTON_WIDTH;
        int buttonHeight = GSRUiParameters.CONTROLS_BUTTON_HEIGHT;
        int gap = Math.max(GSRUiParameters.NEW_WORLD_BUTTON_GAP, GSRUiParameters.NEW_WORLD_MIN_BUTTON_GAP);
        int centerX = width / 2;
        int y = height / 2 + GSRUiParameters.CONTROLS_PADDING;

        addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.PREFERENCES_RESET_CONFIRM), btn -> confirm())
                .dimensions(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight).build());
        addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.PREFERENCES_RESET_CANCEL), btn -> cancel())
                .dimensions(centerX + gap / 2, y, buttonWidth, buttonHeight).build());
    }

    private void confirm() {
        GSRConfigPlayer pc = GSRClient.PLAYER_CONFIG;
        GSRConfigWorld wc = GSRClient.clientWorldConfig;

        pc.timerScale = GSRHudParameters.DEFAULT_SCALE;
        pc.locateScale = GSRHudParameters.DEFAULT_SCALE;
        pc.hudVisibility = GSRConfigPlayer.VISIBILITY_PRESSED;
        pc.allowNewWorldBeforeRunEnd = false;
        pc.clampAll();

        GSRPreferencesScreen gsrPrefs = parent instanceof GSRPreferencesScreen ? (GSRPreferencesScreen) parent : null;
        if (gsrPrefs != null) {
            gsrPrefs.gsr$syncPlayerConfig();
            gsrPrefs.gsr$applyVisibilityChange();
        } else {
            NbtCompound nbt = new NbtCompound();
            pc.writeNbt(nbt);
            if (client != null && client.player != null) {
                ClientPlayNetworking.send(new GSRConfigPayload(nbt));
            }
            GSRClient.setPreviousHudVisibility(pc.hudVisibility);
        }

        if (wc != null) {
            wc.antiCheatEnabled = true;
            wc.autoStartEnabled = true;
            wc.locatorNonAdminMode = GSRLocatorNonAdminMode.POST_SPLIT_30MIN.getValue();
            if (client != null && client.player != null) {
                ClientPlayNetworking.send(new GSRWorldConfigPayload(GSRWorldConfigPayload.fromConfig()));
            }
        }

        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void cancel() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, GSRUiParameters.STATUS_BG_COLOR);
        super.render(context, mouseX, mouseY, delta);

        int centerX = width / 2;
        int msgMaxW = Math.min(GSRUiParameters.RESET_CONFIRM_MESSAGE_MAX_WIDTH, width - 80);
        List<net.minecraft.text.OrderedText> lines = textRenderer.wrapLines(
                Text.literal(GSRButtonParameters.PREFERENCES_RESET_CONFIRM_MESSAGE), msgMaxW);

        context.drawCenteredTextWithShadow(textRenderer, getTitle(), centerX,
                height / 2 - GSRUiParameters.RESET_CONFIRM_TITLE_OFFSET, GSRUiParameters.NEW_WORLD_TITLE_COLOR);

        int lineHeight = textRenderer.fontHeight + 2;
        int totalMsgHeight = lines.size() * lineHeight;
        int msgTop = height / 2 - GSRUiParameters.RESET_CONFIRM_MESSAGE_OFFSET - totalMsgHeight / 2;
        for (int i = 0; i < lines.size(); i++) {
            context.drawCenteredTextWithShadow(textRenderer, lines.get(i), centerX,
                    msgTop + i * lineHeight, GSRUiParameters.NEW_WORLD_LINE1_COLOR);
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            cancel();
            return true;
        }
        return super.keyPressed(keyInput);
    }
}
