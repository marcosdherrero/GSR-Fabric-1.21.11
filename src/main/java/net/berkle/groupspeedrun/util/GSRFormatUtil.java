package net.berkle.groupspeedrun.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Formatting helpers for GSR (timer display, status).
 * Timer format: MM:SS.cc with hundredths (.00-.99) as percent to next second;
 * hours or days prepended when necessary; all units zero-padded.
 * Numbers > {@link GSRBroadcastParameters#NUMBER_SCIENTIFIC_THRESHOLD} use scientific notation.
 */
public final class GSRFormatUtil {

    private GSRFormatUtil() {}

    private static final DecimalFormat COMMA_INT = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));

    /** Converts %.2e output (e.g. 1.23e+04) to compact form (1.23e4). */
    private static String formatScientific(double value) {
        String s = String.format(Locale.US, "%.2e", value);
        return s.replace("e+0", "e").replace("e+", "e").replace("e-0", "e-").replace("e-", "e-");
    }

    /** Formatter for readable date/time: MM/dd/yyyy h:mm a (e.g. 02/24/2025 3:30 PM) in local time. */
    private static final DateTimeFormatter DATE_READABLE = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a")
            .withZone(ZoneId.systemDefault());

    /**
     * Formats epoch milliseconds as readable date/time: MM/dd/yyyy h:mm a (e.g. 02/24/2025 3:30 PM).
     * Uses system default timezone.
     */
    public static String formatDateReadable(long epochMs) {
        return DATE_READABLE.format(Instant.ofEpochMilli(epochMs));
    }

    /**
     * Formats a number for run info and broadcasts. Values &le; 10,000 use comma-separated;
     * values &gt; 10,000 use scientific notation (e.g. 1.23e4).
     */
    public static String formatNumber(int value) {
        if (Math.abs(value) > GSRBroadcastParameters.NUMBER_SCIENTIFIC_THRESHOLD) {
            return formatScientific((double) value);
        }
        return COMMA_INT.format(value);
    }

    /**
     * Formats a number for run info and broadcasts. Values &le; 10,000 use comma-separated;
     * values &gt; 10,000 use scientific notation (e.g. 1.23e4).
     */
    public static String formatNumber(long value) {
        if (Math.abs(value) > GSRBroadcastParameters.NUMBER_SCIENTIFIC_THRESHOLD) {
            return formatScientific((double) value);
        }
        return COMMA_INT.format(value);
    }

    /**
     * Formats a number for run info and broadcasts. Values &le; 10,000 use one decimal place;
     * values &gt; 10,000 use scientific notation (e.g. 1.23e4).
     */
    public static String formatNumber(double value) {
        if (Math.abs(value) > GSRBroadcastParameters.NUMBER_SCIENTIFIC_THRESHOLD) {
            return formatScientific(value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    /** Formats damage (HP) as hearts for display. 1 heart = 2 HP. */
    public static String formatDamageAsHearts(double damage) {
        return formatNumber(damage / 2.0) + " hearts";
    }

    /**
     * Formats elapsed milliseconds as plain English: "X hours and X minutes and X seconds and X milliseconds".
     * Omits units that are zero (e.g. no hours when under 1 hour).
     * Uses singular (hour, minute, second, millisecond) when value is 1.
     */
    public static String formatRunTimePlainEnglish(long elapsedMs) {
        if (elapsedMs < 0) elapsedMs = 0;
        long totalMs = elapsedMs;
        long hours = totalMs / 3_600_000;
        totalMs %= 3_600_000;
        long minutes = totalMs / 60_000;
        totalMs %= 60_000;
        long seconds = totalMs / 1_000;
        long millis = totalMs % 1_000;

        List<String> parts = new ArrayList<>(4);
        if (hours != 0) parts.add(hours == 1 ? "1 hour" : formatNumber(hours) + " hours");
        if (minutes != 0) parts.add(minutes == 1 ? "1 minute" : formatNumber(minutes) + " minutes");
        if (seconds != 0) parts.add(seconds == 1 ? "1 second" : formatNumber(seconds) + " seconds");
        if (millis != 0) parts.add(millis == 1 ? "1 millisecond" : formatNumber(millis) + " milliseconds");
        if (parts.isEmpty()) return "0 seconds";
        return String.join(" and ", parts);
    }

    /**
     * Formats a damage type id (e.g. minecraft:player_attack) for display: "Player Attack".
     * Strips namespace, replaces underscores with spaces, title-cases each word.
     */
    public static String formatDamageTypeForDisplay(String typeId) {
        if (typeId == null || typeId.isEmpty()) return "Unknown";
        String name = typeId.contains(":") ? typeId.substring(typeId.indexOf(':') + 1) : typeId;
        String[] words = name.replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            String w = words[i];
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.length() > 0 ? sb.toString() : "Unknown";
    }

    /**
     * Formats elapsed milliseconds as SS.cc, MM:SS.cc, HH:MM:SS.cc, or DD:HH:MM:SS.cc.
     * Minutes only when m >= 1; hours when h >= 1; days when d >= 1. Hundredths = (elapsedMs % 1000) / 10 (0-99).
     */
    public static String formatTime(long elapsedMs) {
        if (elapsedMs < 0) elapsedMs = 0;
        long totalSec = elapsedMs / 1000;
        if (totalSec > GSRBroadcastParameters.NUMBER_SCIENTIFIC_THRESHOLD) {
            return formatNumber(totalSec) + " s";
        }
        int hundredths = (int) ((elapsedMs % 1000) / 10); // 0-99
        long d = totalSec / 86400;
        long h = (totalSec / 3600) % 24;
        long m = (totalSec / 60) % 60;
        long s = totalSec % 60;

        String secFrac = String.format("%02d.%02d", s, hundredths);
        if (d >= 1) {
            return String.format("%02d:%02d:%02d:%s", d, h, m, secFrac);
        }
        if (h >= 1) {
            return String.format("%02d:%02d:%s", h, m, secFrac);
        }
        if (m >= 1) {
            return String.format("%02d:%s", m, secFrac);
        }
        return secFrac;
    }
}
