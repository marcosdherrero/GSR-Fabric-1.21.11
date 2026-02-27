/**
 * Centralized parameters for the GSR mod, grouped by usage.
 * <ul>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRHudParameters} – HUD display modes, scale/layout bounds, fade, timer positioning</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRLocatorParameters} – Locator bar layout, icon scaling, per-structure colors</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRPlayerConfigParameters} – Per-player HUD config NBT keys</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters} – World run config NBT keys and save file name</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRSharedHealthParameters} – Shared health eat limit (exhaustion allowance, damage exempt)</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRKeyBindingParameters} – Keybind translation keys and default key codes</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRUiParameters} – Status/Controls screen layout and message strings</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRButtonParameters} – Button labels and screen titles</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters} – Run History screen layout and colors</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRTooltipParameters} – Standardized tooltip size, scroll, and styling</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRServerParameters} – Server tick intervals (save, split check)</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRNetworkParameters} – Payload size limits and validation</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRStorageParameters} – DataStore, stats, snapshot, and run-count file names</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRBroadcastParameters} – Run-end broadcast bar length and stat epsilon</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRTrackerParameters} – Tracker mixin constants (jitter, teleport threshold)</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRTimerConfig} – Timer HUD labels, icons, split names, default values</li>
 *   <li>{@link net.berkle.groupspeedrun.parameter.GSRStatTrackerParameters} – Stat tracker display names, icons, units</li>
 * </ul>
 */
package net.berkle.groupspeedrun.parameter;
