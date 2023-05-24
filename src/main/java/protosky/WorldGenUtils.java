package protosky;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadableContainer;
import protosky.stuctures.PillarHelper;
import protosky.stuctures.StructureHelper;
import protosky.mixins.ProtoChunkAccessor;

import java.util.*;

import static protosky.ProtoSkySettings.LOGGER;

public class WorldGenUtils {
    public static void deleteBlocks(ProtoChunk chunk, ServerWorld world) {
        var pos = chunk.getPos();
        var isCenter = pos.x * pos.x + pos.z * pos.z < 2;
        ChunkSection[] sections = chunk.getSectionArray();
        var start = 0;
        if (isCenter)
            start = 1;
        for (int i = start; i < sections.length; i++) {
            ChunkSection chunkSection = sections[i];
            PalettedContainer<BlockState> blockStateContainer = new PalettedContainer<>(Block.STATE_IDS,
                    Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            ReadableContainer<RegistryEntry<Biome>> biomeContainer = chunkSection.getBiomeContainer();
            int chunkPos = chunkSection.getYOffset() >> 4;
            sections[i] = new ChunkSection(chunkPos, blockStateContainer, biomeContainer);
        }
        if (isCenter) {
            var startX = pos.x * 16;
            var startZ = pos.z * 16;
            var startY = chunk.getBottomY();
            for (var bpos : BlockPos.iterate(startX, startY, startZ, startX + 15, startY + 15, startZ + 15)) {
                if (chunk.getBlockState(bpos).isOf(Blocks.BEDROCK))
                    continue;
                chunk.setBlockState(bpos, Blocks.AIR.getDefaultState(), false);
            }
        }

        // for (BlockPos bePos : chunk.getBlockEntityPositions()) {
        // ProtoSkySettings.LOGGER.info("{}", bePos);
        // world.getPlayers().forEach(player -> {
        // player.sendMessage(Text.of(bePos.toString()), true);
        // });
        // var be = chunk.getBlockEntity(bePos);
        // ProtoSkySettings.LOGGER.info("{}", be);
        // world.getPlayers().forEach(player -> {
        // player.sendMessage(Text.of(be.toString()));
        // });
        // ProtoSkySettings.LOGGER.info("{}", be.getType());
        // if (be.getType().equals(BlockEntityType.MOB_SPAWNER))
        // continue;
        // chunk.removeBlockEntity(bePos);
        // }
        // ((ProtoChunkAccessor) chunk).getLightSources().clear();
    }

    public static void genHeightMaps(ProtoChunk chunk) {
        // defined in Heightmap class constructor
        int elementBits = MathHelper.ceilLog2(chunk.getHeight() + 1);
        long[] emptyHeightmap = new PackedIntegerArray(elementBits, 256).getData();
        for (Map.Entry<Heightmap.Type, Heightmap> heightmapEntry : chunk.getHeightmaps()) {
            heightmapEntry.getValue().setTo(chunk, heightmapEntry.getKey(), emptyHeightmap);
        }
    }

    public static void clearEntities(ProtoChunk chunk, ServerWorld world) {
        // erase entities
        if (!(world.getRegistryKey() == World.END)) {
            // chunk.getEntities().clear();
        } else {
            chunk.getEntities().removeIf(tag -> {
                String id = tag.getString("id");
                LOGGER.info(id);
                return !id.equals("minecraft:end_crystal") && !id.equals("minecraft:shulker")
                        && !id.equals("minecraft:item_frame");
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
        if (s == null)
            return;

        ChunkPos chunkPos = chunk.getPos();
        BlockPos blockPos = new BlockPos(chunkPos.x * 16, 64, chunkPos.z * 16);

        StructurePlacementData structurePlacementData = new StructurePlacementData().setUpdateNeighbors(true);

        int flags = 0;
        s.place(world, blockPos, blockPos, structurePlacementData, new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()),
                flags);
        world.setSpawnPos(blockPos.add(s.getSize().getX() / 2, s.getSize().getY() + 1, s.getSize().getZ() / 2), 0);
    }
}
