package protosky;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.light.LightingProvider;
import protosky.mixins.ProtoChunkAccessor;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static protosky.ProtoSkySettings.LOGGER;

public class WorldGenUtils
{
    private static boolean biomeMatchesAt(RegistryEntry<Biome> left,RegistryKey<Biome> right) {
        var biomeAt = left.getKey();
        if (biomeAt.isEmpty()) {
            return false;
        }
        return right.equals(biomeAt.get());
    }

    private static Block getBlockAt(ServerWorld world,BlockPos pos) {
        var biome = world.getBiome(pos);
        if (biomeMatchesAt(biome, BiomeKeys.MUSHROOM_FIELDS)) {
            return Blocks.MYCELIUM;
        }

        if (biomeMatchesAt(biome, BiomeKeys.DESERT)) {
            return Blocks.SAND;
        }

        if (biomeMatchesAt(biome, BiomeKeys.WARPED_FOREST)) {
            return Blocks.WARPED_NYLIUM;
        }

        if (biomeMatchesAt(biome, BiomeKeys.CRIMSON_FOREST)) {
            return Blocks.CRIMSON_NYLIUM;
        }

        if (world.getRegistryKey().equals(World.NETHER)) {
            return Blocks.NETHERRACK;
        }

        return Blocks.GRASS_BLOCK; 
    }
    
    public static void deleteBlocks(Chunk chunk, ServerWorld world) {
        //This loops through all sections (16x16x16) sections of a chunk and copies over the biome information, but not the blocks.
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection chunkSection = sections[i];
            PalettedContainer<BlockState> blockStateContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            ReadableContainer<RegistryEntry<Biome>> biomeContainer = chunkSection.getBiomeContainer();
            sections[i] = new ChunkSection(blockStateContainer, biomeContainer);
        }
        
        var pos = chunk.getPos();
        var px = pos.x * 16;
        var pz = pos.z * 16;
        for (var bpos : BlockPos.iterate(px, chunk.getBottomY(), pz,px + 15,
                chunk.getBottomY(), pz + 15)) {
            if (world.getRegistryKey().equals(World.END)) {
                chunk.setBlockState(bpos, Blocks.END_STONE.getDefaultState(), false);
                continue;
            }
            chunk.setBlockState(bpos, Blocks.BEDROCK.getDefaultState(), false);
            var bup = bpos.up();
            chunk.setBlockState(bup, getBlockAt(world,bup).getDefaultState(), false);
        }

        //This removes all the block entities
        for (BlockPos bePos : chunk.getBlockEntityPositions()) {
            chunk.removeBlockEntity(bePos);
        }

        //This should clear all the light sources
        /*ProtoChunk protoChunk = (ProtoChunk) chunk;
        ProtoChunkAccessor protoChunkAccessor = (ProtoChunkAccessor) protoChunk;
        LightingProvider lightingProvider = protoChunkAccessor.getLightingProvider();
        LightingProvider lightingProvider = world.getLightingProvider();
        lightingProvider.doLightUpdates();
        chunk.getLightSources().clear();*/
    }

    public static void genHeightMaps(Chunk chunk) {

        // defined in Heightmap class constructor
        // int elementBits = MathHelper.ceilLog2(chunk.getHeight() + 1);
        // var emptyHeightmap = new PackedIntegerArray(elementBits, 256);
        Heightmap.populateHeightmaps(chunk, chunk.getHeightmaps().stream().map(f -> f.getKey()).collect(Collectors.toSet()));
    }

    public static void clearEntities(ProtoChunk chunk, ServerWorld world) {
        // erase entities
        if (!(world.getRegistryKey() == World.END)) {
            chunk.getEntities().clear();
        } else {
            chunk.getEntities().removeIf(tag -> {
                String id = tag.getString("id");
                return !id.equals("minecraft:end_crystal") && !id.equals("minecraft:shulker") && !id.equals("minecraft:item_frame");
            });
        }
    }

    public static void genSpawnPlatform(Chunk chunk, ServerWorld world) {
        StructureTemplateManager man = world.getStructureTemplateManager();
        StructureTemplate s = null;

        // Get structure for this dimension
        if (world.getRegistryKey() == World.OVERWORLD) {
            Optional<StructureTemplate> op = man.getTemplate(new Identifier("protosky:spawn_overworld"));
            if (op.isPresent()) {
                s = op.get();
            }
        } else if (world.getRegistryKey() == World.NETHER) {
            Optional<StructureTemplate> op = man.getTemplate(new Identifier("protosky:spawn_nether"));
            if (op.isPresent()) {
                s = op.get();
            }
        }
        if (s == null) return;

        ChunkPos chunkPos = chunk.getPos();
        BlockPos blockPos = new BlockPos(chunkPos.x * 16, 64, chunkPos.z * 16);

        StructurePlacementData structurePlacementData = new StructurePlacementData().setUpdateNeighbors(true);

        int flags = 0;
        s.place(world, blockPos, blockPos, structurePlacementData, new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()), flags);
        world.setSpawnPos(blockPos.add(s.getSize().getX() / 2, s.getSize().getY() + 1, s.getSize().getZ() / 2), 0);
    }
}
