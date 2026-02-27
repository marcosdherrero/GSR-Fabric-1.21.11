package net.berkle.groupspeedrun;

import com.mojang.brigadier.CommandDispatcher;
import net.berkle.groupspeedrun.network.GSROpenScreenPayload;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * GSR commands: /gsr config and /gsr controls. Run actions (pause, resume, reset) are in the controls screen.
 */
public final class GSRCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("gsr")
                .requires(source -> CommandManager.ALWAYS_PASS_CHECK.allows(source.getPermissions()))
                .then(literal("config")
                        .executes(context -> {
                            ServerPlayerEntity p = context.getSource().getPlayer();
                            if (p == null) {
                                context.getSource().sendFeedback(() -> Text.literal(GSRUiParameters.MSG_PREFIX + "Only players can open the config screen."), false);
                                return 0;
                            }
                            GSRNetworking.sendOpenScreen(p, GSROpenScreenPayload.TYPE_CONFIG);
                            context.getSource().sendFeedback(() -> Text.literal(GSRUiParameters.MSG_PREFIX + "Opening GSR config..."), false);
                            return 1;
                        }))
                .then(literal("controls")
                        .executes(context -> {
                            ServerPlayerEntity p = context.getSource().getPlayer();
                            if (p == null) {
                                context.getSource().sendFeedback(() -> Text.literal(GSRUiParameters.MSG_PREFIX + "Only players can open the controls screen."), false);
                                return 0;
                            }
                            GSRNetworking.sendOpenScreen(p, GSROpenScreenPayload.TYPE_CONTROLS);
                            context.getSource().sendFeedback(() -> Text.literal(GSRUiParameters.MSG_PREFIX + "Opening GSR controls..."), false);
                            return 1;
                        })));
    }
}
