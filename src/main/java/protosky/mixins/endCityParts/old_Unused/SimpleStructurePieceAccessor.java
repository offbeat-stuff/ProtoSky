package protosky.mixins.endCityParts;

import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleStructurePiece.class)
public interface SimpleStructurePieceAccessor {
    @Accessor("placementData")
    StructurePlacementData getPlacementData();
}
