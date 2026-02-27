package net.berkle.groupspeedrun.data;

/**
 * One transactional event during a run. Reserved for future event-sourced analytics.
 * eventType: DAMAGE_DEALT, DAMAGE_TAKEN, PEARL_PICKUP, BLAZE_PICKUP, BLOCK_PLACED,
 * BLOCK_BROKEN, DRAGON_DAMAGE, DISTANCE_MOVED, etc.
 *
 * <p>Not currently persisted. Stats are aggregated in-memory ({@link net.berkle.groupspeedrun.GSRStats})
 * and written to {@link GSRRunPlayerSnapshot} at run end. To enable event-sourced analytics, persist
 * events to a stat_events table and aggregate on demand.
 */
public record GSRRunEvent(
    String eventId,
    String runId,
    String playerUuid,
    String eventType,
    float valueFloat,
    int valueInt,
    long timestampMs
) {
    public static final String TYPE_DAMAGE_DEALT = "DAMAGE_DEALT";
    public static final String TYPE_DAMAGE_TAKEN = "DAMAGE_TAKEN";
    public static final String TYPE_PEARL_PICKUP = "PEARL_PICKUP";
    public static final String TYPE_BLAZE_PICKUP = "BLAZE_PICKUP";
    public static final String TYPE_BLOCK_PLACED = "BLOCK_PLACED";
    public static final String TYPE_BLOCK_BROKEN = "BLOCK_BROKEN";
    public static final String TYPE_DRAGON_DAMAGE = "DRAGON_DAMAGE";
    public static final String TYPE_DISTANCE_MOVED = "DISTANCE_MOVED";
}
