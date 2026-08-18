package net.berkle.groupspeedrun.mixin.server;

// GSR: admin command derank handler
import net.berkle.groupspeedrun.server.GSRAdminCommandDerank;
// Minecraft: command manager
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects after executeWithPrefix runs to derank the run when an admin executes any command
 * (except /gsr) during an active run with anti-cheat enabled.
 */
@Mixin(Commands.class)
public abstract class GSRCommandManagerMixin {

    /** After command executes, check if we should derank (admin command during active run). */
    @Inject(method = "performPrefixedCommand", at = @At("RETURN"))
    private void groupspeedrun$onCommandExecuted(CommandSourceStack source, String command, CallbackInfo ci) {
        if (source != null) {
            GSRAdminCommandDerank.onCommandExecuted(source, command, source.getServer());
        }
    }
}
