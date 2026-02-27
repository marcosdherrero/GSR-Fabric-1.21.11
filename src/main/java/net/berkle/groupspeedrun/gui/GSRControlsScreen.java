package net.berkle.groupspeedrun.gui;

// Fabric: client networking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft: screen, GUI, input
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
// LWJGL: key codes
import org.lwjgl.glfw.GLFW;

// GSR: client state, config, network, parameters, HUD renderer, key bindings
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.client.GSRKeyBindings;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.network.GSRRunActionPayload;
import net.berkle.groupspeedrun.mixin.accessors.GSRClickableWidgetAccessor;
import net.berkle.groupspeedrun.gui.components.GSRMenuComponents;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.timer.hud.GSRTimerHudRenderer;

/**
 * GSR Controls: Two-column layout matching main menu. Start/Pause/Resume Run, Reset Run, This Run Mngr,
 * GSR Run History, Locators, GSR Config in content area; Back centered at bottom.
 * Start shown when run not started; Pause when active and !manualPause; Resume when active and manualPause.
 */
public class GSRControlsScreen extends GSRBaseScreen {

    /** Start/Pause/Resume button; label updated each tick so it reflects current run state. */
    private ButtonWidget startPauseResumeBtn;

    public GSRControlsScreen(Screen parent) {
        super(GSRButtonParameters.literal(GSRButtonParameters.SCREEN_CONTROLS), parent);
    }

    @Override
    protected void init() {
        super.init();
        int halfW = GSRUiParameters.CONTROLS_HALF_BUTTON_WIDTH;
        int btnH = GSRUiParameters.CONTROLS_BUTTON_HEIGHT;
        int rowGap = GSRUiParameters.CONTROLS_ROW_GAP;
        int colGap = GSRUiParameters.CONTROLS_COL_GAP;
        int centerX = width / 2;
        int leftX = centerX - colGap / 2 - halfW;
        int rightX = centerX + colGap / 2;

        int row1Y = height / 2 + GSRUiParameters.CONTROLS_INITIAL_Y_OFFSET;
        int row2Y = row1Y + btnH + rowGap;
        int row3Y = row2Y + btnH + rowGap;

        boolean inWorld = client != null && client.world != null;
        GSRConfigPlayer pConfig = GSRClient.PLAYER_CONFIG;
        boolean canUseAdmin = pConfig != null && pConfig.canUseAdmin;

        startPauseResumeBtn = ButtonWidget.builder(
                GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_START),
                b -> {
                    byte action = getStartPauseResumeAction();
                    applyOptimisticRunState(action);
                    ClientPlayNetworking.send(new GSRRunActionPayload(action));
                    updateStartPauseResumeButton();
                })
                .dimensions(leftX, row1Y, halfW, btnH).build();
        updateStartPauseResumeButton();
        addDrawableChild(startPauseResumeBtn);

        ButtonWidget resetBtn = ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_RESET), b -> {
                    if (client != null) client.setScreen(new GSRResetConfirmScreen(this));
                })
                .dimensions(rightX, row1Y, halfW, btnH).build();
        ((GSRClickableWidgetAccessor) resetBtn).gsr$setActive(inWorld && canUseAdmin);
        addDrawableChild(resetBtn);

        ButtonWidget runManagerBtn = ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_RUN_MANAGER), b -> {
            if (client != null) client.setScreen(new GSRRunManagerScreen(this));
        }).dimensions(leftX, row2Y, halfW, btnH).build();
        ((GSRClickableWidgetAccessor) runManagerBtn).gsr$setActive(inWorld);
        addDrawableChild(runManagerBtn);
        ButtonWidget locatorsBtn = ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_LOCATORS), b -> {
            if (client != null) client.setScreen(new GSRLocatorsScreen(this));
        }).dimensions(rightX, row2Y, halfW, btnH).build();
        ((GSRClickableWidgetAccessor) locatorsBtn).gsr$setActive(inWorld);
        addDrawableChild(locatorsBtn);

        addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_RUN_HISTORY), b -> {
            if (client != null) client.setScreen(new GSRRunHistoryScreen(this, true));
        }).dimensions(leftX, row3Y, halfW, btnH).build());
        addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.CONTROLS_PREFERENCES), b -> {
            if (client != null) client.setScreen(new net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen(this));
        }).dimensions(rightX, row3Y, halfW, btnH).build());

        var footer = GSRMenuComponents.singleButtonFooterLayout(width, height);
        addDrawableChild(ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.FOOTER_BACK), btn -> goBack())
                .dimensions(footer.buttonX(), footer.footerY(), footer.buttonWidth(), footer.buttonHeight()).build());
    }

    /** Returns the action byte for Start/Pause/Resume. Resume when manualPause; Pause when run active and not manualPause. */
    private byte getStartPauseResumeAction() {
        GSRConfigWorld wc = GSRClient.clientWorldConfig;
        boolean runNotStarted = wc != null && wc.startTime <= 0 && !wc.isVictorious && !wc.isFailed;
        boolean runActive = wc != null && wc.startTime > 0 && !wc.isVictorious && !wc.isFailed;
        return runNotStarted ? GSRRunActionPayload.ACTION_START
                : (wc != null && runActive && wc.manualPause ? GSRRunActionPayload.ACTION_RESUME : GSRRunActionPayload.ACTION_PAUSE);
    }

    /** Applies expected run state to client config so button reflects change immediately before server sync. */
    private void applyOptimisticRunState(byte action) {
        GSRConfigWorld wc = GSRClient.clientWorldConfig;
        if (wc == null) return;
        if (action == GSRRunActionPayload.ACTION_PAUSE) {
            wc.frozenByServerStop = false;
            wc.manualPause = true;
            wc.frozenTime = wc.getElapsedTime();
            wc.isTimerFrozen = true;
        } else if (action == GSRRunActionPayload.ACTION_RESUME) {
            wc.manualPause = false;
            wc.isTimerFrozen = false;
        } else if (action == GSRRunActionPayload.ACTION_START) {
            wc.startTime = System.currentTimeMillis();
            wc.manualPause = false;
            wc.isTimerFrozen = false;
            wc.frozenTime = 0;
        }
    }

    /** Updates Start/Pause/Resume button from world run data. Show Start when not started; Resume when manualPause; Pause otherwise.
     * In singleplayer when game is paused, Pause/Resume is grayed out (timer already frozen by game pause). */
    private void updateStartPauseResumeButton() {
        if (startPauseResumeBtn == null) return;
        boolean inWorld = client != null && client.world != null;
        GSRConfigWorld wc = GSRClient.clientWorldConfig;
        boolean runNotStarted = wc != null && wc.startTime <= 0 && !wc.isVictorious && !wc.isFailed;
        boolean runActive = wc != null && wc.startTime > 0 && !wc.isVictorious && !wc.isFailed;
        String label = runNotStarted ? GSRButtonParameters.CONTROLS_START
                : (wc != null && runActive && wc.manualPause ? GSRButtonParameters.CONTROLS_RESUME : GSRButtonParameters.CONTROLS_PAUSE);
        GSRConfigPlayer pConfig = GSRClient.PLAYER_CONFIG;
        boolean canUseAdmin = pConfig != null && pConfig.canUseAdmin;
        boolean singlePlayerPaused = client != null && client.getServer() != null && client.isPaused();
        boolean pauseResumeRedundant = singlePlayerPaused && runActive;
        boolean active = inWorld && canUseAdmin && (runNotStarted || runActive) && !pauseResumeRedundant;
        startPauseResumeBtn.setMessage(GSRButtonParameters.literal(label));
        ((GSRClickableWidgetAccessor) startPauseResumeBtn).gsr$setActive(active);
    }

    @Override
    public void tick() {
        super.tick();
        updateStartPauseResumeButton();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, GSRUiParameters.SCREEN_BG_DARK);
        // Timer drawn first (behind buttons) with reduced alpha so options are the focus
        GSRConfigWorld wc = GSRClient.clientWorldConfig;
        GSRConfigPlayer pc = GSRClient.PLAYER_CONFIG;
        if (wc != null && pc != null && client != null) {
            int[] size = GSRTimerHudRenderer.getTimerBoxScaledSize(textRenderer, wc, pc, true);
            int scaledH = size[1];
            int anchorX = pc.timerHudOnRight ? (width - GSRTimerHudRenderer.EDGE_MARGIN) : GSRTimerHudRenderer.EDGE_MARGIN;
            int anchorY = (int) ((height / 2f) - (scaledH / 2f) - (height * GSRTimerHudRenderer.VERTICAL_OFFSET_FACTOR));
            GSRTimerHudRenderer.drawTimerBox(context, textRenderer, pc.timerHudOnRight, anchorX, anchorY, wc, pc, true, 1f, true, GSRUiParameters.CONTROLS_TIMER_ALPHA);
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, GSRUiParameters.TITLE_Y, GSRUiParameters.TITLE_COLOR);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            goBack();
            return true;
        }
        if (GSRKeyBindings.openGsrOptionsKey != null && GSRKeyBindings.openGsrOptionsKey.matchesKey(keyInput)) {
            goBack();
            return true;
        }
        return super.keyPressed(keyInput);
    }
}
