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
 * Confirmation popup before applying Run Manager participant changes when it would invalidate the run for ranking.
 * Shows when anti-cheat is enabled, a run is active, and the user changes group death, shared health, or excluded participants.
 */
public class GSRRunManagerDerankConfirmScreen extends Screen {

    private final Screen parent;
    private final Runnable onConfirm;

    /**
     * @param parent    Screen to return to on cancel (typically Run Manager screen).
     * @param onConfirm Runnable to execute when user confirms (performs the save).
     */
    public GSRRunManagerDerankConfirmScreen(Screen parent, Runnable onConfirm) {
        super(Component.literal("Participant Change Will Invalidate Run"));
        this.parent = parent;
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

        addRenderableWidget(Button.builder(GSRButtonParameters.literal(GSRButtonParameters.RUN_MANAGER_DERANK_CONFIRM_APPLY), btn -> confirm())
                .bounds(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight).build());
        addRenderableWidget(Button.builder(GSRButtonParameters.literal(GSRButtonParameters.RUN_MANAGER_DERANK_CONFIRM_CANCEL), btn -> cancel())
                .bounds(centerX + gap / 2, y, buttonWidth, buttonHeight).build());
    }

    private void confirm() {
        if (onConfirm != null) onConfirm.run();
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    private void cancel() {
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, GSRUiParameters.STATUS_BG_COLOR);
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(font, getTitle(), width / 2, height / 2 - GSRUiParameters.LOCATOR_CONFIRM_TITLE_OFFSET, GSRUiParameters.NEW_WORLD_TITLE_COLOR);
        String message = "Changing participants after a run has started will invalidate this run for ranking.";
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
