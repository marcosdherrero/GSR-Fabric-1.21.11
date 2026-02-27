package net.berkle.groupspeedrun.gui.runhistory;

// GSR run record status constants
import net.berkle.groupspeedrun.data.GSRRunRecord;
// GSR run save state (record, participants, snapshots)
import net.berkle.groupspeedrun.data.GSRRunSaveState;
// GSR standard tooltip with player header and chart
import net.berkle.groupspeedrun.gui.GSRStandardTooltip;
// GSR ticker state for hover-scroll
import net.berkle.groupspeedrun.gui.GSRTickerState;
// Run History layout, colors, bar styling
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
// Time formatting for Run Time / Split Times
import net.berkle.groupspeedrun.util.GSRFormatUtil;
// Minecraft client for skin provider
import net.minecraft.client.MinecraftClient;
// Minecraft text measurement
import net.minecraft.client.font.TextRenderer;
// Minecraft screen drawing
import net.minecraft.client.gui.DrawContext;
// Player head drawing from skin textures
import net.minecraft.client.gui.PlayerSkinDrawer;
// Skin textures for player head
import net.minecraft.entity.player.SkinTextures;

// Mojang authlib for GameProfile (skin fetch)
import com.mojang.authlib.GameProfile;

// Java: HTTP client for Mojang API; charset for offline UUID detection
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Renders the player-level comparison chart for Run History.
 * Uses player comparison: each player's stats from their runs (where they participated),
 * not shared runs. Supports aggregation types: Avg, Best Success, Best Fail, Worst Success, Worst Fail, Avg Success, Avg Fail.
 */
public final class GSRRunHistoryPlayerChartRenderer {


    /** Type 0=Avg, 1=Best Success, 2=Best Fail, 3=Worst Success, 4=Worst Fail, 5=Avg Success, 6=Avg Fail. */
    private static final int TYPE_AVG = 0;
    private static final int TYPE_BEST_SUCCESS = 1;
    private static final int TYPE_BEST_FAIL = 2;
    private static final int TYPE_WORST_SUCCESS = 3;
    private static final int TYPE_WORST_FAIL = 4;
    private static final int TYPE_AVG_SUCCESS = 5;
    private static final int TYPE_AVG_FAIL = 6;

    /** View 0=Recent 5 runs per player, 1=Best 5 runs per player, 2=All time, 3=Worst 5 runs per player. */
    private static final int VIEW_RECENT_5 = 0;
    private static final int VIEW_BEST_5 = 1;
    private static final int VIEW_ALL = 2;
    private static final int VIEW_WORST_5 = 3;
    private static final int RUNS_PER_PLAYER_LIMIT = 5;

    /** Cache of player UUID -> SkinTextures for head drawing. Populated asynchronously. */
    private static final Map<UUID, SkinTextures> SKIN_CACHE = new ConcurrentHashMap<>();
    /** UUIDs we have already triggered a fetch for (avoid duplicate requests). */
    private static final Set<UUID> FETCH_PENDING = ConcurrentHashMap.newKeySet();
    /** Cache of Mojang username -> UUID for players with offline UUIDs or missing UUIDs. */
    private static final Map<String, UUID> MOJANG_UUID_CACHE = new ConcurrentHashMap<>();
    /** Usernames we are currently resolving via Mojang API (avoid duplicate requests). */
    private static final Set<String> MOJANG_PENDING_NAMES = ConcurrentHashMap.newKeySet();
    /** Mojang API base URL for username-to-UUID lookup. */
    private static final String MOJANG_API_BASE = "https://api.mojang.com/users/profiles/minecraft/";
    /** Length of UUID string in Mojang API response (no dashes). */
    private static final int MOJANG_UUID_STRING_LENGTH = 32;
    /** Shared HTTP client for Mojang API calls. */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    private GSRRunHistoryPlayerChartRenderer() {}

    /**
     * Renders player comparison bars. Uses player comparison: each player's stats from their runs
     * (where they participated), aggregated by type. View limits runs per player when Recent 5 or Best 5.
     *
     * @param typeIndex 0=Avg, 1=Best Success, 2=Best Fail, 3=Worst Success, 4=Worst Fail, 5=Avg Success, 6=Avg Fail.
     * @param viewIndex 0=Recent 5 runs per player, 1=Best 5 runs per player, 2=All time (all runs per player).
     * @return true if content was rendered; false if empty (no players or no runs).
     */
    public static boolean render(DrawContext context, TextRenderer textRenderer,
                                 GSRTickerState tickerState,
                                 GSRRunHistoryStatRow row, Set<String> selectedPlayers,
                                 List<GSRRunSaveState> runs, int typeIndex, int viewIndex,
                                 int left, int top, int right, int bottom, int scrollY,
                                 int mouseX, int mouseY) {
        if (runs.isEmpty()) return false;

        List<String> players = selectedPlayers.isEmpty()
                ? new ArrayList<>(collectAllPlayersFromRuns(runs))
                : new ArrayList<>(selectedPlayers);
        if (players.isEmpty()) return false;
        var acc = row.playerAccessor();
        boolean lowerIsBetter = row.isLowerBetter();

        List<String> labels = new ArrayList<>();
        double[] values;
        int[] colors;
        boolean[] hasValidData;

        double[] bestFailValues = null;
        double[] bestSuccessValues = null;
        double[] worstFailValues = null;
        double[] worstSuccessValues = null;

        if (typeIndex >= TYPE_BEST_SUCCESS && typeIndex <= TYPE_WORST_FAIL) {
            // Single value per player: Best Success, Best Fail, Worst Success, or Worst Fail.
            values = new double[players.size()];
            colors = new int[players.size()];
            hasValidData = new boolean[players.size()];
            for (int i = 0; i < players.size(); i++) {
                String p = players.get(i);
                List<GSRRunSaveState> playerRuns = limitRunsPerPlayer(
                        runs.stream().filter(r -> runHasPlayer(r, p)).toList(), p, viewIndex, acc, lowerIsBetter);
                GSRRunSaveState bestFailRun = null, bestSuccessRun = null, worstFailRun = null, worstSuccessRun = null;
                double bestFailVal = lowerIsBetter ? Double.MAX_VALUE : -1;
                double bestSuccessVal = lowerIsBetter ? Double.MAX_VALUE : -1;
                double worstFailVal = lowerIsBetter ? -1 : Double.MAX_VALUE;
                double worstSuccessVal = lowerIsBetter ? -1 : Double.MAX_VALUE;
                for (GSRRunSaveState run : playerRuns) {
                    boolean isVictory = GSRRunRecord.STATUS_VICTORY.equals(run.record().status());
                    boolean isFail = GSRRunRecord.STATUS_FAIL.equals(run.record().status());
                    if (!isVictory && !isFail) continue;
                    double v = acc.apply(p, run);
                    if (v < 0 && !lowerIsBetter) continue;
                    if (lowerIsBetter) {
                        if (isVictory && v < bestSuccessVal) { bestSuccessVal = v; bestSuccessRun = run; }
                        if (isFail && v < bestFailVal) { bestFailVal = v; bestFailRun = run; }
                        if (isVictory && v > worstSuccessVal) { worstSuccessVal = v; worstSuccessRun = run; }
                        if (isFail && v > worstFailVal) { worstFailVal = v; worstFailRun = run; }
                    } else {
                        if (isVictory && v > bestSuccessVal) { bestSuccessVal = v; bestSuccessRun = run; }
                        if (isFail && v > bestFailVal) { bestFailVal = v; bestFailRun = run; }
                        if (isVictory && v < worstSuccessVal && v >= 0) { worstSuccessVal = v; worstSuccessRun = run; }
                        if (isFail && v < worstFailVal && v >= 0) { worstFailVal = v; worstFailRun = run; }
                    }
                }
                // Best fail = worst score (longest before failing); Worst fail = best score (shortest before failing).
                boolean found = switch (typeIndex) {
                    case TYPE_BEST_SUCCESS -> bestSuccessRun != null;
                    case TYPE_BEST_FAIL -> worstFailRun != null;
                    case TYPE_WORST_SUCCESS -> worstSuccessRun != null;
                    case TYPE_WORST_FAIL -> bestFailRun != null;
                    default -> false;
                };
                double chosen = switch (typeIndex) {
                    case TYPE_BEST_SUCCESS -> found ? acc.apply(p, bestSuccessRun) : 0;
                    case TYPE_BEST_FAIL -> found ? acc.apply(p, worstFailRun) : 0;
                    case TYPE_WORST_SUCCESS -> found ? acc.apply(p, worstSuccessRun) : 0;
                    case TYPE_WORST_FAIL -> found ? acc.apply(p, bestFailRun) : 0;
                    default -> 0;
                };
                values[i] = chosen;
                hasValidData[i] = found;
                colors[i] = GSRRunHistoryParameters.BAR_COLOR_RECENT;
                labels.add(truncatePlayerLabel(p, GSRRunHistoryParameters.CHART_LABEL_MAX_WIDTH_UNLIMITED, textRenderer));
            }
        } else {
            values = new double[players.size()];
            colors = new int[players.size()];
            hasValidData = new boolean[players.size()];
            boolean successOnly = (typeIndex == TYPE_AVG_SUCCESS);
            boolean failOnly = (typeIndex == TYPE_AVG_FAIL);

            for (int i = 0; i < players.size(); i++) {
                String p = players.get(i);
                List<GSRRunSaveState> playerRuns = limitRunsPerPlayer(
                        runs.stream().filter(r -> runHasPlayer(r, p)).toList(), p, viewIndex, acc, lowerIsBetter);
                double sum = 0;
                int count = 0;
                for (GSRRunSaveState run : playerRuns) {
                    boolean isVictory = GSRRunRecord.STATUS_VICTORY.equals(run.record().status());
                    boolean isFail = GSRRunRecord.STATUS_FAIL.equals(run.record().status());
                    if (!isVictory && !isFail) continue;
                    if (successOnly && !isVictory) continue;
                    if (failOnly && !isFail) continue;
                    double v = acc.apply(p, run);
                    if (lowerIsBetter) {
                        if (v > 0) { sum += v; count++; }
                    } else if (v >= 0) {
                        sum += v;
                        count++;
                    }
                }
                values[i] = count > 0 ? sum / count : 0;
                hasValidData[i] = count > 0;
                colors[i] = GSRRunHistoryParameters.BAR_COLOR_RECENT;
                labels.add(truncatePlayerLabel(p, GSRRunHistoryParameters.CHART_LABEL_MAX_WIDTH_UNLIMITED, textRenderer));
            }
        }

        double globalMax = 0;
        for (int i = 0; i < values.length; i++) {
            if (hasValidData[i]) globalMax = Math.max(globalMax, values[i]);
        }
        if (globalMax < 0.001) globalMax = 1;

        sortByBestToWorstAndApplyGradient(players, values, labels, colors, hasValidData,
                bestFailValues, bestSuccessValues, worstFailValues, worstSuccessValues, lowerIsBetter);

        Map<String, UUID> nameToUuid = collectNameToUuidFromRuns(runs);
        for (String p : players) {
            ensureSkinFetch(nameToUuid.get(p), p);
        }

        int numBars = values.length;
        int chartWidth = right - left - GSRRunHistoryParameters.CONTENT_PADDING;
        int maxBarWidth = (int) (chartWidth * GSRRunHistoryParameters.BAR_MAX_WIDTH_FRACTION);
        int slotWidth = (chartWidth - GSRRunHistoryParameters.BAR_GAP * (numBars - 1)) / numBars;
        slotWidth = Math.min(Math.max(slotWidth, GSRRunHistoryParameters.PLAYER_CHART_MIN_SLOT_WIDTH), maxBarWidth);

        String typeDesc = switch (typeIndex) {
            case TYPE_BEST_SUCCESS -> "Best successful run per player. Sorted best to worst.";
            case TYPE_BEST_FAIL -> "Best failed run per player (longest before failing). Sorted best to worst.";
            case TYPE_WORST_SUCCESS -> "Worst successful run per player. Sorted best to worst.";
            case TYPE_WORST_FAIL -> "Worst failed run per player (shortest before failing). Sorted best to worst.";
            case TYPE_AVG_SUCCESS -> "Average of successful runs only. Sorted best to worst.";
            case TYPE_AVG_FAIL -> "Average of failed runs only. Sorted best to worst.";
            case TYPE_AVG -> "Average across each player's runs. Sorted best to worst.";
            default -> "Average across each player's runs. Sorted best to worst.";
        };
        String viewDesc = switch (viewIndex) {
            case VIEW_RECENT_5 -> " View: 5 most recent runs per player.";
            case VIEW_BEST_5 -> " View: 5 best runs per player.";
            case VIEW_WORST_5 -> " View: 5 worst runs per player.";
            default -> " View: All time (all runs per player).";
        };

        context.enableScissor(left, top, right, bottom);
        int contentHeight = bottom - top;
        // Use compact layout when container is small so chart fits without scroll.
        boolean compact = contentHeight < GSRRunHistoryParameters.CHART_COMPACT_THRESHOLD;
        int topOffset = compact ? 1 : 2;
        int margin = GSRRunHistoryChartRenderer.computeChartMargin(compact, right - left);

        int titleY = top + topOffset - scrollY;
        String titleWithUnits = row.chartTitle() + " (by player)" + row.unitSuffix();
        int titleMaxWidth = Math.max(1, right - left);
        int titleHeight = GSRRunHistoryChartRenderer.drawWrappedText(context, textRenderer, titleWithUnits,
                left, titleY, titleMaxWidth, GSRRunHistoryParameters.CHART_LABEL_COLOR);
        String descText = players.size() + " players across " + runs.size() + " run(s). " + typeDesc + viewDesc;
        int descY = titleY + titleHeight;
        int descHeight = GSRRunHistoryChartRenderer.drawWrappedText(context, textRenderer, descText,
                left, descY, titleMaxWidth, GSRRunHistoryParameters.BAR_LABEL_COLOR);

        int headerBottom = descY + descHeight;
        int fontHeight = textRenderer.fontHeight;
        int nameLabelGap = GSRRunHistoryParameters.CHART_LABEL_Y_OFFSET;
        int nameY = bottom - margin - fontHeight;
        int barBottom = nameY - nameLabelGap;
        int barAreaTop = headerBottom + margin;
        int availableBarHeight = Math.max(GSRRunHistoryParameters.BAR_HEIGHT, barBottom - barAreaTop);
        int barTop = Math.max(barAreaTop, barBottom - availableBarHeight);
        int valueInset = GSRRunHistoryParameters.CONTENT_PADDING;

        int x = left;
        int hoveredIndex = -1;
        for (int i = 0; i < numBars; i++) {
            context.fill(x, barTop, x + slotWidth, barBottom, GSRRunHistoryParameters.BAR_SLOT_BG);

            {
                int fillHeight = GSRRunHistoryChartRenderer.barFillHeight(values[i], globalMax, availableBarHeight);
                context.fill(x, barBottom - fillHeight, x + slotWidth, barBottom, colors[i]);
            }

            int valueY = barBottom - fontHeight - valueInset;
            String valueStr = formatValue(row, values[i]);
            int maxValueWidth = Math.max(1, slotWidth - 2 * valueInset);
            float valueScale = Math.min(1f, (float) maxValueWidth / textRenderer.getWidth(valueStr));
            drawScaledText(context, textRenderer, valueStr, x + valueInset, valueY, valueScale, GSRRunHistoryParameters.BAR_VALUE_COLOR);

            String playerName = players.get(i);
            UUID playerUuid = getEffectiveSkinUuid(playerName, nameToUuid);
            SkinTextures skin = getCachedSkin(playerUuid);
            int faceSize = GSRRunHistoryParameters.PLAYER_CHART_FACE_SIZE;
            int faceGap = GSRRunHistoryParameters.PLAYER_CHART_FACE_NAME_GAP;
            int nameX = x;
            if (skin != null && slotWidth >= faceSize + faceGap) {
                int faceY = nameY + (fontHeight - faceSize) / 2;
                PlayerSkinDrawer.draw(context, skin, x, faceY, faceSize);
                nameX = x + faceSize + faceGap;
            }
            int nameMaxWidth = slotWidth - (nameX - x);
            String namePart = truncatePlayerLabel(labels.get(i), Math.max(1, nameMaxWidth), textRenderer);
            context.drawText(textRenderer, namePart, nameX, nameY, GSRRunHistoryParameters.BAR_VALUE_COLOR, false);

            if (mouseX >= x && mouseX < x + slotWidth && mouseY >= barTop && mouseY < nameY + fontHeight) {
                hoveredIndex = i;
            }
            x += slotWidth + GSRRunHistoryParameters.BAR_GAP;
        }
        context.disableScissor();

        if (hoveredIndex >= 0) {
            String playerName = players.get(hoveredIndex);
            UUID playerUuid = getEffectiveSkinUuid(playerName, nameToUuid);
            SkinTextures skin = getCachedSkin(playerUuid);
            ensureSkinFetch(nameToUuid.get(playerName), playerName);

            List<GSRRunSaveState> allPlayerRuns = runs.stream().filter(r -> runHasPlayer(r, playerName)).toList();
            List<GSRRunSaveState> playerRuns = limitRunsPerPlayer(allPlayerRuns, playerName, viewIndex, acc, lowerIsBetter);
            String statsRow = buildTooltipStatsRow(row, playerRuns, typeIndex, playerName);
            String scoresRow = buildTooltipScoresRow(row, playerRuns, playerName);

            String barValueStr = formatValue(row, values[hoveredIndex]);
            String highlightedLine = buildHighlightedValueLine(row, barValueStr);

            long elapsed = tickerState.getTooltipElapsedMsUnbounded("tooltip-player-" + hoveredIndex + "-" + playerName, System.currentTimeMillis());
            int sw = context.getScaledWindowWidth();
            int sh = context.getScaledWindowHeight();
            GSRStandardTooltip.drawWithPlayerChartTooltip(context, textRenderer, skin, playerName, highlightedLine,
                    statsRow, scoresRow, GSRStandardTooltip.CURSOR_OFFSET, mouseX, mouseY, sw, sh, elapsed);
        }

        return true;
    }

    /**
     * Sorts players best-to-worst by value and applies green-to-red gradient colors.
     * Best = green, worst = red. For time stats, lowest is best; for others, highest is best.
     * When 4-segment arrays present, ranks by bestSuccess (or bestFail if no success).
     * Players without success/fail runs (hasValidData[i]=false) get neutral color and are excluded from gradient.
     */
    private static void sortByBestToWorstAndApplyGradient(List<String> players, double[] values, List<String> labels,
                                                          int[] colors, boolean[] hasValidData,
                                                          double[] bestFailValues, double[] bestSuccessValues,
                                                          double[] worstFailValues, double[] worstSuccessValues,
                                                          boolean lowerIsBetter) {
        int n = players.size();
        if (n == 0) return;

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;

        double[] rankValues;
        if (bestSuccessValues != null && bestFailValues != null) {
            rankValues = new double[n];
            for (int i = 0; i < n; i++) {
                double bs = bestSuccessValues[i] > 0 ? bestSuccessValues[i] : (lowerIsBetter ? Double.MAX_VALUE : -1);
                double bf = bestFailValues[i] > 0 ? bestFailValues[i] : (lowerIsBetter ? Double.MAX_VALUE : -1);
                rankValues[i] = (bs != (lowerIsBetter ? Double.MAX_VALUE : -1)) ? bs : bf;
            }
        } else {
            rankValues = values;
        }
        Comparator<Integer> cmp = lowerIsBetter
                ? Comparator.comparingDouble(i -> rankValues[i])
                : Comparator.<Integer>comparingDouble(i -> rankValues[i]).reversed();
        java.util.Arrays.sort(order, cmp);

        List<String> sortedPlayers = new ArrayList<>(n);
        List<String> sortedLabels = new ArrayList<>(n);
        double[] sortedValues = new double[n];
        boolean[] sortedHasValidData = new boolean[n];
        double[] sortedBF = bestFailValues != null ? new double[n] : null;
        double[] sortedBS = bestSuccessValues != null ? new double[n] : null;
        double[] sortedWF = worstFailValues != null ? new double[n] : null;
        double[] sortedWS = worstSuccessValues != null ? new double[n] : null;

        int validCount = 0;
        for (int i = 0; i < n; i++) {
            if (hasValidData[order[i]]) validCount++;
        }
        for (int i = 0; i < n; i++) {
            int idx = order[i];
            sortedPlayers.add(players.get(idx));
            sortedLabels.add(labels.get(idx));
            sortedValues[i] = values[idx];
            sortedHasValidData[i] = hasValidData[idx];
            if (!hasValidData[idx] || validCount <= 1) {
                colors[i] = GSRRunHistoryParameters.BAR_COLOR_RECENT;
            } else {
                int validRank = 0;
                for (int j = 0; j < i; j++) {
                    if (hasValidData[order[j]]) validRank++;
                }
                colors[i] = lerpColor(GSRRunHistoryParameters.PLAYER_CHART_COLOR_BEST,
                        GSRRunHistoryParameters.PLAYER_CHART_COLOR_WORST,
                        (double) validRank / (validCount - 1));
            }
            if (sortedBF != null && bestFailValues != null) sortedBF[i] = bestFailValues[idx];
            if (sortedBS != null && bestSuccessValues != null) sortedBS[i] = bestSuccessValues[idx];
            if (sortedWF != null && worstFailValues != null) sortedWF[i] = worstFailValues[idx];
            if (sortedWS != null && worstSuccessValues != null) sortedWS[i] = worstSuccessValues[idx];
        }

        players.clear();
        players.addAll(sortedPlayers);
        labels.clear();
        labels.addAll(sortedLabels);
        System.arraycopy(sortedValues, 0, values, 0, n);
        System.arraycopy(sortedHasValidData, 0, hasValidData, 0, n);
        if (bestFailValues != null && sortedBF != null) System.arraycopy(sortedBF, 0, bestFailValues, 0, n);
        if (bestSuccessValues != null && sortedBS != null) System.arraycopy(sortedBS, 0, bestSuccessValues, 0, n);
        if (worstFailValues != null && sortedWF != null) System.arraycopy(sortedWF, 0, worstFailValues, 0, n);
        if (worstSuccessValues != null && sortedWS != null) System.arraycopy(sortedWS, 0, worstSuccessValues, 0, n);
    }

    /** Interpolates ARGB color from a to b by fraction (0 = a, 1 = b). */
    private static int lerpColor(int a, int b, double fraction) {
        int aR = (a >> 16) & 0xFF, aG = (a >> 8) & 0xFF, aB = a & 0xFF;
        int bR = (b >> 16) & 0xFF, bG = (b >> 8) & 0xFF, bB = b & 0xFF;
        int r = (int) (aR + (bR - aR) * fraction);
        int g = (int) (aG + (bG - aG) * fraction);
        int bl = (int) (aB + (bB - aB) * fraction);
        return 0xFF000000 | (Math.max(0, Math.min(255, r)) << 16)
                | (Math.max(0, Math.min(255, g)) << 8)
                | Math.max(0, Math.min(255, bl));
    }

    /** Collects all unique player names from runs (participants and snapshots). Used when filter is "All". */
    private static Set<String> collectAllPlayersFromRuns(List<GSRRunSaveState> runs) {
        Set<String> names = new LinkedHashSet<>();
        for (GSRRunSaveState run : runs) {
            for (var p : run.participants()) {
                if (p.playerName() != null && !p.playerName().isBlank()) names.add(p.playerName());
            }
            for (var s : run.snapshots()) {
                if (s.playerName() != null && !s.playerName().isBlank()) names.add(s.playerName());
            }
        }
        return names;
    }

    /**
     * Builds name -> UUID map from participants and snapshots.
     * For the local player (Session username), uses Session UUID so Mojang skin fetches correctly.
     */
    private static Map<String, UUID> collectNameToUuidFromRuns(List<GSRRunSaveState> runs) {
        Map<String, UUID> out = new java.util.HashMap<>();
        for (GSRRunSaveState run : runs) {
            for (var p : run.participants()) {
                if (p.playerName() != null && !p.playerName().isBlank() && p.playerUuid() != null && !p.playerUuid().isBlank()) {
                    try {
                        out.putIfAbsent(p.playerName(), UUID.fromString(p.playerUuid()));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            for (var s : run.snapshots()) {
                if (s.playerName() != null && !s.playerName().isBlank() && s.playerUuid() != null && !s.playerUuid().isBlank()) {
                    try {
                        out.putIfAbsent(s.playerName(), UUID.fromString(s.playerUuid()));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        var client = MinecraftClient.getInstance();
        if (client != null && client.getSession() != null) {
            UUID sessionUuid = client.getSession().getUuidOrNull();
            if (sessionUuid != null) {
                String sessionName = client.getSession().getUsername();
                if (sessionName != null && !sessionName.isBlank()) out.put(sessionName, sessionUuid);
                if (client.player != null) {
                    String inGameName = client.player.getName().getString();
                    if (inGameName != null && !inGameName.isBlank()) out.put(inGameName, sessionUuid);
                }
            }
        }
        return out;
    }

    /**
     * Returns the UUID to use for skin lookup. For local player uses Session UUID.
     * For others with Mojang UUID uses that; for offline UUID or no UUID, uses Mojang API cache if present.
     */
    private static UUID getEffectiveSkinUuid(String playerName, Map<String, UUID> nameToUuid) {
        if (playerName == null || playerName.isBlank()) return null;
        UUID baseUuid = nameToUuid.get(playerName);
        if (baseUuid != null && !isOfflineUuid(baseUuid, playerName)) return baseUuid;
        return MOJANG_UUID_CACHE.get(playerName);
    }

    /** Returns true if uuid matches Minecraft's offline UUID for the given name. */
    private static boolean isOfflineUuid(UUID uuid, String name) {
        if (uuid == null || name == null || name.isBlank()) return false;
        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return uuid.equals(offline);
    }

    /**
     * Fetches skin textures for UUID and caches when complete. Triggers async fetch if not already cached.
     * For null or offline UUID, resolves Mojang UUID via API first; when resolved, fetches skin on client thread.
     */
    private static void ensureSkinFetch(UUID uuid, String name) {
        if (name == null || name.isBlank()) return;
        if (uuid != null && !isOfflineUuid(uuid, name)) {
            doSkinFetch(uuid, name);
            return;
        }
        UUID mojangUuid = MOJANG_UUID_CACHE.get(name);
        if (mojangUuid != null) {
            doSkinFetch(mojangUuid, name);
            return;
        }
        if (!MOJANG_PENDING_NAMES.add(name)) return;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOJANG_API_BASE + name.replace(" ", "%20")))
                .GET()
                .build();
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return null;
                    String body = response.body();
                    int idStart = body.indexOf("\"id\":\"");
                    if (idStart < 0) return null;
                    idStart += 6;
                    int idEnd = body.indexOf("\"", idStart);
                    if (idEnd < 0) return null;
                    String idStr = body.substring(idStart, idEnd);
                    if (idStr.length() != MOJANG_UUID_STRING_LENGTH) return null;
                    try {
                        return UUID.fromString(idStr.replaceFirst(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .exceptionally(throwable -> null)
                .thenAccept(resolvedUuid -> {
                    MOJANG_PENDING_NAMES.remove(name);
                    if (resolvedUuid != null) MOJANG_UUID_CACHE.put(name, resolvedUuid);
                    var client = MinecraftClient.getInstance();
                    if (client != null && resolvedUuid != null) {
                        client.execute(() -> doSkinFetch(resolvedUuid, name));
                    }
                });
    }

    /** Performs the actual skin fetch for a known Mojang UUID. */
    private static void doSkinFetch(UUID uuid, String name) {
        if (uuid == null) return;
        if (SKIN_CACHE.containsKey(uuid)) return;
        if (!FETCH_PENDING.add(uuid)) return;
        var client = MinecraftClient.getInstance();
        if (client == null || client.getSkinProvider() == null) {
            FETCH_PENDING.remove(uuid);
            return;
        }
        GameProfile profile = new GameProfile(uuid, name != null ? name : uuid.toString());
        client.getSkinProvider().fetchSkinTextures(profile).thenAccept(opt -> {
            opt.ifPresent(textures -> SKIN_CACHE.put(uuid, textures));
            FETCH_PENDING.remove(uuid);
        }).exceptionally(throwable -> {
            FETCH_PENDING.remove(uuid);
            return null;
        });
    }

    /** Returns cached SkinTextures for UUID, or null if not yet loaded. */
    private static SkinTextures getCachedSkin(UUID uuid) {
        return uuid != null ? SKIN_CACHE.get(uuid) : null;
    }

    /** Returns true if the run has this player in participants or snapshots. */
    private static boolean runHasPlayer(GSRRunSaveState run, String playerName) {
        for (var p : run.participants()) {
            if (playerName != null && playerName.equals(p.playerName())) return true;
        }
        for (var s : run.snapshots()) {
            if (playerName != null && playerName.equals(s.playerName())) return true;
        }
        return false;
    }

    /**
     * Limits runs per player when view is Recent 5 or Best 5. Recent 5: sort by endMs desc, take first 5.
     * Best 5: sort by accessor value (best first), take first 5. View 2 (All) returns list unchanged.
     */
    private static List<GSRRunSaveState> limitRunsPerPlayer(List<GSRRunSaveState> playerRuns, String playerName,
                                                           int viewIndex, BiFunction<String, GSRRunSaveState, Double> acc,
                                                           boolean lowerIsBetter) {
        if (viewIndex == VIEW_ALL || playerRuns.size() <= RUNS_PER_PLAYER_LIMIT) return playerRuns;
        if (viewIndex == VIEW_RECENT_5) {
            return playerRuns.stream()
                    .sorted(Comparator.comparingLong((GSRRunSaveState r) -> r.record().endMs()).reversed())
                    .limit(RUNS_PER_PLAYER_LIMIT)
                    .toList();
        }
        if (viewIndex == VIEW_BEST_5) {
            return playerRuns.stream()
                    .sorted((a, b) -> {
                        double va = acc.apply(playerName, a);
                        double vb = acc.apply(playerName, b);
                        if (va < 0 && vb >= 0) return 1;
                        if (va >= 0 && vb < 0) return -1;
                        return lowerIsBetter ? Double.compare(va, vb) : Double.compare(vb, va);
                    })
                    .limit(RUNS_PER_PLAYER_LIMIT)
                    .toList();
        }
        if (viewIndex == VIEW_WORST_5) {
            return playerRuns.stream()
                    .sorted((a, b) -> {
                        double va = acc.apply(playerName, a);
                        double vb = acc.apply(playerName, b);
                        if (va < 0 && vb >= 0) return 1;
                        if (va >= 0 && vb < 0) return -1;
                        return lowerIsBetter ? Double.compare(vb, va) : Double.compare(va, vb);
                    })
                    .limit(RUNS_PER_PLAYER_LIMIT)
                    .toList();
        }
        return playerRuns;
    }

    /** Draws text scaled to fit; scale &lt; 1 shrinks text. */
    private static void drawScaledText(DrawContext context, TextRenderer textRenderer,
                                       String text, int x, int y, float scale, int color) {
        if (scale >= 1f) {
            context.drawText(textRenderer, text, x, y, color, false);
        } else {
            var matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            context.drawText(textRenderer, text, 0, 0, color, false);
            matrices.popMatrix();
        }
    }

    /** Truncates player name with ellipsis if it exceeds maxWidth. */
    private static String truncatePlayerLabel(String name, int maxWidth, TextRenderer tr) {
        if (name == null) return "?";
        int w = tr.getWidth(name);
        if (w <= maxWidth) return name;
        for (int len = name.length() - 1; len > 0; len--) {
            String s = name.substring(0, len) + "…";
            if (tr.getWidth(s) <= maxWidth) return s;
        }
        return "…";
    }

    /** Builds highlighted value line for tooltip: stat label + value + unit (e.g. "Run Time: 12:34" or "Blaze Rods: 12 (collected)"). */
    private static String buildHighlightedValueLine(GSRRunHistoryStatRow row, String formattedValue) {
        if (formattedValue == null || formattedValue.isEmpty()) return "";
        String unit = row.unitSuffix();
        return "§f" + row.label() + ": §e" + formattedValue + (unit.isEmpty() ? "" : " §7" + unit);
    }

    /** Builds stats row for tooltip: Runs, wins, losses, Avg with labels and units. Excludes non-success/fail runs. */
    private static String buildTooltipStatsRow(GSRRunHistoryStatRow row, List<GSRRunSaveState> playerRuns,
                                               int typeIndex, String playerName) {
        if (playerRuns == null || playerRuns.isEmpty()) return "";
        var acc = row.playerAccessor();
        boolean lowerIsBetter = row.isLowerBetter();
        int victories = 0, fails = 0;
        double sum = 0;
        int count = 0;
        boolean successOnly = (typeIndex == TYPE_AVG_SUCCESS);
        boolean failOnly = (typeIndex == TYPE_AVG_FAIL);
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            boolean isVictory = GSRRunRecord.STATUS_VICTORY.equals(r.status());
            boolean isFail = GSRRunRecord.STATUS_FAIL.equals(r.status());
            if (!isVictory && !isFail) continue;
            if (isVictory) victories++;
            else fails++;
            if (successOnly && !isVictory) continue;
            if (failOnly && !isFail) continue;
            double v = acc.apply(playerName, s);
            if (lowerIsBetter) { if (v > 0) { sum += v; count++; } }
            else if (v >= 0) { sum += v; count++; }
        }
        int n = victories + fails;
        double avg = count > 0 ? sum / count : 0;
        String unitSuffix = row.unitSuffix();
        StringBuilder sb = new StringBuilder();
        sb.append("§7Runs: §f").append(n);
        if (victories > 0 || fails > 0) sb.append(" §7| §a").append(victories).append(" win §7| §c").append(fails).append(" loss");
        sb.append(" §7| §7Avg").append(unitSuffix.isEmpty() ? "" : " " + unitSuffix).append(": §f").append(formatValue(row, avg));
        return sb.toString();
    }

    /** Builds scores row for tooltip: stat label prefix + icon+value per run. Excludes non-success/fail runs. */
    private static String buildTooltipScoresRow(GSRRunHistoryStatRow row, List<GSRRunSaveState> playerRuns,
                                                String playerName) {
        if (playerRuns == null || playerRuns.isEmpty()) return "";
        var acc = row.playerAccessor();
        StringBuilder sb = new StringBuilder();
        sb.append("§7").append(row.label()).append(": ");
        String sep = "";
        for (GSRRunSaveState s : playerRuns) {
            GSRRunRecord r = s.record();
            boolean isVictory = GSRRunRecord.STATUS_VICTORY.equals(r.status());
            boolean isFail = GSRRunRecord.STATUS_FAIL.equals(r.status());
            if (!isVictory && !isFail) continue;
            String icon = isVictory ? GSRRunHistoryParameters.STATUS_ICON_VICTORY
                    : GSRRunHistoryParameters.STATUS_ICON_FAIL;
            double v = acc.apply(playerName, s);
            sb.append(sep).append(icon).append(" §f").append(formatValue(row, v));
            sep = "  ";
        }
        return sb.toString();
    }

    /** Formats stat value for display (time vs numeric). Damage in hearts when displayAsHearts. */
    private static String formatValue(GSRRunHistoryStatRow row, double value) {
        if ("Run Time".equals(row.label()) || "Split Times".equals(row.label())) {
            return GSRFormatUtil.formatTime((long) value);
        }
        if (row.displayAsHearts()) {
            return String.format("%.1f", value / 2.0);
        }
        return row.isInt() ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    public static int computeContentHeight() {
        return GSRRunHistoryChartRenderer.computeContentHeight();
    }
}
