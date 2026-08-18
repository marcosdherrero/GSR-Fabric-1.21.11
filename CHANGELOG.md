# Changelog

Newest first. Each Minecraft line has its own jar — install the one that matches your game version.

Jars look like `gsr-2.1.0+26.1.2-Minecraft26.1.2.jar`. GitHub tags look like `v2.1.0-mc26.1.2`.

## Minecraft version notes

GSR 2.1.0 ships on three Minecraft lines. Features are meant to match; the code underneath had to change because Mojang renamed APIs and rewrote menus.

**For players / pack makers:**

- **1.21.11** — original GSR 2.0 line. Yarn mappings, Java 21, Fabric Loader 0.18.4, Fabric API `0.141.2+1.21.11`.
- **26.1.2** — this branch. First official-mappings port. Java 25, Fabric Loader 0.19.2, Fabric API `0.149.0+26.1.2`, Cloth Config `26.1.154`.
- **26.2** — later official-mappings line (square **GSR** icon-row button is a vanilla 26.2 layout). Fabric Loader 0.19.3, Fabric API `0.157.0+26.2`.

Pick the jar whose Minecraft number matches the instance. A 26.1.2 jar will not load on 26.2 or 1.21.11.

This branch tagged **2.0.0**, **2.0.2**, and **2.1.0** on GitHub. Seed Filter, run-time persistence, Config click fixes, and reset/world-restore from the 26.2 2.0.3–2.0.6 line are included in **2.1.0** here (they were not separate 26.1.2 GitHub releases).

**For developers (code / API):**

- **1.21.11 (Yarn):** `DrawContext`, `MinecraftClient.setScreen`, `WorldCreator` on Create World.
- **26.1.2 (official Mojang names):** `GuiGraphicsExtractor`, `Minecraft.setScreen` (not `client.gui.setScreen`). Create World seed filter uses a shadowed `worldCreator` (`WorldCreationUiState`). Port remaps include NBT getters, death / locate / screen helpers, and `templateIdString` → `templateName`.
- **26.2:** `client.gui.setScreen`, Create World `uiState`. Title-screen item icons use `Holder.direct` + `ITEM_MODEL` when registry components are unbound.

---

## Mod versions

### 2.1.0 (Minecraft 26.1.2)

GitHub: [v2.1.0-mc26.1.2](https://github.com/marcosdherrero/GroupSpeedRun/releases/tag/v2.1.0-mc26.1.2) · jar `gsr-2.1.0+26.1.2-Minecraft26.1.2.jar`

**For players:**

- Config item icons show on the title screen instead of crashing or drawing as air.
- GSR menu buttons fade with the rest of the vanilla menu.
- **Seed Filter** is a full-width Config row: map icon, **ON** by default / **OFF**, green check / red X. Random Create World seeds retry until Overworld + Nether pass; typed seeds are never filtered. The choice is stored in `seed_filter.json` so it works from the title screen.
- Square **GSR** opens **Controls**, not Config. Opening waits one tick so the pause menu cannot steal the click.
- Completed run times survive reload. Reset restores the original world backup instead of hanging on Saving World or only clearing inventory.
- Config toggles register clicks (26.x GUI click flag is not “already handled”).

**For developers:**

- Unbound registries: `Holder.direct` + `ITEM_MODEL`; `isUsable` = non-air. Skip unbound Air hover names so Preferences can open on the title screen.
- Deferred `setScreen` next tick; config packets must not recreate Controls / Preferences.
- Create World mixin shadows `worldCreator`. 26.x NBT getters are type-strict. `mouseClicked` `captured` is a double-click flag.

### 2.0.2 (Minecraft 26.1.2)

GitHub: [v2.0.2-mc26.1.2](https://github.com/marcosdherrero/GroupSpeedRun/releases/tag/v2.0.2-mc26.1.2)

**For players:**

- End ship locator finds a ship that is already on screen.
- Advancement-style toast when a locator finds nothing.
- Pause / title still used the wide GSR Controls button (the square icon row is a 26.2 vanilla change).

**For developers:**

- Scan loaded End structure pieces first (`end_city/ship`). Ignore vanilla `INVALID_START`.

### 2.0.0 (Minecraft 26.1.2)

GitHub: [v2.0.0-mc26.1.2](https://github.com/marcosdherrero/GroupSpeedRun/releases/tag/v2.0.0-mc26.1.2)

**For players:**

- First 26.1.2 build of GSR 2.0 (shared timer, splits, locators, run history). Port of the 1.21.11 line; this jar is for Minecraft 26.1.2 only.

**For developers:**

- Official Mojang names, Java 25, Fabric Loader 0.19.2, Fabric API `0.149.0+26.1.2`. Yarn types remapped so the project compiles against Mojang + Fabric 26.1 APIs.
