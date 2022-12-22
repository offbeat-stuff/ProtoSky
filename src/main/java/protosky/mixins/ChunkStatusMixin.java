package protosky.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import protosky.WorldGenUtils;
import protosky.stuctures.PillarHelper;
import protosky.stuctures.StructureHelper;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {
    @Inject(method = "method_20613", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus FEATURES. To find the inject method you need to read the bytecode.
    //This is where blocks structures should get placed, now it's where the structures ProtoSky needs get placed.
    private static void FEATURES(ChunkStatus targetStatus, Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, Function function, List<Chunk> chunks, Chunk chunk, boolean regenerate, CallbackInfoReturnable<CompletableFuture> cir) {
        ProtoChunk protoChunk = (ProtoChunk) chunk;
        protoChunk.setLightingProvider(lightingProvider);
        if (regenerate || !chunk.getStatus().isAtLeast(targetStatus)) {
            /*Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
            ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
            generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));
            Blender.tickLeavesAndFluids(chunkRegion, chunk);
            protoChunk.setStatus(targetStatus);*/


            Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));

            //This would normally generate structures, the blocks, not the bounding boxes.
            //generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));

            //Generate do the structures then delete blocks while in the end to remove the end cities
            if (world.getRegistryKey() == World.END) {
                ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
                //This generates all the structures
                StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator);
                //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
                WorldGenUtils.deleteBlocks((ProtoChunk) chunk, world);

                PillarHelper.generate(world, protoChunk);

            //Do it the other way around when generating the ow as to leave the end portal frames.
            } else {
                ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
                //This generates all the structures
                StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator);
                //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
                //WorldGenUtils.deleteBlocks((ProtoChunk) chunk, world);
            }

            //Move the heightmaps down to y0
            //WorldGenUtils.genHeightMaps((ProtoChunk) chunk);

            protoChunk.setStatus(targetStatus);
        }
        cir.setReturnValue(lightingProvider.retainData(chunk).thenApply(Either::left));
        //cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
    }

        /*if (!chunk.getStatus().isAtLeast(targetStatus)) {
            Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));

            ProtoChunk protoChunk = (ProtoChunk)chunk;
            protoChunk.setLightingProvider(lightingProvider);

            ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
            /*if(world.getRegistryKey() == World.END) {
                generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));
                Blender.tickLeavesAndFluids(chunkRegion, chunk);
            } else {*/

                //This would normally generate structures, the blocks, not the bounding boxes.
                //generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));

                //This is for end cities, I couldn't figure out how to generate just the city, so I resorted to just generating the whole thing and deleting the blocks.
                //if(world.getRegistryKey() == World.END) endCityHelper.generateEndCities(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator);
                /*endCityHelper.generateEndCities(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator);

                //Delete the basic terrain and end city blocks that I couldn't figure out how to not generate.
                WorldGenUtils.deleteBlocks((ProtoChunk) chunk, world);
                //Generate the structures I could figure out how to generate.
                StructureHelper.genStructures((ProtoChunk) chunk, world, structureManager, generator);

                WorldGenUtils.genHeightMaps((ProtoChunk) chunk);

                //This isn't needed as we never generate structures that would have entities.
                //WorldGenUtils.clearEntities((ProtoChunk)chunk, world);

                //This was half implemented and never used. polarbub personally doesn't think this is necessary so will not be using it.
                /*if (new ChunkPos(world.getSpawnPos()).equals(chunk.getPos())) {
                    WorldGenUtils.genSpawnPlatform(chunk, world);
                }*/

            //}

            /*Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
            protoChunk.setStatus(targetStatus);
        }

        cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
    }*/

    //These mixins skips few steps in generation to go straight to FEATURES

    /*@Inject(method = "method_17033", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus SPAWN. To find the inject method you need to read the bytecode.
    //Spawning for structures should be done here
    private static void SPAWN(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
        ci.cancel();
    }
    @Inject(method = "method_20613", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus NOISE. To find the inject method you need to read the bytecode.
    private static void NOISE(ChunkStatus targetStatus, Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, Function function, List chunks, Chunk chunk, boolean bl, CallbackInfoReturnable<CompletableFuture> cir) {
        if (!bl && chunk.getStatus().isAtLeast(targetStatus)) {
            cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
        } else {
            ProtoChunk protoChunk = (ProtoChunk)chunk;
            protoChunk.setStatus(targetStatus);
            cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
        }
    }

    @Inject(method = "method_16569", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus SURFACE. To find the inject method you need to read the bytecode.
    private static void SURFACE(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
        System.out.println("ran");
        ci.cancel();
    }

    @Inject(method = "method_38282", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus CARVERS. To find the inject method you need to read the bytecode.
    private static void CARVERS(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
        ci.cancel();
    }
    */

}