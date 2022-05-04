package protosky.mixins.endCityParts;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorMixin {
    @Accessor("populationSource")
    BiomeSource getPopulationSource();

    @Invoker("getBlockBoxForChunk")
    public BlockBox getBlockBoxForChunkInvoker(Chunk chunk);
}
