package protosky.stuctures;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.random.ChunkRandom;
import net.minecraft.world.gen.random.RandomSeed;
import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;
import protosky.mixins.StructurePieceAccessor;
import protosky.mixins.endCityParts.ChunkGeneratorMixin;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StructureHelper {
    public static void genStructures(ProtoChunk protoChunk, ServerWorld world, StructureManager structureManager, ChunkGenerator generator) {
        if (world.getRegistryKey() == World.OVERWORLD) StrongHoldHelper.processStronghold(world, protoChunk);
        if (world.getRegistryKey() == World.END) PillarHelper.generate(world, protoChunk);
    }

    public static BlockPos getBlockInStructurePiece(StructurePiece piece, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        return new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
    }

    public static BlockState getBlockAt(BlockView blockView, int x, int y, int z, StructurePiece piece) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        int i = access.invokeApplyXTransform(x, z);
        int j = access.invokeApplyYTransform(y);
        int k = access.invokeApplyZTransform(x, z);
        BlockPos blockPos = new BlockPos(i, j, k);
        return !piece.getBoundingBox().contains(blockPos) ? Blocks.AIR.getDefaultState() : blockView.getBlockState(blockPos);
    }

    public static void setBlockInStructure(StructurePiece piece, ProtoChunk chunk, BlockState state, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = getBlockInStructurePiece(piece, x, y, z);
        if (piece.getBoundingBox().contains(pos)) {
            BlockMirror mirror = access.getMirror();
            if (mirror != BlockMirror.NONE)
                state = state.mirror(mirror);
            BlockRotation rotation = piece.getRotation();
            if (rotation != BlockRotation.NONE)
                state = state.rotate(rotation);

            setBlockInChunk(chunk, pos, state);
        }
    }

    //This checks if a structure is intersecting a chunk
    public static boolean isIntersecting(StructureStart stronghold, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        BlockBox posBox = new BlockBox(pos.getStartX(), chunk.getBottomY(), pos.getStartZ(), pos.getEndX(), chunk.getTopY(), pos.getEndZ());
        StructurePiecesHolder structurePiecesHolder = new StructurePiecesCollector();
        if (stronghold != null) {
            for (StructurePiece piece : stronghold.getChildren()) {
                structurePiecesHolder.addPiece(piece);
            }
            return structurePiecesHolder.getIntersecting(posBox) != null;
        }
        return false;
    }

    public static void setBlockInChunk(ProtoChunk chunk, BlockPos pos, BlockState state) {
        if (chunk.getPos().equals(new ChunkPos(pos))) {
            chunk.setBlockState(pos, state, false);
        }
    }

    public static void fillAirAndLiquidDownwards(ProtoChunk chunk, BlockState blockState, int x, int y, int z, StructurePiece piece) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        int i = access.invokeApplyXTransform(x, z);
        int j = access.invokeApplyYTransform(y);
        int k = access.invokeApplyZTransform(x, z);
        if (piece.getBoundingBox().contains(new BlockPos(i, j, k))) {
            while ((chunk.getBlockState(new BlockPos(i, j, k)).isAir() || chunk.getBlockState(new BlockPos(i, j, k)).getMaterial().isLiquid()) && j > 1) {
                setBlockInChunk(chunk, new BlockPos(i, j, k), blockState);
                --j;
            }
        }
    }

    public static void fillWithOutline(ProtoChunk chunk, int i, int j, int k, int l, int m, int n, BlockState blockState, BlockState inside, boolean bl, StructurePiece piece) {
        for (int o = j; o <= m; ++o) {
            for (int p = i; p <= l; ++p) {
                for (int q = k; q <= n; ++q) {
                    if (!bl || !getBlockAt(chunk, p, o, q, piece).isAir()) {
                        if (o != j && o != m && p != i && p != l && q != k && q != n) {
                            setBlockInStructure(piece, chunk, inside, p, o, q);
                        } else {
                            setBlockInStructure(piece, chunk, blockState, p, o, q);
                        }
                    }
                }
            }
        }

    }

    public static boolean addChest(ProtoChunk chunk, Random random, int x, int y, int z, Identifier lootTableId, /*@Nullable*/ BlockState block, StructurePiece piece) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
        if (piece.getBoundingBox().contains(pos) && chunk.getBlockState(pos).getBlock() != Blocks.CHEST) {
            if (block == null) {
                block = StructurePiece.orientateChest(chunk, pos, Blocks.CHEST.getDefaultState());
            }

            setBlockInChunk(chunk, pos, block);
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity) {
                ((ChestBlockEntity) blockEntity).setLootTable(lootTableId, random.nextLong());
            }

            return true;
        } else {
            return false;
        }
    }

}
