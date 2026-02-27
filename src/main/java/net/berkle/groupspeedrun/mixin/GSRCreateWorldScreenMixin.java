package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
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

    @Shadow @Final private WorldCreator worldCreator;

    protected GSRCreateWorldScreenMixin() {
        super(null);
    }

    /** Injects at end of init to prefill world name from nextGsrWorldName when set. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$prefillWorldName(CallbackInfo ci) {
        String name = GSRClient.nextGsrWorldName;
        if (name == null || name.isEmpty() || worldCreator == null || client == null) return;

        worldCreator.setWorldName(name);
        // Defer to next tick; retry until we find the world name field (Game tab may load later)
        MinecraftClient mc = client;
        gsr$scheduleApplyAttempt(mc, name, 0);
    }

    private static void gsr$scheduleApplyAttempt(MinecraftClient mc, String name, int attempt) {
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

    private static boolean gsr$tryApplyName(MinecraftClient mc, String name) {
        try {
            if (!(mc.currentScreen instanceof CreateWorldScreen screen)) return false;
            if (GSRClient.nextGsrWorldName == null) return true; // already done

            TextFieldWidget field = gsr$findWorldNameField(screen);
            if (field == null) return false;

            mc.keyboard.setClipboard(name);
            screen.setFocused(field);
            // Simulate Ctrl+A (select all)
            KeyInput ctrlA = new KeyInput(GLFW.GLFW_KEY_A, 0, GLFW_MOD_CONTROL);
            screen.keyPressed(ctrlA);
            // Simulate Ctrl+V (paste) - uses clipboard we just set
            KeyInput ctrlV = new KeyInput(GLFW.GLFW_KEY_V, 0, GLFW_MOD_CONTROL);
            screen.keyPressed(ctrlV);
            // Direct setText as fallback (paste may be handled by Keyboard before Screen)
            field.setText(name);
            GSRClient.nextGsrWorldName = null;
            return true;
        } catch (Throwable t) {
            GSRClient.nextGsrWorldName = null;
            return true; // stop retrying
        }
    }

    /** Find the world name TextFieldWidget (max length 32) in the screen hierarchy. */
    private static TextFieldWidget gsr$findWorldNameField(Screen screen) {
        return gsr$findWorldNameFieldRecurse(screen);
    }

    private static TextFieldWidget gsr$findWorldNameFieldRecurse(Element e) {
        if (e instanceof TextFieldWidget tf && gsr$getMaxLength(tf) == WORLD_NAME_MAX_LENGTH) {
            return tf;
        }
        if (e instanceof ParentElement pe) {
            for (Element child : pe.children()) {
                TextFieldWidget found = gsr$findWorldNameFieldRecurse(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static int gsr$getMaxLength(TextFieldWidget tf) {
        try {
            Method m = TextFieldWidget.class.getDeclaredMethod("getMaxLength");
            m.setAccessible(true);
            return (Integer) m.invoke(tf);
        } catch (Exception ex) {
            return -1;
        }
    }
}
