package net.berkle.groupspeedrun.client;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Placement-only seed check (no full world generation).
 *
 * <p>Spawn is estimated at overworld 0,0 (vanilla spawn search is near origin). Nether
 * portal origin is spawn/8, also 0,0 with that estimate.
 *
 * <p>Required:
 * <ul>
 *   <li>Village within {@link #OVERWORLD_RADIUS_BLOCKS} (256).</li>
 *   <li>Ruined portal within 256, or lava in noise-column samples around spawn.</li>
 *   <li>Bastion within {@link #BASTION_RADIUS_BLOCKS} (192) of nether origin.</li>
 *   <li>Fortress within {@link #FORTRESS_RADIUS_BLOCKS} (256) of nether origin.</li>
 * </ul>
 * Stronghold closeness is optional and never blocks acceptance.
 */
public final class GSRSeedFilterChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-SeedFilter");

    public static final int OVERWORLD_RADIUS_BLOCKS = 256;
    public static final int BASTION_RADIUS_BLOCKS = 192;
    public static final int FORTRESS_RADIUS_BLOCKS = 256;
    private static final int OVERWORLD_RADIUS_CHUNKS = OVERWORLD_RADIUS_BLOCKS / 16;
    private static final int NETHER_SCAN_CHUNKS = FORTRESS_RADIUS_BLOCKS / 16;
    private static final int LAVA_SAMPLE_STEP = 32;
    private static final int BIOME_QUART_Y = 16;

    private static boolean loggedApiWarning;

    private GSRSeedFilterChecker() {}

    public static boolean passes(GeneratorOptionsHolder ctx, long seed) {
        if (ctx == null) return false;
        try {
            DimensionOptionsRegistryHolder dims = ctx.selectedDimensions();
            ChunkGenerator overworld = dims.getChunkGenerator();
            var netherStem = dims.getOrEmpty(DimensionOptions.NETHER);
            if (netherStem.isEmpty()) return false;
            ChunkGenerator nether = netherStem.get().chunkGenerator();
            if (!(overworld instanceof NoiseChunkGenerator owNoise)
                    || !(nether instanceof NoiseChunkGenerator netherNoise)) {
                return true;
            }

            DynamicRegistryManager.Immutable access = ctx.getCombinedRegistryManager();
            RegistryWrapper.Impl<StructureSet> sets = access.getOrThrow(RegistryKeys.STRUCTURE_SET);
            var noiseParams = access.getOrThrow(RegistryKeys.NOISE_PARAMETERS);

            NoiseConfig owRandom = NoiseConfig.create(owNoise.getSettings().value(), noiseParams, seed);
            NoiseConfig netherRandom = NoiseConfig.create(netherNoise.getSettings().value(), noiseParams, seed);
            StructurePlacementCalculator owState = overworld.createStructurePlacementCalculator(sets, owRandom, seed);
            StructurePlacementCalculator netherState = nether.createStructurePlacementCalculator(sets, netherRandom, seed);

            RegistryEntry<StructureSet> villages = sets.getOrThrow(StructureSetKeys.VILLAGES);
            RegistryEntry<StructureSet> portals = sets.getOrThrow(StructureSetKeys.RUINED_PORTALS);
            RegistryEntry<StructureSet> netherComplexes = sets.getOrThrow(StructureSetKeys.NETHER_COMPLEXES);

            if (!hasMatchingStructure(owState, villages, seed, 0, 0, OVERWORLD_RADIUS_CHUNKS,
                    OVERWORLD_RADIUS_BLOCKS, overworld.getBiomeSource(), owRandom.getMultiNoiseSampler())) {
                return false;
            }
            boolean portal = hasMatchingStructure(owState, portals, seed, 0, 0, OVERWORLD_RADIUS_CHUNKS,
                    OVERWORLD_RADIUS_BLOCKS, overworld.getBiomeSource(), owRandom.getMultiNoiseSampler());
            if (!portal && !hasNearbyLava(owNoise, owRandom)) {
                return false;
            }

            boolean bastion = false;
            boolean fortress = false;
            for (ChunkPos pos : structureChunks(netherState, netherComplexes, seed, 0, 0, NETHER_SCAN_CHUNKS)) {
                RegistryEntry<Structure> picked = pickStructure(netherComplexes.value(), pos, seed,
                        nether.getBiomeSource(), netherRandom.getMultiNoiseSampler());
                if (picked == null) continue;
                double dist = blockDistance(pos, 0, 0);
                if (picked.matchesKey(StructureKeys.BASTION_REMNANT) && dist <= BASTION_RADIUS_BLOCKS) {
                    bastion = true;
                }
                if (picked.matchesKey(StructureKeys.FORTRESS) && dist <= FORTRESS_RADIUS_BLOCKS) {
                    fortress = true;
                }
                if (bastion && fortress) return true;
            }
            return false;
        } catch (RuntimeException e) {
            if (!loggedApiWarning) {
                loggedApiWarning = true;
                LOGGER.warn("GSR: Seed filter worldgen check failed; accepting seeds conservatively", e);
            }
            return true;
        }
    }

    private static boolean hasMatchingStructure(
            StructurePlacementCalculator state,
            RegistryEntry<StructureSet> set,
            long seed,
            int originCx,
            int originCz,
            int rangeChunks,
            int maxBlockDist,
            BiomeSource biomes,
            MultiNoiseUtil.MultiNoiseSampler sampler
    ) {
        for (ChunkPos pos : structureChunks(state, set, seed, originCx, originCz, rangeChunks)) {
            if (blockDistance(pos, originCx * 16, originCz * 16) > maxBlockDist) continue;
            if (pickStructure(set.value(), pos, seed, biomes, sampler) != null) return true;
        }
        return false;
    }

    private static List<ChunkPos> structureChunks(
            StructurePlacementCalculator state,
            RegistryEntry<StructureSet> set,
            long seed,
            int originCx,
            int originCz,
            int rangeChunks
    ) {
        List<ChunkPos> out = new ArrayList<>();
        StructurePlacement placement = set.value().placement();
        if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
            return out;
        }
        int spacing = Math.max(1, spread.getSpacing());
        int minRegX = Math.floorDiv(originCx - rangeChunks, spacing) - 1;
        int maxRegX = Math.floorDiv(originCx + rangeChunks, spacing) + 1;
        int minRegZ = Math.floorDiv(originCz - rangeChunks, spacing) - 1;
        int maxRegZ = Math.floorDiv(originCz + rangeChunks, spacing) + 1;
        for (int rx = minRegX; rx <= maxRegX; rx++) {
            for (int rz = minRegZ; rz <= maxRegZ; rz++) {
                ChunkPos pos = spread.getStartChunk(seed, rx * spacing, rz * spacing);
                if (pos.getChebyshevDistance(new ChunkPos(originCx, originCz)) > rangeChunks) continue;
                if (!placement.shouldGenerate(state, pos.x, pos.z)) continue;
                out.add(pos);
            }
        }
        return out;
    }

    private static RegistryEntry<Structure> pickStructure(
            StructureSet set,
            ChunkPos pos,
            long seed,
            BiomeSource biomes,
            MultiNoiseUtil.MultiNoiseSampler sampler
    ) {
        var biome = biomes.getBiome((pos.getStartX() + 8) >> 2, BIOME_QUART_Y, (pos.getStartZ() + 8) >> 2, sampler);
        List<StructureSet.WeightedEntry> valid = new ArrayList<>();
        for (StructureSet.WeightedEntry entry : set.structures()) {
            if (entry.structure().value().getValidBiomes().contains(biome)) {
                valid.add(entry);
            }
        }
        if (valid.isEmpty()) return null;
        ChunkRandom random = new ChunkRandom(new CheckedRandom(0L));
        random.setCarverSeed(seed, pos.x, pos.z);
        int total = 0;
        for (StructureSet.WeightedEntry entry : valid) {
            total += entry.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (StructureSet.WeightedEntry entry : valid) {
            roll -= entry.weight();
            if (roll < 0) return entry.structure();
        }
        return valid.get(valid.size() - 1).structure();
    }

    private static boolean hasNearbyLava(NoiseChunkGenerator generator, NoiseConfig randomState) {
        HeightLimitView height = HeightLimitView.create(generator.getMinimumY(), generator.getWorldHeight());
        int yMin = Math.max(generator.getMinimumY(), 0);
        int yMax = Math.min(64, generator.getMinimumY() + generator.getWorldHeight());
        for (int x = -OVERWORLD_RADIUS_BLOCKS; x <= OVERWORLD_RADIUS_BLOCKS; x += LAVA_SAMPLE_STEP) {
            for (int z = -OVERWORLD_RADIUS_BLOCKS; z <= OVERWORLD_RADIUS_BLOCKS; z += LAVA_SAMPLE_STEP) {
                if (Math.hypot(x, z) > OVERWORLD_RADIUS_BLOCKS) continue;
                VerticalBlockSample column = generator.getColumnSample(x, z, height, randomState);
                for (int y = yMin; y < yMax; y++) {
                    if (column.getState(y).getBlock() == Blocks.LAVA) return true;
                }
            }
        }
        return false;
    }

    private static double blockDistance(ChunkPos pos, int originX, int originZ) {
        int x = pos.getStartX() + 8 - originX;
        int z = pos.getStartZ() + 8 - originZ;
        return Math.hypot(x, z);
    }
}
