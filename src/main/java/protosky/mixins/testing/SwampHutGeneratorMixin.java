package protosky.mixins.testing;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwampHutGenerator.class)
public abstract class SwampHutGeneratorMixin {
    @Accessor("hasWitch")
    abstract boolean getHasWitch();

    @Accessor("hasWitch")
    abstract void setHasWitch(boolean hasWitch);

    @Invoker("spawnCat")
    abstract void invokeSpawnCat(ServerWorldAccess world, BlockBox box);

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        SwampHutGenerator This = ((SwampHutGenerator)(Object)this);
        StructurePieceInvoker structurePieceInvoker = ((StructurePieceInvoker) This);
        ShiftableStructurePieceInvoker shiftableStructurePieceInvoker = ((ShiftableStructurePieceInvoker) This);

        if (shiftableStructurePieceInvoker.invokeAdjustToAverageHeight(world, chunkBox, 0)) {
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
            structurePieceInvoker.invokeAddBlock(world, Blocks.OAK_FENCE.getDefaultState(), 2, 3, 2, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.OAK_FENCE.getDefaultState(), 3, 3, 7, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.AIR.getDefaultState(), 1, 3, 4, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 4, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 5, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.POTTED_RED_MUSHROOM.getDefaultState(), 1, 3, 5, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.CRAFTING_TABLE.getDefaultState(), 3, 2, 6, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.CAULDRON.getDefaultState(), 4, 2, 6, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.OAK_FENCE.getDefaultState(), 1, 2, 1, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, Blocks.OAK_FENCE.getDefaultState(), 5, 2, 1, chunkBox);
            BlockState blockState = Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
            BlockState blockState2 = Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
            BlockState blockState3 = Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
            BlockState blockState4 = Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 0, 4, 1, 6, 4, 1, blockState, blockState, false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 0, 4, 2, 0, 4, 7, blockState2, blockState2, false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 6, 4, 2, 6, 4, 7, blockState3, blockState3, false);
            structurePieceInvoker.invokeFillWithOutline(world, chunkBox, 0, 4, 8, 6, 4, 8, blockState4, blockState4, false);
            structurePieceInvoker.invokeAddBlock(world, blockState.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 0, 4, 1, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, blockState.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 6, 4, 1, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 0, 4, 8, chunkBox);
            structurePieceInvoker.invokeAddBlock(world, blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 6, 4, 8, chunkBox);

            for(int i = 2; i <= 7; i += 5) {
                for(int j = 1; j <= 5; j += 4) {
                    structurePieceInvoker.invokeFillDownwards(world, Blocks.OAK_LOG.getDefaultState(), j, -1, i, chunkBox);
                }
            }

            if (!this.getHasWitch()) {
                BlockPos blockPos = structurePieceInvoker.invokeOffsetPos(2, 2, 5);
                if (chunkBox.contains(blockPos)) {
                    this.setHasWitch(true);
                    WitchEntity witchEntity = EntityType.WITCH.create(world.toServerWorld());
                    witchEntity.setPersistent();
                    witchEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
                    witchEntity.initialize(world, world.getLocalDifficulty(blockPos), SpawnReason.STRUCTURE, null, null);
                    world.spawnEntityAndPassengers(witchEntity);
                }
            }

            //TEST: does this spawn the cat?
            this.invokeSpawnCat(world, chunkBox);
        }

        ci.cancel();
    }
}
