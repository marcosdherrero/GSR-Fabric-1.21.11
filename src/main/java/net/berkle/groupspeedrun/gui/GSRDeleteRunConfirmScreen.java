package net.berkle.groupspeedrun.gui;

// Minecraft: screen, GUI, input
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

// GSR: parameters
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

/**
 * Confirmation popup before deleting a run from personal and shared lists.
 * Shows when the user clicks "Delete Run" in Run History.
 * When multiple runs are selected, offers "Delete" (single) and "Delete All Selected".
 */
public class GSRDeleteRunConfirmScreen extends Screen {

    private final Screen parent;
    /** Deletes only the run currently being displayed. */
    private final Runnable onConfirmOne;
    /** Deletes all selected runs; null when only one run is selected. */
    private final Runnable onConfirmAll;

    /**
     * @param parent       Screen to return to on cancel (typically Run History).
     * @param onConfirmOne Runnable to delete the single run being displayed.
     * @param onConfirmAll Optional runnable to delete all selected runs; null to hide "Delete All Selected" button.
     */
    public GSRDeleteRunConfirmScreen(Screen parent, Runnable onConfirmOne, Runnable onConfirmAll) {
        super(Text.literal("Delete Run?"));
        this.parent = parent;
        this.onConfirmOne = onConfirmOne;
        this.onConfirmAll = onConfirmAll;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = GSRUiParameters.NEW_WORLD_BUTTON_WIDTH;
        int buttonHeight = GSRUiParameters.CONTROLS_BUTTON_HEIGHT;
        int gap = Math.max(GSRUiParameters.NEW_WORLD_BUTTON_GAP, GSRUiParameters.NEW_WORLD_MIN_BUTTON_GAP);
        int centerX = width / 2;
        int y = height / 2 + GSRUiParameters.CONTROLS_PADDING;

        if (onConfirmAll != null) {
            int totalWidth = 3 * buttonWidth + 2 * gap;
            int leftX = centerX - totalWidth / 2;
            addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.RUN_HISTORY_DELETE_ONE), btn -> confirmOne())
                    .dimensions(leftX, y, buttonWidth, buttonHeight).build());
            addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.RUN_HISTORY_DELETE_ALL_SELECTED), btn -> confirmAll())
                    .dimensions(leftX + buttonWidth + gap, y, buttonWidth, buttonHeight).build());
            addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.NEW_WORLD_CANCEL), btn -> cancel())
                    .dimensions(leftX + 2 * (buttonWidth + gap), y, buttonWidth, buttonHeight).build());
        } else {
            addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.RUN_HISTORY_DELETE_ONE), btn -> confirmOne())
                    .dimensions(centerX - buttonWidth - gap / 2, y, buttonWidth, buttonHeight).build());
            addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.NEW_WORLD_CANCEL), btn -> cancel())
                    .dimensions(centerX + gap / 2, y, buttonWidth, buttonHeight).build());
        }
    }

    private void confirmOne() {
        if (onConfirmOne != null) onConfirmOne.run();
        if (client != null) client.setScreen(parent);
    }

    private void confirmAll() {
        if (onConfirmAll != null) onConfirmAll.run();
        if (client != null) client.setScreen(parent);
    }

    private void cancel() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, GSRUiParameters.STATUS_BG_COLOR);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, height / 2 - GSRUiParameters.LOCATOR_CONFIRM_TITLE_OFFSET, GSRUiParameters.NEW_WORLD_TITLE_COLOR);
        String message = onConfirmAll != null
                ? "Delete the displayed run, or delete all selected runs from your personal and shared lists."
                : "This will remove the run from your personal and shared lists.";
        context.drawCenteredTextWithShadow(textRenderer, message, width / 2, height / 2 - GSRUiParameters.LOCATOR_CONFIRM_MESSAGE_OFFSET, GSRUiParameters.NEW_WORLD_LINE1_COLOR);
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
