package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.config.GSRSeedFilterSettings;
import net.berkle.groupspeedrun.mixin.accessors.GSRCreateWorldScreenAccessor;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.text.Text;
import net.minecraft.world.gen.GeneratorOptions;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs the random-seed filter on Create World without generating worlds.
 * Typed seeds are never filtered. Cancel with Escape or the overlay button.
 */
public final class GSRSeedFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-SeedFilter");
    private static final int CANCEL_WIDTH = 120;
    private static final int CANCEL_HEIGHT = 20;

    private static final AtomicBoolean searching = new AtomicBoolean(false);
    private static final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private static final AtomicInteger attempts = new AtomicInteger(0);
    private static volatile String status = "";
    private static volatile CreateWorldScreen activeScreen;

    private GSRSeedFilter() {}

    public static boolean isSearching() {
        return searching.get();
    }

    public static boolean shouldFilter(WorldCreator worldCreator) {
        if (!GSRSeedFilterSettings.isEnabled()) return false;
        if (worldCreator == null) return false;
        if (!worldCreator.shouldGenerateStructures()) return false;
        if (worldCreator.isDebug()) return false;
        String seed = worldCreator.getSeed();
        return seed == null || seed.isBlank();
    }

    public static void start(CreateWorldScreen screen, WorldCreator worldCreator) {
        if (screen == null || worldCreator == null || !searching.compareAndSet(false, true)) return;
        cancelRequested.set(false);
        attempts.set(0);
        activeScreen = screen;
        status = "Checking seed 1…";
        GeneratorOptionsHolder ctx = worldCreator.getGeneratorOptionsHolder();
        Thread worker = new Thread(() -> search(screen, worldCreator, ctx), "gsr-seed-filter");
        worker.setDaemon(true);
        worker.start();
    }

    public static void cancel() {
        cancelRequested.set(true);
        status = "Cancelling…";
    }

    public static boolean handleKey(Screen screen, KeyInput key) {
        if (!isSearching() || !(screen instanceof CreateWorldScreen)) return false;
        if (key.key() == GLFW.GLFW_KEY_ESCAPE) {
            cancel();
            return true;
        }
        return true;
    }

    public static boolean handleClick(Screen screen, Click click) {
        if (!isSearching() || !(screen instanceof CreateWorldScreen create)) return false;
        int[] box = cancelBounds(create);
        int mx = (int) click.x();
        int my = (int) click.y();
        if (mx >= box[0] && mx < box[0] + box[2] && my >= box[1] && my < box[1] + box[3]) {
            cancel();
        }
        return true;
    }

    public static void renderOverlay(Screen screen, DrawContext context, int mouseX, int mouseY) {
        if (!isSearching() || !(screen instanceof CreateWorldScreen create)) return;
        int w = create.width;
        int h = create.height;
        context.fill(0, 0, w, h, 0xB0000000);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        String line = status == null || status.isEmpty() ? "Checking seed…" : status;
        context.drawCenteredTextWithShadow(font, Text.literal(line), w / 2, h / 2 - 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(font, Text.literal("Random seeds only. Escape to cancel."), w / 2, h / 2 - 6, 0xFFCCCCCC);

        int[] box = cancelBounds(create);
        boolean hovered = mouseX >= box[0] && mouseX < box[0] + box[2] && mouseY >= box[1] && mouseY < box[1] + box[3];
        context.fill(box[0], box[1], box[0] + box[2], box[1] + box[3],
                hovered ? GSRUiParameters.PREFERENCES_SEED_FILTER_OFF : 0xFF8B1E1E);
        context.drawCenteredTextWithShadow(font, Text.literal("Cancel"), box[0] + box[2] / 2,
                box[1] + (box[3] - font.fontHeight) / 2, 0xFFFFFFFF);
    }

    private static int[] cancelBounds(CreateWorldScreen screen) {
        int x = screen.width / 2 - CANCEL_WIDTH / 2;
        int y = screen.height / 2 + 16;
        return new int[] { x, y, CANCEL_WIDTH, CANCEL_HEIGHT };
    }

    private static void search(CreateWorldScreen screen, WorldCreator worldCreator, GeneratorOptionsHolder ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean found = false;
        try {
            int n = 0;
            while (!cancelRequested.get()) {
                n++;
                attempts.set(n);
                status = "Checking seed " + n + "…";
                long seed = GeneratorOptions.getRandomSeed();
                if (GSRSeedFilterChecker.passes(ctx, seed)) {
                    found = true;
                    final long accepted = seed;
                    final int tried = n;
                    mc.execute(() -> applyAndCreate(screen, worldCreator, accepted, tried));
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("GSR: Seed filter search failed", e);
        } finally {
            if (!found) {
                mc.execute(() -> {
                    searching.set(false);
                    activeScreen = null;
                    status = "";
                });
            }
        }
    }

    private static void applyAndCreate(CreateWorldScreen screen, WorldCreator worldCreator, long seed, int tried) {
        try {
            if (MinecraftClient.getInstance().currentScreen != screen) {
                searching.set(false);
                activeScreen = null;
                return;
            }
            worldCreator.setSeed(Long.toString(seed));
            LOGGER.info("GSR: Seed filter accepted {} after {} attempt(s)", seed, tried);
            searching.set(false);
            ((GSRCreateWorldScreenAccessor) screen).gsr$onCreate();
        } finally {
            searching.set(false);
            activeScreen = null;
            status = "";
        }
    }
}
