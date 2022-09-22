package protosky.mixins.testing;

import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShiftableStructurePiece.class)
public interface ShiftableStructurePieceInvoker {

    @Invoker("adjustToAverageHeight")
    boolean invokeAdjustToAverageHeight(WorldAccess world, BlockBox boundingBox, int deltaY);
}
