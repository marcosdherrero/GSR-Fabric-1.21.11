package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        var player = client.player;
        var world = client.world;
        if (player == null || world == null) return;
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f);
    }

    /**
     * Starts the victory celebration: plays sounds and spawns firework particles for 5 seconds.
     */
    public static void onVictoryCelebration() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        var player = client.player;
        var world = client.world;
        if (player == null || world == null) return;

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        world.playSound(player, px, py, pz, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.AMBIENT, 1f, 1f);
        world.playSound(player, px, py, pz, SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.AMBIENT, 0.8f, 1f);

        victoryCelebrationEndTick = world.getTime() + GSRBroadcastParameters.VICTORY_CELEBRATION_TICKS;
    }

    /**
     * Called each client tick. Spawns firework particles during victory celebration.
     */
    public static void tick(MinecraftClient client) {
        if (client == null || victoryCelebrationEndTick < 0) return;
        var world = client.world;
        if (world == null || client.player == null) return;
        if (world.getTime() >= victoryCelebrationEndTick) {
            victoryCelebrationEndTick = -1;
            return;
        }
        if (world.getTime() % GSRBroadcastParameters.VICTORY_FIREWORK_INTERVAL_TICKS != 0) return;

        var players = world.getPlayers();
        if (players.isEmpty()) return;

        Random r = world.getRandom();
        var target = players.get(r.nextInt(players.size()));
        double x = target.getX();
        double y = target.getY() + target.getHeight() * 0.5;
        double z = target.getZ();

        var pm = client.particleManager;
        int choice = r.nextInt(4);
        if (choice == 0) {
            double vx = (r.nextDouble() - 0.5) * 0.4;
            double vy = 0.2 + r.nextDouble() * 0.3;
            double vz = (r.nextDouble() - 0.5) * 0.4;
            pm.addParticle(ParticleTypes.FIREWORK, x, y, z, vx, vy, vz);
        } else if (choice == 1) {
            pm.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0, 0, 0);
        } else if (choice == 2) {
            pm.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0, 0, 0);
        } else {
            double vx = (r.nextDouble() - 0.5) * 0.3;
            double vy = 0.15 + r.nextDouble() * 0.25;
            double vz = (r.nextDouble() - 0.5) * 0.3;
            pm.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
        }
    }
}
