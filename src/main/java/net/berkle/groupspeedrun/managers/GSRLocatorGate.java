package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Time gates and permission checks for locator toggles. Non-admin access depends on
 * locatorNonAdminMode: Never, Always, or 30 min post previous split. Admins bypass all gates.
 */
public final class GSRLocatorGate {

    private static final int MODE_NEVER = 0;
    private static final int MODE_ALWAYS = 1;

    private GSRLocatorGate() {}

    /** True if the source has permission to bypass time gates (same as ADMINS_CHECK). */
    public static boolean isAdmin(ServerCommandSource source) {
        return source != null && CommandManager.ADMINS_CHECK.allows(source.getPermissions());
    }

    /**
     * True if the player can toggle this locator type: either admin or non-admin mode allows
     * (Never/Always/30 min post split). Does not depend on run being active; locators work
     * after run ends if permissions and time gates allow.
     */
    public static boolean canUseLocator(GSRConfigWorld config, String type, boolean isAdmin) {
        if (config == null) return false;
        if (isAdmin) return true;
        /* No run ever started: no split data, cannot gate. */
        if (config.startTime <= 0) return false;
        int mode = config.locatorNonAdminMode;
        if (mode == MODE_NEVER) return false;
        if (mode == MODE_ALWAYS) {
            return switch (type.toLowerCase()) {
                case "fortress", "bastion" -> config.timeNether > 0;
                case "stronghold" -> config.timeFortress > 0 && config.timeBastion > 0;
                case "ship" -> config.timeDragon > 0;
                default -> false;
            };
        }
        long elapsed = config.getElapsedTime();
        return switch (type.toLowerCase()) {
            case "fortress", "bastion" -> config.timeNether > 0 && elapsed >= config.timeNether + GSRLocatorParameters.LOCATE_GATE_MS;
            case "stronghold" -> strongholdGatePassed(config, elapsed);
            case "ship" -> config.timeDragon > 0 && elapsed >= config.timeDragon + GSRLocatorParameters.LOCATE_GATE_MS;
            default -> false;
        };
    }

    /** Stronghold: 30 min after overworld return (when fortress+bastion done), or 30 min after first ender eye as fallback. */
    private static boolean strongholdGatePassed(GSRConfigWorld config, long elapsed) {
        if (config.timeFortress <= 0 || config.timeBastion <= 0) return false;
        if (config.timeFirstOverworldReturnAfterNether > 0) {
            return elapsed >= config.timeFirstOverworldReturnAfterNether + GSRLocatorParameters.LOCATE_GATE_MS;
        }
        return config.timeFirstEnderEye > 0 && elapsed >= config.timeFirstEnderEye + GSRLocatorParameters.LOCATE_GATE_MS;
    }

    /** Human-readable reason why the locator is locked (for tooltip/feedback). */
    public static String getLockReason(GSRConfigWorld config, String type, boolean isAdmin) {
        if (config == null || config.startTime <= 0) return "No run active.";
        if (isAdmin) return "";
        if (config.locatorNonAdminMode == MODE_NEVER) return "Locators disabled for non-admins.";
        if (config.locatorNonAdminMode == MODE_ALWAYS) {
            return switch (type.toLowerCase()) {
                case "fortress", "bastion" -> config.timeNether <= 0 ? "Enter the Nether first." : "";
                case "stronghold" -> (config.timeFortress <= 0 || config.timeBastion <= 0) ? "Complete fortress and bastion first." : "";
                case "ship" -> config.timeDragon <= 0 ? "Complete the run first." : "";
                default -> "";
            };
        }
        long elapsed = config.getElapsedTime();
        switch (type.toLowerCase()) {
            case "fortress", "bastion":
                if (config.timeNether <= 0) return "Enter the Nether first.";
                long netherGate = config.timeNether + GSRLocatorParameters.LOCATE_GATE_MS;
                if (elapsed < netherGate) return "Unlocks 30 min after Nether.";
                return "";
            case "stronghold":
                if (config.timeFortress <= 0 || config.timeBastion <= 0) return "Complete fortress and bastion first.";
                if (config.timeFirstOverworldReturnAfterNether > 0) {
                    long owGate = config.timeFirstOverworldReturnAfterNether + GSRLocatorParameters.LOCATE_GATE_MS;
                    if (elapsed < owGate) return "Unlocks 30 min after returning to Overworld.";
                    return "";
                }
                if (config.timeFirstEnderEye <= 0) return "Use an Ender Eye first.";
                long eyeGate = config.timeFirstEnderEye + GSRLocatorParameters.LOCATE_GATE_MS;
                if (elapsed < eyeGate) return "Unlocks 30 min after first Ender Eye.";
                return "";
            case "ship":
                if (config.timeDragon <= 0) return "Complete the run first.";
                long dragonGate = config.timeDragon + GSRLocatorParameters.LOCATE_GATE_MS;
                if (elapsed < dragonGate) return "Unlocks 30 min after victory.";
                return "";
            default:
                return "";
        }
    }
}
