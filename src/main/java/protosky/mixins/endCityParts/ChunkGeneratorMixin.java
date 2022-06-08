package protosky.mixins.endCityParts;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorMixin {

    @Accessor("indexedFeaturesListSupplier")
    Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> getIndexedFeaturesListSupplier();

    @Invoker("getBlockBoxForChunk")
    public BlockBox getBlockBoxForChunkInvoker(Chunk chunk);
}
