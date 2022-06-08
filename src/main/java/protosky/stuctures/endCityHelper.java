package protosky.stuctures;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.structure.Structure;
import protosky.mixins.endCityParts.ChunkGeneratorMixin;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class endCityHelper {
    private static boolean ran = false;
    private static Structure endCityFeature = null;

    private static synchronized void fixRaceCondition(WorldAccess world) {
        if (!ran) {
            endCityFeature = world.getRegistryManager().get(Registry.STRUCTURE_KEY).get(Identifier.tryParse("end_city"));
            ran = true;
        }
    }

    public static void generateEndCities(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, ChunkGenerator generator) {
        ChunkPos chunkPos = chunk.getPos();
        //Check if we should generate features
        if (!SharedConstants.method_37896(chunkPos)) {
            //Find where to generate
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
            BlockPos blockPos = chunkSectionPos.getMinPos();

            //Get the structure registry
            Registry<Structure> registry = world.getRegistryManager().get(Registry.STRUCTURE_KEY);
            Map<Integer, List<Structure>> map = registry.stream()
                    .collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));

            //Get the structure to place
            List<PlacedFeatureIndexer.IndexedFeatures> list = ((ChunkGeneratorMixin) generator).getIndexedFeaturesListSupplier().get();

            //Make a new random
            ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
            long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());

            int i = list.size();

            try {
                Registry<PlacedFeature> registry2 = world.getRegistryManager().get(Registry.PLACED_FEATURE_KEY);
                int j = Math.max(GenerationStep.Feature.values().length, i);

                for(int k = 0; k < j; ++k) {
                    int m = 0;
                    if (structureAccessor.shouldGenerateStructures()) {
                        for(Object structure : (List)map.getOrDefault(k, Collections.emptyList())) {
                            chunkRandom.setDecoratorSeed(l, m, k);
                            Supplier<String> supplier = () -> (String)registry.getKey((Structure) structure).map(Object::toString).orElseGet(structure::toString);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier);
                                structureAccessor.getStructureStarts(chunkSectionPos, (Structure) structure)
                                        .forEach(start -> start.place(world, structureAccessor, generator, chunkRandom, ((ChunkGeneratorMixin) generator).getBlockBoxForChunkInvoker(chunk), chunkPos));
                            } catch (Exception var29) {
                                CrashReport crashReport = CrashReport.create(var29, "Feature placement");
                                crashReport.addElement("Feature").add("Description", supplier::get);
                                throw new CrashException(crashReport);
                            }

                            ++m;
                        }
                    }
                }

                world.setCurrentlyGeneratingStructureName(null);
            } catch (Exception var31) {
                CrashReport crashReport3 = CrashReport.create(var31, "Biome decoration");
                crashReport3.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", l);
                throw new CrashException(crashReport3);
            }
        }

        /*
        //Position Stuff
        ChunkPos chunkPos = chunk.getPos();
        if (!SharedConstants.method_37896(chunkPos)) {
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
            BlockPos blockPos = chunkSectionPos.getMinPos();

            //Registry Stuff
            Registry<Structure> registry = world.getRegistryManager().get(Registry.STRUCTURE_KEY);
            Map<Integer, List<Structure>> map = registry.stream()
                    .collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));

            List<PlacedFeatureIndexer.IndexedFeatures> list = ((ChunkGeneratorMixin) generator).getIndexedFeaturesListSupplier().get();
            ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
            long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());


            int i = list.size();

            fixRaceCondition(world);

            try {
                int j = Math.max(GenerationStep.Feature.values().length, i);

                for(int k = 0; k < j; ++k) {
                    int m = 0;
                    if (structureAccessor.shouldGenerateStructures()) {
                        for (ConfiguredStructureFeature<?, ?> configuredStructureFeature : map.getOrDefault(k, Collections.emptyList())) {
                            chunkRandom.setDecoratorSeed(l, m, k);
                            Supplier<String> supplier = () -> (String) registry.getKey(configuredStructureFeature)
                                    .map(Object::toString)
                                    .orElseGet(configuredStructureFeature::toString);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier);
                                structureAccessor.getStructureStarts(chunkSectionPos, configuredStructureFeature)
                                        .forEach(
                                                structureStart -> {
                                                    if (structureStart.getStructure().equals(endCityFeature)) {
                                                        structureStart.place(
                                                                world, structureAccessor, generator, chunkRandom, ((ChunkGeneratorMixin) generator).getBlockBoxForChunkInvoker(chunk), chunkPos
                                                        );
                                                    }
                                                }
                                        );
                            } catch (Exception var29) {
                                CrashReport crashReport = CrashReport.create(var29, "Feature placement");
                                crashReport.addElement("Feature").add("Description", supplier::get);
                                throw new CrashException(crashReport);
                            }

                            ++m;
                        }
                    }
                }

                world.setCurrentlyGeneratingStructureName(null);
            } catch (Exception var31) {
                CrashReport crashReport3 = CrashReport.create(var31, "Biome decoration");
                crashReport3.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", l);
                throw new CrashException(crashReport3);
            }
        }*/
    }

    //
    //This whole commented out section was my own attempt to generate end cities. It failed, but I wanted to keep it around for later reference.
    //

    /*//This gets the two corners of a BlockBox needed to recreate it. Left is min, right it max
    private static Pair<BlockPos, BlockPos> getEdges(BlockBox blockBox) {
        return new MutablePair<>(new BlockPos(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ()), new BlockPos(blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ()));
    }

    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        HeightLimitView heightLimitView = chunk.getHeightLimitView();
        int k = heightLimitView.getBottomY() + 1;
        int l = heightLimitView.getTopY() - 1;
        return new BlockBox(i, k, j, i + 15, l, j + 15);
    }

    private static boolean ran = false;
    private static ConfiguredStructureFeature<?, ?> endCityFeature = null;

    private static synchronized void fixRaceCondition(WorldAccess world) {
        if (!ran) {
            endCityFeature = world.getRegistryManager().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY).get(Identifier.tryParse("end_city"));
            ran = true;
        }
    }

    public static void processEndCity(WorldAccess world, ProtoChunk chunk, StructureManager structureManager, ChunkGenerator generator) {
        //Grab the ConfiguredStructureFeature of end city
        if (!ran) {
            fixRaceCondition(world);
        }

        //Get chunk bounding box
        ChunkPos chunkPos = chunk.getPos();
        //BlockBox chunkBox = new BlockBox(chunkPos.getStartX(), chunk.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), chunk.getTopY(), chunkPos.getEndZ());
        BlockBox chunkBoundingBox = ((ChunkGeneratorMixin) generator).getBlockBoxForChunkInvoker(chunk);

        //Loop through end cities in our chunk
        for (long startPosLong : chunk.getStructureReferences(endCityFeature)) {
            //Get the end city
            ChunkPos startPos = new ChunkPos(startPosLong);
            ProtoChunk startChunk = (ProtoChunk) world.getChunk(startPos.x, startPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart cityStart = startChunk.getStructureStart(endCityFeature);

            //Check if the end city is valid
            if (cityStart != null && isIntersecting(cityStart, chunk)) {

                //Loop through the parts of the end city
                for (StructurePiece piece : cityStart.getChildren()) {
                    //Check if the structure piece intersects with the chunk
                    if (piece.getBoundingBox().intersectsXZ(chunkPos.getStartX(), chunkPos.getStartZ(), chunkPos.getEndX(), chunkPos.getEndZ())) {

                        //To show that were getting here successfully
                        //chunk.setBlockState(chunk.getPos().getStartPos(), Blocks.BEDROCK.getDefaultState(), false);

                        //ServerWorld serverWorld = (ServerWorld) world;
                        //piece.generate(serverWorld, serverWorld.getStructureAccessor(), generator, new Random(), chunkBoundingBox, chunkPos, chunk.getPos().getStartPos());
                        
                        Optional<Structure> structureOptional = structureManager.getStructure(((EndCityPieceAccessor) piece).getIdInvoker());
                        if(structureOptional.isPresent()) {
                            //BlockIgnoreStructureProcessor blockIgnoreStructureProcessor = BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS;
                            //StructurePlacementData placementData = new StructurePlacementData().setBoundingBox(chunkBoundingBox).setIgnoreEntities(false).setPlaceFluids(true).setMirror(piece.getMirror()).setInitializeMobs(false).setPosition(new BlockPos(0,0,0)).setUpdateNeighbors(false).addProcessor(blockIgnoreStructureProcessor);
                            //TEST: Is this the cause of the non-reproducibility?
                            //      Maybe only run once and see the results
                            //      Maybe it's from trying to place entities?
                            
                            StructurePlacementData placementData = ((SimpleStructurePieceAccessor) piece).getPlacementData();

                            LOGGER.info("ran");

                            //Get the bounding box in two blockPoses
                            Pair<BlockPos, BlockPos> pieceBlockPosBoundBox = getEdges(piece.getBoundingBox());
                            if(structureOptional.get().place((ServerWorldAccess) world, *//*Start of piece*//*TEST: Does replacing my bounding box system with the real ones from the piece do anything?
                                                                                                                Maybe these are chunk not structure and maybe the first part places because it's by chance in our chunk?
                                                                                                                *//* pieceBlockPosBoundBox.getLeft(), /*End of piece*//* pieceBlockPosBoundBox.getRight(), placementData, new Random(), 2)) {
                                LOGGER.info(structureOptional.get().getInfosForBlock(pieceBlockPosBoundBox.getLeft(), placementData, Blocks.STRUCTURE_BLOCK).toString());
                            }
                        }
                    }
                }
            }
        }
    }*/
}
