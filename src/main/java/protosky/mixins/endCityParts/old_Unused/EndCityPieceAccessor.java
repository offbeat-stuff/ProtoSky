package protosky.mixins.endCityParts.old_Unused;

import net.minecraft.structure.EndCityGenerator;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EndCityGenerator.Piece.class)
public interface EndCityPieceAccessor {
    //This gets the ID of the structure piece we want to place.
    @Invoker("getId")
    public Identifier getIdInvoker();
}
