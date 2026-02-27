package net.berkle.groupspeedrun.data;

/**
 * One row in run_participants: links a run to a player (UUID + display name at run end).
 */
public record GSRRunParticipant(
    String runId,
    String playerUuid,
    String playerName
) {}
