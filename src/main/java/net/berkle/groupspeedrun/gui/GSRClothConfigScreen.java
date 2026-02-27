package net.berkle.groupspeedrun.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.client.GSRKeyBindings;
import net.berkle.groupspeedrun.network.GSRWorldConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRBastionIconOption;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRFortressIconOption;
import net.berkle.groupspeedrun.config.GSREndShowTicksOption;
import net.berkle.groupspeedrun.config.GSRHudLookMode;
import net.berkle.groupspeedrun.config.GSRHudPaddingOption;
import net.berkle.groupspeedrun.config.GSRHudVisibilityMode;
import net.berkle.groupspeedrun.config.GSRIconScaleOption;
import net.berkle.groupspeedrun.config.GSRLocatorColorOption;
import net.berkle.groupspeedrun.config.GSRLocatorNonAdminMode;
import net.berkle.groupspeedrun.config.GSRMaxScaleDistOption;
import net.berkle.groupspeedrun.config.GSRRowHeightOption;
import net.berkle.groupspeedrun.config.GSRScaleOption;
import net.berkle.groupspeedrun.config.GSRSeparatorAlphaOption;
import net.berkle.groupspeedrun.config.GSRShipIconOption;
import net.berkle.groupspeedrun.config.GSRSplitGapOption;
import net.berkle.groupspeedrun.config.GSRSplitShowTicksOption;
import net.berkle.groupspeedrun.config.GSRStrongholdIconOption;
import net.berkle.groupspeedrun.parameter.GSRKeyBindingParameters;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.Arrays;

/**
 * Cloth Config screen for GSR (Mod Menu → Configure). Settings are per-player (saved by UUID on server).
 */
@SuppressWarnings("null")
public final class GSRClothConfigScreen {

    private GSRClothConfigScreen() {}

    public static Screen create(Screen parent) {
        GSRConfigPlayer config = GSRClient.PLAYER_CONFIG;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("GSR Preferences"))
                .setSavingRunnable(() -> {
                    config.clampAll();
                    int prevVisibility = GSRClient.PLAYER_CONFIG.hudVisibility;
                    NbtCompound nbt = new NbtCompound();
                    config.writeNbt(nbt);
                    GSRClient.PLAYER_CONFIG.readNbt(nbt);
                    if (config.hudVisibility == GSRConfigPlayer.VISIBILITY_TOGGLE
                            && prevVisibility == GSRConfigPlayer.VISIBILITY_PRESSED) {
                        GSRClient.setHudToggledVisible(true);
                    }
                    GSRClient.setPreviousHudVisibility(config.hudVisibility);
                    if (MinecraftClient.getInstance().player != null) {
                        ClientPlayNetworking.send(new GSRConfigPayload(nbt));
                    }
                });

        ConfigEntryBuilder entry = builder.entryBuilder();

        ConfigCategory timerHud = builder.getOrCreateCategory(Text.literal("Timer HUD"));
        timerHud.addEntry(gsr$enumDropdown(entry, Text.literal("HUD Look"), GSRHudLookMode.from(config.hudMode), GSRHudLookMode.values(),
                e -> ((GSRHudLookMode) e).getDisplayName(),
                v -> config.hudMode = ((GSRHudLookMode) v).getValue())
                .setDefaultValue(GSRHudLookMode.FULL)
                .setTooltip(Text.literal("Full: always show timer and splits. Condensed: timer always visible; splits only during event window (start, stop, split)."))
                .build());
        timerHud.addEntry(gsr$enumDropdown(entry, Text.literal("Overall Scale"), GSRScaleOption.from(config.hudOverallScale), GSRScaleOption.values(),
                e -> ((GSRScaleOption) e).getDisplayName(),
                v -> config.hudOverallScale = GSRConfigPlayer.clampOverallScale(((GSRScaleOption) v).getValue()))
                .setDefaultValue(GSRScaleOption.S100)
                .build());
        timerHud.addEntry(gsr$enumDropdown(entry, Text.literal("Timer Scale"), GSRScaleOption.from(config.timerScale), GSRScaleOption.values(),
                e -> ((GSRScaleOption) e).getDisplayName(),
                v -> config.timerScale = GSRConfigPlayer.clampOverallScale(((GSRScaleOption) v).getValue()))
                .setDefaultValue(GSRScaleOption.S100)
                .build());
        timerHud.addEntry(entry.startBooleanToggle(Text.literal("Timer on Right"), config.timerHudOnRight)
                .setDefaultValue(true)
                .setSaveConsumer(v -> config.timerHudOnRight = v)
                .build());
        timerHud.addEntry(gsr$visibilityEntry(entry, config));

        ConfigCategory locatorHud = builder.getOrCreateCategory(Text.literal("Locator HUD"));
        locatorHud.addEntry(gsr$visibilityEntry(entry, config));
        locatorHud.addEntry(entry.startBooleanToggle(Text.literal("Compass at Top"), config.locateHudOnTop)
                .setDefaultValue(true)
                .setTooltip(Text.literal("When on, the structure compass bar is at the top of the screen; when off, at the bottom."))
                .setSaveConsumer(v -> config.locateHudOnTop = v)
                .build());
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Locate Scale"), GSRScaleOption.from(config.locateScale), GSRScaleOption.values(),
                e -> ((GSRScaleOption) e).getDisplayName(),
                v -> config.locateScale = GSRConfigPlayer.clampOverallScale(((GSRScaleOption) v).getValue()))
                .setDefaultValue(GSRScaleOption.S100)
                .build());
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Minimum Scale Distance"), GSRMaxScaleDistOption.from(config.maxScaleDistance), GSRMaxScaleDistOption.values(),
                e -> ((GSRMaxScaleDistOption) e).getDisplayName(),
                v -> config.maxScaleDistance = Math.max(GSRConfigPlayer.MIN_MAX_SCALE_DIST, Math.min(GSRConfigPlayer.MAX_MAX_SCALE_DIST, ((GSRMaxScaleDistOption) v).getValue())))
                .setDefaultValue(GSRMaxScaleDistOption.D1000)
                .setTooltip(Text.literal("Distance at which icons reach max scale (closer = larger)."))
                .build());
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Min Icon Scale"), GSRIconScaleOption.from(config.minIconScale), GSRIconScaleOption.values(),
                e -> ((GSRIconScaleOption) e).getDisplayName(),
                v -> config.minIconScale = Math.max(GSRConfigPlayer.MIN_ICON_SCALE, Math.min(GSRConfigPlayer.MAX_ICON_SCALE, ((GSRIconScaleOption) v).getValue())))
                .setDefaultValue(GSRIconScaleOption.I30)
                .build());

        int fortressDefColor = GSRLocatorParameters.DEFAULT_FORTRESS_COLOR;
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Fortress Icon"), GSRFortressIconOption.from(config.fortressItem), GSRFortressIconOption.values(),
                e -> ((GSRFortressIconOption) e).getDisplayName(),
                v -> config.fortressItem = ((GSRFortressIconOption) v).getRegistryId())
                .setDefaultValue(GSRFortressIconOption.DEFAULT)
                .build());
        locatorHud.addEntry(entry.startDropdownMenu(Text.literal("Fortress Color"), GSRLocatorColorOption.from(config.fortressColor, fortressDefColor),
                        s -> gsr$parseEnum(s, GSRLocatorColorOption.values(), e -> ((GSRLocatorColorOption) e).getDisplayName(fortressDefColor)),
                        e -> ((GSRLocatorColorOption) e).getDisplayName(fortressDefColor))
                .setSelections(Arrays.asList(GSRLocatorColorOption.values()))
                .setDefaultValue(GSRLocatorColorOption.DEFAULT)
                .setSaveConsumer(v -> config.fortressColor = ((GSRLocatorColorOption) v).getValue(fortressDefColor))
                .build());
        int bastionDefColor = GSRLocatorParameters.DEFAULT_BASTION_COLOR;
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Bastion Icon"), GSRBastionIconOption.from(config.bastionItem), GSRBastionIconOption.values(),
                e -> ((GSRBastionIconOption) e).getDisplayName(),
                v -> config.bastionItem = ((GSRBastionIconOption) v).getRegistryId())
                .setDefaultValue(GSRBastionIconOption.DEFAULT)
                .build());
        locatorHud.addEntry(entry.startDropdownMenu(Text.literal("Bastion Color"), GSRLocatorColorOption.from(config.bastionColor, bastionDefColor),
                        s -> gsr$parseEnum(s, GSRLocatorColorOption.values(), e -> ((GSRLocatorColorOption) e).getDisplayName(bastionDefColor)),
                        e -> ((GSRLocatorColorOption) e).getDisplayName(bastionDefColor))
                .setSelections(Arrays.asList(GSRLocatorColorOption.values()))
                .setDefaultValue(GSRLocatorColorOption.DEFAULT)
                .setSaveConsumer(v -> config.bastionColor = ((GSRLocatorColorOption) v).getValue(bastionDefColor))
                .build());
        int strongholdDefColor = GSRLocatorParameters.DEFAULT_STRONGHOLD_COLOR;
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Stronghold Icon"), GSRStrongholdIconOption.from(config.strongholdItem), GSRStrongholdIconOption.values(),
                e -> ((GSRStrongholdIconOption) e).getDisplayName(),
                v -> config.strongholdItem = ((GSRStrongholdIconOption) v).getRegistryId())
                .setDefaultValue(GSRStrongholdIconOption.DEFAULT)
                .build());
        locatorHud.addEntry(entry.startDropdownMenu(Text.literal("Stronghold Color"), GSRLocatorColorOption.from(config.strongholdColor, strongholdDefColor),
                        s -> gsr$parseEnum(s, GSRLocatorColorOption.values(), e -> ((GSRLocatorColorOption) e).getDisplayName(strongholdDefColor)),
                        e -> ((GSRLocatorColorOption) e).getDisplayName(strongholdDefColor))
                .setSelections(Arrays.asList(GSRLocatorColorOption.values()))
                .setDefaultValue(GSRLocatorColorOption.DEFAULT)
                .setSaveConsumer(v -> config.strongholdColor = ((GSRLocatorColorOption) v).getValue(strongholdDefColor))
                .build());
        int shipDefColor = GSRLocatorParameters.DEFAULT_SHIP_COLOR;
        locatorHud.addEntry(gsr$enumDropdown(entry, Text.literal("Wings (Ship) Icon"), GSRShipIconOption.from(config.shipItem), GSRShipIconOption.values(),
                e -> ((GSRShipIconOption) e).getDisplayName(),
                v -> config.shipItem = ((GSRShipIconOption) v).getRegistryId())
                .setDefaultValue(GSRShipIconOption.DEFAULT)
                .build());
        locatorHud.addEntry(entry.startDropdownMenu(Text.literal("Wings (Ship) Color"), GSRLocatorColorOption.from(config.shipColor, shipDefColor),
                        s -> gsr$parseEnum(s, GSRLocatorColorOption.values(), e -> ((GSRLocatorColorOption) e).getDisplayName(shipDefColor)),
                        e -> ((GSRLocatorColorOption) e).getDisplayName(shipDefColor))
                .setSelections(Arrays.asList(GSRLocatorColorOption.values()))
                .setDefaultValue(GSRLocatorColorOption.DEFAULT)
                .setSaveConsumer(v -> config.shipColor = ((GSRLocatorColorOption) v).getValue(shipDefColor))
                .build());

        ConfigCategory style = builder.getOrCreateCategory(Text.literal("Styling"));
        style.addEntry(gsr$enumDropdown(entry, Text.literal("HUD Padding"), GSRHudPaddingOption.from(config.hudPadding), GSRHudPaddingOption.values(),
                e -> ((GSRHudPaddingOption) e).getDisplayName(),
                v -> config.hudPadding = Math.max(GSRConfigPlayer.MIN_HUD_PADDING, Math.min(GSRConfigPlayer.MAX_HUD_PADDING, ((GSRHudPaddingOption) v).getValue())))
                .setDefaultValue(GSRHudPaddingOption.P6)
                .build());
        style.addEntry(gsr$enumDropdown(entry, Text.literal("Row Height"), GSRRowHeightOption.from(config.hudRowHeight), GSRRowHeightOption.values(),
                e -> ((GSRRowHeightOption) e).getDisplayName(),
                v -> config.hudRowHeight = Math.max(GSRConfigPlayer.MIN_HUD_ROW_HEIGHT, Math.min(GSRConfigPlayer.MAX_HUD_ROW_HEIGHT, ((GSRRowHeightOption) v).getValue())))
                .setDefaultValue(GSRRowHeightOption.R10)
                .build());
        style.addEntry(gsr$enumDropdown(entry, Text.literal("Split Gap"), GSRSplitGapOption.from(config.hudSplitGap), GSRSplitGapOption.values(),
                e -> ((GSRSplitGapOption) e).getDisplayName(),
                v -> config.hudSplitGap = Math.max(GSRConfigPlayer.MIN_HUD_SPLIT_GAP, Math.min(GSRConfigPlayer.MAX_HUD_SPLIT_GAP, ((GSRSplitGapOption) v).getValue())))
                .setDefaultValue(GSRSplitGapOption.G2)
                .build());
        style.addEntry(gsr$enumDropdown(entry, Text.literal("Separator Alpha"), GSRSeparatorAlphaOption.from(config.hudSeparatorAlpha), GSRSeparatorAlphaOption.values(),
                e -> ((GSRSeparatorAlphaOption) e).getDisplayName(),
                v -> config.hudSeparatorAlpha = Math.max(GSRConfigPlayer.MIN_SEPARATOR_ALPHA, Math.min(GSRConfigPlayer.MAX_SEPARATOR_ALPHA, ((GSRSeparatorAlphaOption) v).getValue())))
                .setDefaultValue(GSRSeparatorAlphaOption.A31)
                .build());
        style.addEntry(gsr$enumDropdown(entry, Text.literal("Split Show (s)"), GSRSplitShowTicksOption.from(config.splitShowTicks), GSRSplitShowTicksOption.values(),
                e -> ((GSRSplitShowTicksOption) e).getDisplayName(),
                v -> config.splitShowTicks = Math.max(GSRConfigPlayer.MIN_SPLIT_SHOW_TICKS, Math.min(GSRConfigPlayer.MAX_SPLIT_SHOW_TICKS, ((GSRSplitShowTicksOption) v).getValue())))
                .setDefaultValue(GSRSplitShowTicksOption.S10)
                .setTooltip(Text.literal("How long the split list stays visible after a split (seconds)."))
                .build());
        style.addEntry(gsr$enumDropdown(entry, Text.literal("End Show (s)"), GSREndShowTicksOption.from(config.endShowTicks), GSREndShowTicksOption.values(),
                e -> ((GSREndShowTicksOption) e).getDisplayName(),
                v -> config.endShowTicks = Math.max(GSRConfigPlayer.MIN_END_SHOW_TICKS, Math.min(GSRConfigPlayer.MAX_END_SHOW_TICKS, ((GSREndShowTicksOption) v).getValue())))
                .setDefaultValue(GSREndShowTicksOption.S30)
                .setTooltip(Text.literal("How long the HUD stays visible after victory/fail (seconds)."))
                .build());

        ConfigCategory modSettings = builder.getOrCreateCategory(Text.literal("Mod Settings"));
        modSettings.addEntry(entry.startBooleanToggle(Text.literal("Anti-Cheat (Invalidate Run)"), GSRClient.clientWorldConfig.antiCheatEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("When ON (host only): using locators invalidates the run for ranking. When OFF: locator use does not invalidate. Only the host's setting applies; other players' settings are ignored."))
                .setSaveConsumer(v -> {
                    GSRClient.clientWorldConfig.antiCheatEnabled = v;
                    if (MinecraftClient.getInstance().player != null) {
                        ClientPlayNetworking.send(new GSRWorldConfigPayload(GSRWorldConfigPayload.fromConfig()));
                    }
                })
                .build());
        modSettings.addEntry(entry.startBooleanToggle(Text.literal("Auto Start"), GSRClient.clientWorldConfig.autoStartEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("When ON (default): run auto-starts on first movement or block break. When OFF: admin must press Start Run to begin."))
                .setSaveConsumer(v -> {
                    GSRClient.clientWorldConfig.autoStartEnabled = v;
                    if (MinecraftClient.getInstance().player != null) {
                        ClientPlayNetworking.send(new GSRWorldConfigPayload(GSRWorldConfigPayload.fromConfig()));
                    }
                })
                .build());
        modSettings.addEntry(gsr$enumDropdown(entry, Text.literal("Locator Non-Admin Mode"),
                        GSRLocatorNonAdminMode.from(GSRClient.clientWorldConfig.locatorNonAdminMode), GSRLocatorNonAdminMode.values(),
                        e -> ((GSRLocatorNonAdminMode) e).getDisplayName(),
                        v -> {
                            GSRClient.clientWorldConfig.locatorNonAdminMode = ((GSRLocatorNonAdminMode) v).getValue();
                            if (MinecraftClient.getInstance().player != null) {
                                ClientPlayNetworking.send(new GSRWorldConfigPayload(GSRWorldConfigPayload.fromConfig()));
                            }
                        })
                .setDefaultValue(GSRLocatorNonAdminMode.POST_SPLIT_30MIN)
                .setTooltip(Text.literal("When non-admins can use locator bar: Never = disabled. Always = no time gate. 30 min post split = fortress/bastion 30 min after Nether; stronghold 30 min after overworld return (fortress+bastion done); wings 30 min after dragon. Host only."))
                .build());
        modSettings.addEntry(entry.startBooleanToggle(Text.literal("New World Before Run Ends"), config.allowNewWorldBeforeRunEnd)
                .setDefaultValue(false)
                .setTooltip(Text.literal("When ON (host only): allows the New World key to be used before the run has ended. When OFF (default): New World key only works after victory or fail."))
                .setSaveConsumer(v -> config.allowNewWorldBeforeRunEnd = v)
                .build());

        ConfigCategory keybinds = builder.getOrCreateCategory(Text.literal("Keybinds"));
        keybinds.addEntry(entry.fillKeybindingField(Text.translatable(GSRKeyBindingParameters.TOGGLE_HUD_KEY), GSRKeyBindings.toggleGsrHudKey)
                .setTooltip(Text.literal("Toggle Timer and Locator HUD on/off. Used when Visibility is \"Toggle (V)\"."))
                .build());
        keybinds.addEntry(entry.fillKeybindingField(Text.translatable(GSRKeyBindingParameters.PRESS_TO_SHOW_HUD_KEY), GSRKeyBindings.pressToShowGsrHudKey)
                .setTooltip(Text.literal("Hold to show Timer and Locator HUD. Used when Visibility is Hold."))
                .build());
        keybinds.addEntry(entry.fillKeybindingField(Text.translatable(GSRKeyBindingParameters.OPEN_CONFIG_KEY), GSRKeyBindings.openGsrConfigKey)
                .setTooltip(Text.literal("Hold G and press this key to open GSR Preferences (config)."))
                .build());
        keybinds.addEntry(entry.fillKeybindingField(Text.translatable(GSRKeyBindingParameters.OPEN_OPTIONS_KEY), GSRKeyBindings.openGsrOptionsKey)
                .setTooltip(Text.literal("Opens the GSR run controls screen."))
                .build());
        keybinds.addEntry(entry.fillKeybindingField(Text.translatable(GSRKeyBindingParameters.NEW_WORLD_KEY), GSRKeyBindings.newGsrWorldKey)
                .setTooltip(Text.literal("After victory or fail, starts new run and returns to world list."))
                .build());

        return builder.build();
    }

    /**
     * Shared Visibility entry for Timer and Locator HUD (both use the same config; changing one updates both).
     * Uses dropdown instead of cycling button for consistency with other options.
     */
    private static AbstractConfigListEntry<?> gsr$visibilityEntry(ConfigEntryBuilder entry, GSRConfigPlayer config) {
        GSRHudVisibilityMode current = GSRHudVisibilityMode.from(config.hudVisibility);
        return entry.startDropdownMenu(Text.literal("Visibility"), current,
                        s -> gsr$parseEnum(s, GSRHudVisibilityMode.values(), e -> {
                            GSRHudVisibilityMode m = (GSRHudVisibilityMode) e;
                            if (m == GSRHudVisibilityMode.PRESSED) {
                                String key = GSRKeyBindings.pressToShowGsrHudKey != null ? GSRKeyBindings.pressToShowGsrHudKey.getBoundKeyLocalizedText().getString() : "Tab";
                                return Text.literal("Hold (" + key + ")");
                            }
                            String key = GSRKeyBindings.toggleGsrHudKey != null ? GSRKeyBindings.toggleGsrHudKey.getBoundKeyLocalizedText().getString() : "V";
                            return Text.literal("Toggle (" + key + ")");
                        }),
                        e -> {
                            GSRHudVisibilityMode m = (GSRHudVisibilityMode) e;
                            if (m == GSRHudVisibilityMode.PRESSED) {
                                String key = GSRKeyBindings.pressToShowGsrHudKey != null ? GSRKeyBindings.pressToShowGsrHudKey.getBoundKeyLocalizedText().getString() : "Tab";
                                return Text.literal("Hold (" + key + ")");
                            }
                            String key = GSRKeyBindings.toggleGsrHudKey != null ? GSRKeyBindings.toggleGsrHudKey.getBoundKeyLocalizedText().getString() : "V";
                            return Text.literal("Toggle (" + key + ")");
                        })
                .setSelections(Arrays.asList(GSRHudVisibilityMode.values()))
                .setDefaultValue(GSRHudVisibilityMode.PRESSED)
                .setTooltip(Text.literal("Affects both Timer and Locator HUD. Toggle (V): press to show/hide. Hold (Tab): hold key to show, release to hide with symmetric fade."))
                .setSaveConsumer(v -> config.hudVisibility = ((GSRHudVisibilityMode) v).getValue())
                .build();
    }

    /**
     * Builds a dropdown entry for an enum (opens list on click instead of cycling).
     * Call .setDefaultValue(), .setTooltip() as needed, then .build().
     */
    private static <E extends Enum<E>> DropdownMenuBuilder<E> gsr$enumDropdown(ConfigEntryBuilder entry, Text title, E current, E[] values,
            java.util.function.Function<E, Text> displayFn,
            java.util.function.Consumer<E> saveConsumer) {
        return entry.startDropdownMenu(title, current,
                        s -> gsr$parseEnum(s, values, displayFn),
                        displayFn)
                .setSelections(Arrays.asList(values))
                .setSaveConsumer(saveConsumer);
    }

    /**
     * Parses a search string to an enum by matching display text or enum name.
     * Returns null if no match.
     */
    private static <E extends Enum<E>> E gsr$parseEnum(String s, E[] values, java.util.function.Function<E, Text> displayFn) {
        String x = s == null ? "" : s.trim();
        if (x.isEmpty()) return null;
        for (E e : values) {
            if (displayFn.apply(e).getString().equalsIgnoreCase(x)) return e;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<E> clazz = (Class<E>) values[0].getClass();
            return Enum.valueOf(clazz, x);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
