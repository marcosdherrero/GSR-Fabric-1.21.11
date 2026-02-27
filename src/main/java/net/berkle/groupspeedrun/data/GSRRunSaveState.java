package net.berkle.groupspeedrun.data;

import java.util.List;

/**
 * Complete save state for a single run: the run record (with date/time),
 * all participants, and all per-player stat snapshots.
 * Written when a run ends; used by the run-end broadcast and for later
 * leaderboard / graph lookups.
 */
public record GSRRunSaveState(
    GSRRunRecord record,
    List<GSRRunParticipant> participants,
    List<GSRRunPlayerSnapshot> snapshots
) {}
