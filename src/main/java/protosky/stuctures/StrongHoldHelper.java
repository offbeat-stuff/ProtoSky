package protosky.stuctures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.structure.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.structure.Structure;

import java.util.Random;

import static protosky.stuctures.StructureHelper.isIntersecting;
import static protosky.stuctures.StructureHelper.setBlockInStructure;

public class StrongHoldHelper {
    /*private static boolean isIntersecting(StructureStart stronghold, BlockBox posBox) {
        StructurePiecesHolder structurePiecesHolder = new StructurePiecesCollector();
        if (stronghold != null) {
            for (Object piece : stronghold.getChildren()) {
                structurePiecesHolder.addPiece((StructurePiece) piece);
            }
            return structurePiecesHolder.getIntersecting(posBox) != null;
        }
        return false;
    }*/

    public static void genEndPortal(ProtoChunk chunk, StrongholdGenerator.PortalRoom room)
    {
        BlockState northFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState southFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState eastFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState westFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);

        setBlockInStructure(room, chunk, northFrame, 4, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 5, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 6, 3, 8);
        setBlockInStructure(room, chunk, southFrame, 4, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 5, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 6, 3, 12);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 9);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 10);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 11);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 9);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 10);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 11);
    }

    public static boolean ran = false;
    private static Structure strongHoldFeature = null;

    private static synchronized void fixRaceCondition(WorldAccess world) {
        if(!ran) {
            strongHoldFeature = world.getRegistryManager().get(Registry.STRUCTURE_KEY).get(Identifier.tryParse("stronghold"));
            ran = true;
        }
    }

    public static void processStronghold(WorldAccess world, ProtoChunk chunk) {
        if(!ran) {
            fixRaceCondition(world);
        }

        for (long startPosLong : chunk.getStructureReferences(strongHoldFeature)) {
            ChunkPos startPos = new ChunkPos(startPosLong);
            ProtoChunk startChunk = (ProtoChunk) world.getChunk(startPos.x, startPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart stronghold = startChunk.getStructureStart(strongHoldFeature);

            if (stronghold != null && isIntersecting(stronghold, chunk))
            {
                ChunkPos pos = chunk.getPos();
                for (Object piece : stronghold.getChildren())
                {
                    if (((StructurePiece)piece).getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ()))
                    {
                        if (piece instanceof StrongholdGenerator.PortalRoom)
                            genEndPortal(chunk, (StrongholdGenerator.PortalRoom) piece);
                    }
                }
            }
        }
    }
}
