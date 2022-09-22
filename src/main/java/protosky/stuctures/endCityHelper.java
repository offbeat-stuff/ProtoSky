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
    private static Structure endCityStructure = null;

    private static synchronized void fixRaceCondition(WorldAccess world) {
        if (!ran) {
            //endCityFeature = world.getRegistryManager().get(Registry.STRUCTURE_KEY).get(Identifier.tryParse("end_city"));
            endCityStructure = world.getRegistryManager().get(Registry.STRUCTURE_KEY).get(Identifier.tryParse("swamp_hut"));
            ran = true;
        }
    }

    public static void generateEndCities(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, ChunkGenerator generator) {
        ChunkPos chunkPos = chunk.getPos();
        //Check if we should generate features
        if (!SharedConstants.method_37896(chunkPos)) {
            //Get access to a bunch of private stuff by making a fake this
            ChunkGeneratorMixin This = ((ChunkGeneratorMixin) generator);

            //Find where to generate
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
            BlockPos blockPos = chunkSectionPos.getMinPos();

            //Get the structure registry
            Registry<Structure> registry = world.getRegistryManager().get(Registry.STRUCTURE_KEY);
            Map<Integer, List<Structure>> map = registry.stream()
                    .collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));

            //Get the structure to place
            List<PlacedFeatureIndexer.IndexedFeatures> list = ((ChunkGeneratorMixin) generator).getIndexedFeaturesListSupplier().get();
            int i = list.size();

            //Make a new random
            ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
            long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());

            //Stuff to do with features
            Set<RegistryEntry<Biome>> set = new ObjectArraySet();
            ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(chunkPosx -> {
                Chunk chunkx = world.getChunk(chunkPosx.x, chunkPosx.z);

                for(ChunkSection chunkSection : chunkx.getSectionArray()) {
                    chunkSection.getBiomeContainer().method_39793(set::add);
                }

            });
            set.retainAll(This.getBiomeSource().getBiomes());

            if(!ran) {
                fixRaceCondition(world);
            }

            try {
                Registry<PlacedFeature> registry2 = world.getRegistryManager().get(Registry.PLACED_FEATURE_KEY);
                int j = Math.max(GenerationStep.Feature.values().length, i);

                for(int k = 0; k < j; ++k) {
                    int m = 0;
                    if (structureAccessor.shouldGenerateStructures()) {
                        for(Structure structure : map.getOrDefault(k, Collections.emptyList())) {
                            chunkRandom.setDecoratorSeed(l, m, k);
                            Supplier<String> supplier = () -> (String)registry.getKey(structure).map(Object::toString).orElseGet(structure::toString);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier);
                                structureAccessor.getStructureStarts(chunkSectionPos, structure)
                                        .forEach(start -> {
                                            //ADD: Move stronghold code here
                                            //ADD: Move pillar code here
                                            //ADD: Add all the edit.txt moves here
                                            if (start.getStructure().equals(endCityStructure)) {
                                                System.out.println("hut");
                                            }
                                            start.place(world, structureAccessor, generator, chunkRandom, This.getBlockBoxForChunkInvoker(chunk), chunkPos);
                                        });
                            } catch (Exception var29) {
                                CrashReport crashReport = CrashReport.create(var29, "Feature placement");
                                crashReport.addElement("Feature").add("Description", supplier::get);
                                throw new CrashException(crashReport);
                            }

                            ++m;
                        }
                    }

                    if (k < i) {
                        IntSet intSet = new IntArraySet();

                        for(RegistryEntry<Biome> registryEntry : set) {
                            List<RegistryEntryList<PlacedFeature>> list3 = This.getGenerationSettingsGetter().apply(registryEntry)
                                    .getFeatures();
                            if (k < list3.size()) {
                                RegistryEntryList<PlacedFeature> registryEntryList = list3.get(k);
                                PlacedFeatureIndexer.IndexedFeatures indexedFeatures = list.get(k);
                                registryEntryList.stream()
                                        .map(RegistryEntry::value)
                                        .forEach(placedFeaturex -> intSet.add(indexedFeatures.indexMapping().applyAsInt(placedFeaturex)));
                            }
                        }

                        int n = intSet.size();
                        int[] is = intSet.toIntArray();
                        Arrays.sort(is);
                        PlacedFeatureIndexer.IndexedFeatures indexedFeatures2 = list.get(k);

                        for(int o = 0; o < n; ++o) {
                            int p = is[o];
                            PlacedFeature placedFeature = indexedFeatures2.features().get(p);
                            Supplier<String> supplier2 = () -> (String)registry2.getKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                            chunkRandom.setDecoratorSeed(l, p, k);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier2);
                                placedFeature.generate(world, generator, chunkRandom, blockPos);
                            } catch (Exception var30) {
                                CrashReport crashReport2 = CrashReport.create(var30, "Feature placement");
                                crashReport2.addElement("Feature").add("Description", supplier2::get);
                                throw new CrashException(crashReport2);
                            }
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
    }
}
