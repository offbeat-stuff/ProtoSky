package protosky.gen.stuctures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.feature.EndSpikeFeature;

import java.util.ArrayList;

import static protosky.ProtoSkySettings.LOGGER;

public class PillarHelper {
    /*public static void generateSpike(ProtoChunk chunk, WorldAccess world, Random random, EndSpikeFeature.Spike spike, EnderDragonFight fight)
    {
        int i = spike.getRadius();

        for (BlockPos blockPos : BlockPos.iterate(new BlockPos(spike.getCenterX() - i, 0, spike.getCenterZ() - i), new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i)))
        {
            if (blockPos.getSquaredDistanceFromCenter((double) spike.getCenterX(), (double) blockPos.getY(), (double) spike.getCenterZ()) <= (double) (i * i + 1) && blockPos.getY() < spike.getHeight())
            {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.OBSIDIAN.getDefaultState());
            }
            else if (blockPos.getY() > 65)
            {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.AIR.getDefaultState());
            }
        }

        if (spike.isGuarded())
        {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int m = -2; m <= 2; ++m)
            {
                for (int n = -2; n <= 2; ++n)
                {
                    for (int o = 0; o <= 3; ++o)
                    {
                        boolean bl = MathHelper.abs(m) == 2;
                        boolean bl2 = MathHelper.abs(n) == 2;
                        boolean bl3 = o == 3;
                        if (bl || bl2 || bl3)
                        {
                            boolean bl4 = m == -2 || m == 2 || bl3;
                            boolean bl5 = n == -2 || n == 2 || bl3;
                            BlockState blockState = (BlockState) Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, bl4 && n != -2).with(PaneBlock.SOUTH, bl4 && n != 2).with(PaneBlock.WEST, bl5 && m != -2).with(PaneBlock.EAST, bl5 && m != 2);
                            StructureHelper.setBlockInChunk(chunk, mutable.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), blockState);
                        }
                    }
                }
            }
        }

        BlockPos crystalPos = new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ());
        StructureHelper.setBlockInChunk(chunk, crystalPos, Blocks.BEDROCK.getDefaultState());
    }*/

    //public static ArrayList<Integer> generatedSpikes = new ArrayList<>();
    public static ArrayList<EndSpikeFeature.Spike> notGeneratedSpikes = new ArrayList<>();
    public static boolean madeSpikesList = false;

    public static void generate(StructureWorldAccess world, ProtoChunk chunk) {
        //TEST: New method
        //ADD: maybe check if chunks have a pillar in them, then generate?
        if(!madeSpikesList) {
            notGeneratedSpikes.addAll(EndSpikeFeature.getSpikes(world));
        }

        if(notGeneratedSpikes.size() > 0) {
            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;
            //-6 Chunks is 96 blocks, all pillars should be within this
            //spike.isInChunk() Doesn't work because it checks the center of the pillar cutting it off.
            if (chunkX >= -6 && chunkX <= 6 && chunkZ >= -6 && chunkZ <= 6) {
                for (EndSpikeFeature.Spike spike : notGeneratedSpikes) {
                    PillarHelper.generateSpike(world, spike, chunk);
                    notGeneratedSpikes.remove(spike);
                }
            }
        }

        /*int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        //-6 Chunks is 96 blocks, all pillars should be within this
        //spike.isInChunk() Doesn't work because it checks the center of the pillar cutting it off.
        // - Maybe use bounding box?
        if (chunkX >= -6 && chunkX <= 6 && chunkZ >= -6 && chunkZ <= 6) {

            for (EndSpikeFeature.Spike spike : EndSpikeFeature.getSpikes(world)) {
                /*if(!generatedSpikes.contains(spike.hashCode())) {
                    generatedSpikes.add(spike.hashCode());
                    PillarHelper.generateSpike(world, spike, chunk);
                    LOGGER.info("!Contained");
                }*//*

            }
        }*/

    }

    private static int i = 1;
    private static synchronized void incI() {
        i++;
        LOGGER.info(String.valueOf(i));
    }

    public static void generateSpike(ServerWorldAccess world, EndSpikeFeature.Spike spike, ProtoChunk chunk) {

        int i = spike.getRadius();
        for (BlockPos blockPos : BlockPos.iterate(
                new BlockPos(spike.getCenterX() - i, 0, spike.getCenterZ() - i),
                new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i)
        )) {
            if (blockPos.getSquaredDistance(spike.getCenterX(), blockPos.getY(), spike.getCenterZ()) <= (double) (i * i + 1)
                    && blockPos.getY() < spike.getHeight()) {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.OBSIDIAN.getDefaultState());

            } else if (blockPos.getY() > 65) {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.AIR.getDefaultState());
            }

        }
        if (spike.isGuarded()) {
            int j = -2;
            int k = 2;
            int l = 3;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int m = -2; m <= 2; ++m) {
                for (int n = -2; n <= 2; ++n) {
                    for (int o = 0; o <= 3; ++o) {
                        boolean bl = MathHelper.abs(m) == 2;
                        boolean bl2 = MathHelper.abs(n) == 2;
                        boolean bl3 = o == 3;
                        if (bl || bl2 || bl3) {
                            boolean bl4 = m == -2 || m == 2 || bl3;
                            boolean bl5 = n == -2 || n == 2 || bl3;
                            BlockState blockState = Blocks.IRON_BARS
                                    .getDefaultState()
                                    .with(PaneBlock.NORTH, bl4 && n != -2)
                                    .with(PaneBlock.SOUTH, bl4 && n != 2)
                                    .with(PaneBlock.WEST, bl5 && m != -2)
                                    .with(PaneBlock.EAST, bl5 && m != 2);
                            StructureHelper.setBlockInChunk(chunk, mutable.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), blockState);
                        }
                    }
                }
            }
        }

        //FIX: Add end crystal spawning
        /*EndCrystalEntity endCrystalEntity = EntityType.END_CRYSTAL.create(world.toServerWorld());

        //endCrystalEntity.setBeamTarget(config.getPos());
        //endCrystalEntity.setInvulnerable(config.isCrystalInvulnerable());
        endCrystalEntity.refreshPositionAndAngles(
                (double)spike.getCenterX() + 0.5, spike.getHeight() + 1, (double)spike.getCenterZ() + 0.5, new Random().nextFloat() * 360.0F, 0.0F
        );
        world.spawnEntity(endCrystalEntity);*/
        //incI();
        StructureHelper.setBlockInChunk(chunk, new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ()), Blocks.BEDROCK.getDefaultState());
    }
}