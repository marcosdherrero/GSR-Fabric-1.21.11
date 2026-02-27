package net.berkle.groupspeedrun.gui.runmanager;

import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.gui.components.GSRMultiSelectDropdown;
import net.berkle.groupspeedrun.gui.components.GSRMultiSelectDropdownBehavior;
import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Renders Run Manager dropdown sections: Group Death Participants and Group Health Participants.
 * Container fits content exactly: no extra gap, selections not cut off.
 */
public final class GSRRunManagerContent {

    private final GSRMultiSelectDropdown<GSRRunManagerModel> deathDropdown;
    private final GSRMultiSelectDropdown<GSRRunManagerModel> healthDropdown;

    public GSRRunManagerContent() {
        deathDropdown = new GSRMultiSelectDropdown<>(
                "Excluded from Group Death:",
                "Select players to exclude (their death will not fail the run)",
                "dd-death",
                true,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunManagerModel model) {
                        return model.getSelectablePlayers().stream().map(e -> e.name()).toList();
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunManagerModel model) {
                        Set<Integer> indices = new HashSet<>();
                        List<GSRRunManagerModel.PlayerEntry> selectable = model.getSelectablePlayers();
                        Set<UUID> ref = model.deathDropdownOpen ? model.pendingGroupDeathParticipants : model.groupDeathParticipants;
                        for (int i = 0; i < selectable.size(); i++) {
                            if (ref.contains(selectable.get(i).uuid())) indices.add(i);
                        }
                        return indices;
                    }

                    @Override
                    public String getDisplayLabel(GSRRunManagerModel model) {
                        Set<UUID> ref = model.deathDropdownOpen ? model.pendingGroupDeathParticipants : model.groupDeathParticipants;
                        if (ref.isEmpty()) return "None";
                        return ref.size() + " excluded";
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunManagerModel model) {
                        return model.deathSelectionTimeMs;
                    }
                });
        healthDropdown = new GSRMultiSelectDropdown<>(
                "Group Health Participants:",
                "Group Health – select players sharing health",
                "dd-health",
                true,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunManagerModel model) {
                        return model.getSelectablePlayers().stream().map(e -> e.name()).toList();
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunManagerModel model) {
                        Set<Integer> indices = new HashSet<>();
                        List<GSRRunManagerModel.PlayerEntry> selectable = model.getSelectablePlayers();
                        Set<UUID> ref = model.healthDropdownOpen ? model.pendingSharedHealthParticipants : model.sharedHealthParticipants;
                        for (int i = 0; i < selectable.size(); i++) {
                            if (ref.contains(selectable.get(i).uuid())) indices.add(i);
                        }
                        return indices;
                    }

                    @Override
                    public String getDisplayLabel(GSRRunManagerModel model) {
                        Set<UUID> ref = model.healthDropdownOpen ? model.pendingSharedHealthParticipants : model.sharedHealthParticipants;
                        if (ref.isEmpty()) return "None";
                        return ref.size() + " selected";
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunManagerModel model) {
                        return model.healthSelectionTimeMs;
                    }
                });
    }

    public GSRMultiSelectDropdown<GSRRunManagerModel> getDeathDropdown() {
        return deathDropdown;
    }

    public GSRMultiSelectDropdown<GSRRunManagerModel> getHealthDropdown() {
        return healthDropdown;
    }

    /**
     * Renders the two dropdown sections (closed) or overlay + confirm (open).
     * Container bounds fit content exactly; no scissor needed.
     */
    public void render(GSRRunManagerModel model, DrawContext context, TextRenderer textRenderer,
                       GSRTickerState tickerState, int listLeft, int listWidth, int containerTop, int containerBottom,
                       int mouseX, int mouseY) {
        int barHeight = GSRRunHistoryParameters.SELECTOR_BAR_HEIGHT;
        float labelScale = GSRRunHistoryParameters.LEFT_COLUMN_LABEL_SCALE;

        boolean dropdownOpen = model.deathDropdownOpen || model.healthDropdownOpen;

        if (dropdownOpen) {
            int itemCount = model.getSelectablePlayers().size();
            String header = model.deathDropdownOpen ? deathDropdown.getHeader() : healthDropdown.getHeader();
            int[] bounds = GSRRunManagerLayout.openBounds(textRenderer, header, itemCount, listWidth);
            int overlayTop = bounds[0];
            int listBottom = bounds[3];
            int confirmButtonTop = bounds[4];
            int delimiterTop = listBottom + GSRRunHistoryParameters.SELECTION_SCROLL_BEHIND_HEIGHT;

            context.fill(listLeft, containerTop, listLeft + listWidth, containerBottom, GSRUiParameters.CONTENT_BOX_BG);

            int listBottomExtended = delimiterTop + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT;
            if (model.deathDropdownOpen && !model.getSelectablePlayers().isEmpty()) {
                deathDropdown.renderOverlay(model, context, textRenderer, tickerState,
                        listLeft, overlayTop, listBottomExtended, listWidth,
                        model.deathDropdownScroll, mouseX, mouseY);
            } else if (model.healthDropdownOpen && !model.getSelectablePlayers().isEmpty()) {
                healthDropdown.renderOverlay(model, context, textRenderer, tickerState,
                        listLeft, overlayTop, listBottomExtended, listWidth,
                        model.healthDropdownScroll, mouseX, mouseY);
            }
            context.fill(listLeft, delimiterTop, listLeft + listWidth,
                    delimiterTop + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT,
                    GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_COLOR);

            boolean hasPending = model.hasPendingDeathChanges() || model.hasPendingHealthChanges();
            boolean confirmHover = mouseX >= listLeft && mouseX < listLeft + listWidth
                    && mouseY >= confirmButtonTop && mouseY < confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT;
            var textures = GSRPressableWidgetAccessor.gsr$getTextures();
            var confirmTexture = textures.get(true, confirmHover);
            int confirmLeft = listLeft + GSRRunHistoryParameters.CONTAINER_INSET;
            int confirmWidth = listWidth - 2 * GSRRunHistoryParameters.CONTAINER_INSET;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, confirmTexture, confirmLeft, confirmButtonTop, confirmWidth,
                    GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT);
            if (hasPending) {
                float breath = (float) ((1 + Math.sin(System.currentTimeMillis() * Math.PI * 2 / GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_PERIOD_MS)) / 2);
                float alpha = GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MIN
                        + (GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MAX - GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MIN) * breath;
                int glowColor = GSRColorHelper.applyAlpha(GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_GLOW_COLOR, alpha);
                int border = GSRRunHistoryParameters.MAKE_SELECTION_GLOW_BORDER;
                context.fill(confirmLeft - border, confirmButtonTop - border, confirmLeft + confirmWidth + border, confirmButtonTop, glowColor);
                context.fill(confirmLeft - border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, confirmLeft + confirmWidth + border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT + border, glowColor);
                context.fill(confirmLeft - border, confirmButtonTop, confirmLeft, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, glowColor);
                context.fill(confirmLeft + confirmWidth, confirmButtonTop, confirmLeft + confirmWidth + border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, glowColor);
            }
            int confirmCenterX = listLeft + listWidth / 2;
            int confirmTextY = confirmButtonTop + (GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT - textRenderer.fontHeight) / 2;
            context.drawCenteredTextWithShadow(textRenderer, GSRMultiSelectDropdown.CONFIRM_BUTTON_TEXT, confirmCenterX, confirmTextY, GSRRunHistoryParameters.TEXT_COLOR);
        } else {
            context.fill(listLeft, containerTop, listLeft + listWidth, containerBottom, GSRUiParameters.CONTENT_BOX_BG);
            int deathSectionTop = containerTop;
            int deathBarTop = deathSectionTop + GSRUiParameters.RUN_MANAGER_LABEL_TOP_OFFSET;
            int healthSectionTop = deathSectionTop + GSRUiParameters.RUN_MANAGER_SECTION_HEIGHT + GSRUiParameters.RUN_MANAGER_SECTION_GAP;
            int healthBarTop = healthSectionTop + GSRUiParameters.RUN_MANAGER_LABEL_TOP_OFFSET;

            deathDropdown.renderTrigger(model, context, textRenderer, tickerState,
                    listLeft, deathSectionTop, deathBarTop, listWidth, barHeight, labelScale,
                    false, deathDropdown.isBarHovered(listLeft, deathBarTop, listWidth, barHeight, mouseX, mouseY));
            healthDropdown.renderTrigger(model, context, textRenderer, tickerState,
                    listLeft, healthSectionTop, healthBarTop, listWidth, barHeight, labelScale,
                    false, healthDropdown.isBarHovered(listLeft, healthBarTop, listWidth, barHeight, mouseX, mouseY));
        }
    }
}
