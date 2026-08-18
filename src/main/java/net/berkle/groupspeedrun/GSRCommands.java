package net.berkle.groupspeedrun;

import com.mojang.brigadier.CommandDispatcher;
import net.berkle.groupspeedrun.network.GSROpenScreenPayload;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;

import static net.minecraft.commands.Commands.literal;

/**
 * GSR commands: /gsr config and /gsr controls. Run actions (pause, resume, reset) are in the controls screen.
 */
public final class GSRCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("gsr")
                .requires(source -> Commands.LEVEL_ALL.check(source.permissions()))
                .then(literal("config")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayer();
                            if (p == null) {
                                context.getSource().sendSuccess(() -> Component.literal(GSRUiParameters.MSG_PREFIX + "Only players can open the config screen."), false);
                                return 0;
                            }
                            GSRNetworking.sendOpenScreen(p, GSROpenScreenPayload.TYPE_CONFIG);
                            context.getSource().sendSuccess(() -> Component.literal(GSRUiParameters.MSG_PREFIX + "Opening GSR config..."), false);
                            return 1;
                        }))
                .then(literal("controls")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayer();
                            if (p == null) {
                                context.getSource().sendSuccess(() -> Component.literal(GSRUiParameters.MSG_PREFIX + "Only players can open the controls screen."), false);
                                return 0;
                            }
                            GSRNetworking.sendOpenScreen(p, GSROpenScreenPayload.TYPE_CONTROLS);
                            context.getSource().sendSuccess(() -> Component.literal(GSRUiParameters.MSG_PREFIX + "Opening GSR controls..."), false);
                            return 1;
                        })));
    }
}
