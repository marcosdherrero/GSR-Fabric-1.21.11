package net.berkle.groupspeedrun.gui;

// Minecraft: screen, GUI, input
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

// GSR: parameters
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

/**
 * Confirmation popup before turning on a locator when it would invalidate the run for ranking.
 * Shows when anti-cheat is enabled, a run is active, and the user selects an icon (not Off).
 */
public class GSRLocatorInvalidateConfirmScreen extends Screen {

    private final Screen parent;
    private final String locatorName;
    private final Runnable onConfirm;

    /**
     * @param parent      Screen to return to on cancel (typically Locators screen).
     * @param locatorName Display name of the locator (e.g. "Fortress", "Bastion").
     * @param onConfirm   Runnable to execute when user confirms (performs the toggle).
     */
    public GSRLocatorInvalidateConfirmScreen(Screen parent, String locatorName, Runnable onConfirm) {
        super(Component.literal("Locator Will Invalidate Run"));
        this.parent = parent;
        this.locatorName = locatorName != null ? locatorName : "Locator";
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = GSRUiParameters.NEW_WORLD_BUTTON_WIDTH;
        int buttonHeight = GSRUiParameters.CONTROLS_BUTTON_HEIGHT;
        int gap = Math.max(GSRUiParameters.NEW_WORLD_BUTTON_GAP, GSRUiParameters.NEW_WORLD_MIN_BUTTON_GAP);
        int centerX = width / 2;
        int y = height / 2 + GSRUiParameters.CONTROLS_PADDING;

        addRenderableWidget(Button.builder(GSRButtonParameters.literal(GSRButtonParameters.LOCATOR_CONFIRM_TURN_ON), btn -> confirm())
                .bounds(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight).build());
        addRenderableWidget(Button.builder(GSRButtonParameters.literal(GSRButtonParameters.LOCATOR_CONFIRM_CANCEL), btn -> cancel())
                .bounds(centerX + gap / 2, y, buttonWidth, buttonHeight).build());
    }

    private void confirm() {
        if (onConfirm != null) onConfirm.run();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    private void cancel() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, GSRUiParameters.STATUS_BG_COLOR);
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(font, getTitle(), width / 2, height / 2 - GSRUiParameters.LOCATOR_CONFIRM_TITLE_OFFSET, GSRUiParameters.NEW_WORLD_TITLE_COLOR);
        String message = "Turning on " + locatorName + " will invalidate this run for ranking.";
        context.centeredText(font, message, width / 2, height / 2 - GSRUiParameters.LOCATOR_CONFIRM_MESSAGE_OFFSET, GSRUiParameters.NEW_WORLD_LINE1_COLOR);
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            cancel();
            return true;
        }
        return super.keyPressed(keyInput);
    }
}
