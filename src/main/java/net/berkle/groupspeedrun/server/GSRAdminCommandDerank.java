package net.berkle.groupspeedrun.server;

// GSR: config, parameters
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
// Minecraft: server, commands, networking
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Deranks the run when an admin executes a run-affecting command during an active run.
 * Covers vanilla commands (gamemode, difficulty, weather, time, gamerule, etc.) that change
 * the world from vanilla. GSR commands (/gsr) are excluded; pause/resume derank is handled elsewhere.
 */
public final class GSRAdminCommandDerank {

    private static final String GSR_PREFIX = "gsr";

    private GSRAdminCommandDerank() {}

    /**
     * Called after a command executes. If the command was run by an admin player during an
     * active run with anti-cheat enabled, deranks the run and notifies the player.
     *
     * @param source command source (player, command block, etc.)
     * @param command the command string (may have leading slash)
     * @param server the server
     */
    public static void onCommandExecuted(ServerCommandSource source, String command, MinecraftServer server) {
        if (source == null || command == null || server == null) return;
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) return;
        if (!CommandManager.ADMINS_CHECK.allows(source.getPermissions())) return;

        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (!config.antiCheatEnabled || config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        if (config.locatorDeranked) return;

        String cmd = command.stripLeading().startsWith("/") ? command.stripLeading().substring(1) : command.stripLeading();
        int space = cmd.indexOf(' ');
        String firstWord = space >= 0 ? cmd.substring(0, space) : cmd;
        if (firstWord.isEmpty()) return;
        if (firstWord.equalsIgnoreCase(GSR_PREFIX)) return;

        config.locatorDeranked = true;
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        player.sendMessage(Text.literal(GSRUiParameters.MSG_PREFIX + GSRUiParameters.MSG_ADMIN_COMMAND_DERANKED), false);
    }
}
