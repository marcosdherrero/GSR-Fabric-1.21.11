package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

/**
 * Client-side handler for split and victory celebration feedback.
 * Plays sounds and spawns firework particles during victory.
 */
public final class GSRCelebrationHandler {

    /** World tick when victory celebration ends; -1 = not active. */
    private static long victoryCelebrationEndTick = -1;

    private GSRCelebrationHandler() {}

    /**
     * Plays the level-up chime (same as experience bar every 5 levels).
     * Call when a split is achieved.
     */
    public static void onSplitAchieved() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        var player = client.player;
        var world = client.level;
        if (player == null || world == null) return;
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
    }

    /**
     * Starts the victory celebration: plays sounds and spawns firework particles for 5 seconds.
     */
    public static void onVictoryCelebration() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        var player = client.player;
        var world = client.level;
        if (player == null || world == null) return;

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        world.playSound(player, px, py, pz, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.AMBIENT, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.AMBIENT, 0.8f, 1f);

        victoryCelebrationEndTick = world.getGameTime() + GSRBroadcastParameters.VICTORY_CELEBRATION_TICKS;
    }

    /**
     * Called each client tick. Spawns firework particles during victory celebration.
     */
    public static void tick(Minecraft client) {
        if (client == null || victoryCelebrationEndTick < 0) return;
        var world = client.level;
        if (world == null || client.player == null) return;
        if (world.getGameTime() >= victoryCelebrationEndTick) {
            victoryCelebrationEndTick = -1;
            return;
        }
        if (world.getGameTime() % GSRBroadcastParameters.VICTORY_FIREWORK_INTERVAL_TICKS != 0) return;

        var players = world.players();
        if (players.isEmpty()) return;

        RandomSource r = world.getRandom();
        var target = players.get(r.nextInt(players.size()));
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5;
        double z = target.getZ();

        var pm = client.particleEngine;
        int choice = r.nextInt(4);
        if (choice == 0) {
            double vx = (r.nextDouble() - 0.5) * 0.4;
            double vy = 0.2 + r.nextDouble() * 0.3;
            double vz = (r.nextDouble() - 0.5) * 0.4;
            pm.createParticle(ParticleTypes.FIREWORK, x, y, z, vx, vy, vz);
        } else if (choice == 1) {
            pm.createParticle(ParticleTypes.EXPLOSION, x, y, z, 0, 0, 0);
        } else if (choice == 2) {
            pm.createParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0, 0, 0);
        } else {
            double vx = (r.nextDouble() - 0.5) * 0.3;
            double vy = 0.15 + r.nextDouble() * 0.25;
            double vz = (r.nextDouble() - 0.5) * 0.3;
            pm.createParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
        }
    }
}
