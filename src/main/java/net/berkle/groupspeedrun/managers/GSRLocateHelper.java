package net.berkle.groupspeedrun.managers;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;
import net.berkle.groupspeedrun.mixin.accessors.GSRSimpleStructurePieceAccessor;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.berkle.groupspeedrun.parameter.GSRServerParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-side helper to locate structures for the locator HUD.
 * Uses ServerWorld.locateStructure with structure tags (from GSR data pack).
 * Stronghold uses EYE_OF_ENDER_LOCATED and points to the portal room.
 * Ship locator searches for the nearest end city that has a ship piece and points to the ship (elytra) specifically.
 */
public final class GSRLocateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Locate");

    private GSRLocateHelper() {}

    /**
     * Locates the nearest structure of the given type from the given position.
     * For stronghold, returns the portal room center. For ship, returns the ship piece center (elytra).
     * For others, returns structure center.
     *
     * @return BlockPos of the target, or null if not found
     */
    public static BlockPos locate(ServerWorld world, String structureType, BlockPos from) {
        if (world == null || from == null) return null;
        TagKey<Structure> tag = tagFor(structureType);
        if (tag == null) return null;
        try {
            if ("ship".equalsIgnoreCase(structureType)) {
                return locateNearestEndShip(world, from, tag);
            }
            BlockPos found = world.locateStructure(tag, from, GSRLocatorParameters.LOCATE_RADIUS_CHUNKS, false);
            if (found == null) return null;
            if ("stronghold".equalsIgnoreCase(structureType)) {
                BlockPos portal = locateStrongholdPortal(world, found);
                return portal != null ? portal : found;
            }
            return found;
        } catch (Exception e) {
            LOGGER.warn("[GSR] Locate failed for {}: {}", structureType, e.getMessage());
            return null;
        }
    }

    /**
     * Finds the nearest end city that has a ship piece and returns the ship (elytra) position.
     * Uses locateStructure (like /locate) so it works from any distance; loads chunks as needed.
     * End cities without ships are added to a dud set and skipped – never point to structures without wings.
     * Uses getStructureStarts per chunk to find all end cities (avoids sampling positions that miss structures).
     */
    private static BlockPos locateNearestEndShip(ServerWorld world, BlockPos from, TagKey<Structure> endCityTag) {
        try {
            StructureAccessor accessor = world.getStructureAccessor();
            int radiusChunks = Math.min(GSRLocatorParameters.LOCATE_RADIUS_CHUNKS, GSRLocatorParameters.SHIP_LOCATE_SEARCH_RADIUS_CHUNKS);
            int maxChunks = GSRLocatorParameters.SHIP_LOCATE_MAX_CHUNKS;
            int fromChunkX = from.getX() >> 4;
            int fromChunkZ = from.getZ() >> 4;
            double bestDistSq = Double.MAX_VALUE;
            BlockPos bestShip = null;
            Set<Long> dudStructures = new HashSet<>();
            int chunksChecked = 0;

            Structure endCityStructure = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE).get(Identifier.of("minecraft", "end_city"));
            if (endCityStructure == null) return null;

            // 1. Player position first – if within view of a city with ship, they may be inside it
            StructureStart atPlayer = accessor.getStructureContaining(from, endCityTag);
            if (atPlayer != null) {
                BlockPos shipPos = extractShipPosition(atPlayer);
                if (shipPos != null) return shipPos;
                BlockBox box = atPlayer.getBoundingBox();
                long key = ((long) box.getMinX() << 32) | (box.getMinZ() & 0xFFFFFFFFL);
                dudStructures.add(key);
            }

            // 2. locateStructure nearest end city
            BlockPos firstCity = world.locateStructure(endCityTag, from, radiusChunks, false);
            if (firstCity != null) {
                world.getChunk(firstCity.getX() >> 4, firstCity.getZ() >> 4);
                StructureStart start = accessor.getStructureContaining(firstCity, endCityTag);
                if (start != null) {
                    BlockPos shipPos = extractShipPosition(start);
                    if (shipPos != null) return shipPos;
                    BlockBox box = start.getBoundingBox();
                    long key = ((long) box.getMinX() << 32) | (box.getMinZ() & 0xFFFFFFFFL);
                    dudStructures.add(key);
                }
            }

            // 3. Spiral: getStructureStarts per chunk – finds all end cities overlapping each chunk (no position sampling)
            int[] sectionYs = { 3, 4, 5, 6 }; // Y 48–111; end cities span this range
            for (int r = 0; r <= radiusChunks && chunksChecked < maxChunks; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                        if (chunksChecked++ >= maxChunks) break;
                        int cx = fromChunkX + dx;
                        int cz = fromChunkZ + dz;
                        world.getChunk(cx, cz);
                        ChunkPos chunkPos = new ChunkPos(cx, cz);
                        for (int sectionY : sectionYs) {
                            ChunkSectionPos sectionPos = ChunkSectionPos.from(chunkPos, sectionY);
                            List<StructureStart> starts = accessor.getStructureStarts(sectionPos, endCityStructure);
                        for (StructureStart start : starts) {
                            BlockBox box = start.getBoundingBox();
                            long key = ((long) box.getMinX() << 32) | (box.getMinZ() & 0xFFFFFFFFL);
                            if (dudStructures.contains(key)) continue;
                            BlockPos shipPos = extractShipPosition(start);
                            if (shipPos == null) {
                                dudStructures.add(key);
                                continue;
                            }
                            double distSq = from.getSquaredDistance(shipPos);
                            if (distSq < bestDistSq) {
                                bestDistSq = distSq;
                                bestShip = shipPos;
                            }
                        }
                        }
                    }
                }
            }
            return bestShip;
        } catch (Exception e) {
            LOGGER.warn("[GSR] Locate ship failed: {}", e.getMessage());
            return null;
        }
    }

    /** Extracts ship piece center from an end city StructureStart, or null if no ship. Uses template path (end_city/ship) and bounding box size (ship ~29x13x24). */
    private static BlockPos extractShipPosition(StructureStart start) {
        for (StructurePiece piece : start.getChildren()) {
            BlockBox box = piece.getBoundingBox();
            if (piece instanceof SimpleStructurePiece simple) {
                String templateId = ((GSRSimpleStructurePieceAccessor) simple).gsr$getTemplateIdString();
                if (templateId != null && templateId.toLowerCase().contains("ship")) {
                    int cx = (box.getMinX() + box.getMaxX()) / 2;
                    int cy = (box.getMinY() + box.getMaxY()) / 2;
                    int cz = (box.getMinZ() + box.getMaxZ()) / 2;
                    return new BlockPos(cx, cy, cz);
                }
            }
            String className = piece.getClass().getSimpleName().toLowerCase();
            String fullName = piece.getClass().getName().toLowerCase();
            if (className.contains("ship") || fullName.contains("ship")) {
                int cx = (box.getMinX() + box.getMaxX()) / 2;
                int cy = (box.getMinY() + box.getMaxY()) / 2;
                int cz = (box.getMinZ() + box.getMaxZ()) / 2;
                return new BlockPos(cx, cy, cz);
            }
            int spanX = box.getBlockCountX();
            int spanY = box.getBlockCountY();
            int spanZ = box.getBlockCountZ();
            int maxHz = Math.max(spanX, spanZ);
            int minHz = Math.min(spanX, spanZ);
            if (maxHz >= 25 && minHz >= 8 && spanY >= 18 && spanY <= 30) {
                int cx = (box.getMinX() + box.getMaxX()) / 2;
                int cy = (box.getMinY() + box.getMaxY()) / 2;
                int cz = (box.getMinZ() + box.getMaxZ()) / 2;
                return new BlockPos(cx, cy, cz);
            }
        }
        return null;
    }

    private static TagKey<Structure> tagFor(String type) {
        return switch (type.toLowerCase()) {
            case "fortress" -> TagKey.of(RegistryKeys.STRUCTURE, Identifier.of("gsr", "fortress"));
            case "bastion" -> TagKey.of(RegistryKeys.STRUCTURE, Identifier.of("gsr", "bastion_remnant"));
            case "stronghold" -> StructureTags.EYE_OF_ENDER_LOCATED;
            case "ship" -> TagKey.of(RegistryKeys.STRUCTURE, Identifier.of("gsr", "end_city"));
            default -> null;
        };
    }

    /**
     * Finds the portal room center within the stronghold at the given position.
     * @return BlockPos at portal room center, or null if portal room not found
     */
    private static BlockPos locateStrongholdPortal(ServerWorld world, BlockPos strongholdPos) {
        try {
            StructureAccessor accessor = world.getStructureAccessor();
            StructureStart start = accessor.getStructureContaining(strongholdPos, StructureTags.EYE_OF_ENDER_LOCATED);
            if (start == null) return null;
            for (StructurePiece piece : start.getChildren()) {
                if (piece instanceof StrongholdGenerator.PortalRoom portalRoom) {
                    BlockBox box = portalRoom.getBoundingBox();
                    int cx = (box.getMinX() + box.getMaxX()) / 2;
                    int cy = (box.getMinY() + box.getMaxY()) / 2;
                    int cz = (box.getMinZ() + box.getMaxZ()) / 2;
                    return new BlockPos(cx, cy, cz);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[GSR] Could not find stronghold portal room: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Checks if the given position is inside any structure of the given type.
     * Used for split detection (fortress, bastion) when no locator is active.
     * Verifies the position is inside at least one structure piece's bounding box,
     * not just in a chunk with a structure reference (avoids false positives on Nether entry).
     *
     * @param world Server world.
     * @param pos Position to check.
     * @param structureType "fortress", "bastion", "stronghold", or "ship".
     * @return true if pos is inside a structure piece of that type.
     */
    public static boolean isInStructure(ServerWorld world, BlockPos pos, String structureType) {
        if (world == null || pos == null) return false;
        TagKey<Structure> tag = tagFor(structureType);
        if (tag == null) return false;
        try {
            StructureStart start = world.getStructureAccessor().getStructureContaining(pos, tag);
            if (start == null) return false;
            return isPosInsideStructurePiece(pos, start);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the position is inside or within proximity of any child piece of the structure.
     * Prevents false positives when getStructureContaining returns a structure
     * whose reference chunk overlaps the pos but no actual piece contains it.
     * Uses SPLIT_STRUCTURE_PROXIMITY_BLOCKS so players near the structure edge still trigger.
     */
    private static boolean isPosInsideStructurePiece(BlockPos pos, StructureStart start) {
        int margin = GSRServerParameters.SPLIT_STRUCTURE_PROXIMITY_BLOCKS;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (StructurePiece piece : start.getChildren()) {
            BlockBox box = piece.getBoundingBox();
            if (box.getMinX() - margin <= x && x <= box.getMaxX() + margin
                    && box.getMinY() - margin <= y && y <= box.getMaxY() + margin
                    && box.getMinZ() - margin <= z && z <= box.getMaxZ() + margin) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given position is inside a structure of the given type that contains the stored target.
     * Used to detect when a player enters a tracked structure (locator turn-off).
     * For ship: uses spherical distance (3D) – player within 100 blocks of ship position.
     * storedX, storedY, storedZ are the ship (elytra) coordinates.
     */
    public static boolean isInTrackedStructure(ServerWorld world, BlockPos playerPos, String structureType, int storedX, int storedY, int storedZ) {
        if (world == null || playerPos == null) return false;
        if ("ship".equalsIgnoreCase(structureType)) {
            int dx = playerPos.getX() - storedX;
            int dy = playerPos.getY() - storedY;
            int dz = playerPos.getZ() - storedZ;
            int r = GSRLocatorParameters.SHIP_LOCATOR_TRIGGER_RADIUS_BLOCKS;
            return (long) dx * dx + (long) dy * dy + (long) dz * dz <= (long) r * r;
        }
        TagKey<Structure> tag = tagFor(structureType);
        if (tag == null) return false;
        try {
            var accessor = world.getStructureAccessor();
            StructureStart start = accessor.getStructureContaining(playerPos, tag);
            if (start == null) return false;
            BlockBox structureBox = start.getBoundingBox();
            return structureBox.getMinX() <= storedX && storedX <= structureBox.getMaxX()
                && structureBox.getMinZ() <= storedZ && storedZ <= structureBox.getMaxZ();
        } catch (Exception e) {
            return false;
        }
    }

    /** Overload for structures that only need XZ (fortress, bastion, stronghold). Pass 0 for storedY. */
    public static boolean isInTrackedStructure(ServerWorld world, BlockPos playerPos, String structureType, int storedX, int storedZ) {
        return isInTrackedStructure(world, playerPos, structureType, storedX, 0, storedZ);
    }
}
