package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(CreateWorldScreen.class)
public abstract class GSRCreateWorldScreenMixin extends Screen {

    private static final int WORLD_NAME_MAX_LENGTH = 32;
    private static final int GLFW_MOD_CONTROL = 0x0002;

    @Shadow @Final private WorldCreationUiState worldCreator;

    protected GSRCreateWorldScreenMixin() {
        super(null);
    }

    /** Injects at end of init to prefill world name from nextGsrWorldName when set. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$prefillWorldName(CallbackInfo ci) {
        String name = GSRClient.nextGsrWorldName;
        if (name == null || name.isEmpty() || worldCreator == null || minecraft == null) return;

        worldCreator.setName(name);
        // Defer to next tick; retry until we find the world name field (Game tab may load later)
        Minecraft mc = minecraft;
        gsr$scheduleApplyAttempt(mc, name, 0);
    }

    private static void gsr$scheduleApplyAttempt(Minecraft mc, String name, int attempt) {
        mc.execute(() -> {
            if (attempt >= 60) { // ~3 seconds max
                GSRClient.nextGsrWorldName = null;
                return;
            }
            if (!gsr$tryApplyName(mc, name)) {
                gsr$scheduleApplyAttempt(mc, name, attempt + 1);
            }
        });
    }

    private static boolean gsr$tryApplyName(Minecraft mc, String name) {
        try {
            if (!(mc.screen instanceof CreateWorldScreen screen)) return false;
            if (GSRClient.nextGsrWorldName == null) return true; // already done

            EditBox field = gsr$findWorldNameField(screen);
            if (field == null) return false;

            mc.keyboardHandler.setClipboard(name);
            screen.setFocused(field);
            // Simulate Ctrl+A (select all)
            KeyEvent ctrlA = new KeyEvent(GLFW.GLFW_KEY_A, 0, GLFW_MOD_CONTROL);
            screen.keyPressed(ctrlA);
            // Simulate Ctrl+V (paste) - uses clipboard we just set
            KeyEvent ctrlV = new KeyEvent(GLFW.GLFW_KEY_V, 0, GLFW_MOD_CONTROL);
            screen.keyPressed(ctrlV);
            // Direct setText as fallback (paste may be handled by Keyboard before Screen)
            field.setValue(name);
            GSRClient.nextGsrWorldName = null;
            return true;
        } catch (Throwable t) {
            GSRClient.nextGsrWorldName = null;
            return true; // stop retrying
        }
    }

    /** Find the world name EditBox (max length 32) in the screen hierarchy. */
    private static EditBox gsr$findWorldNameField(Screen screen) {
        return gsr$findWorldNameFieldRecurse(screen);
    }

    private static EditBox gsr$findWorldNameFieldRecurse(GuiEventListener e) {
        if (e instanceof EditBox tf && gsr$getMaxLength(tf) == WORLD_NAME_MAX_LENGTH) {
            return tf;
        }
        if (e instanceof ContainerEventHandler pe) {
            for (GuiEventListener child : pe.children()) {
                EditBox found = gsr$findWorldNameFieldRecurse(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static int gsr$getMaxLength(EditBox tf) {
        try {
            Method m = EditBox.class.getDeclaredMethod("getMaxLength");
            m.setAccessible(true);
            return (Integer) m.invoke(tf);
        } catch (Exception ex) {
            return -1;
        }
    }
}
