package protosky.mixins;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import protosky.ProtoSkySettings;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin
{
    @Shadow
    private BlockPos exitPortalLocation;

    @Shadow @Final private BlockPattern endPortalPattern;

    @Inject(method = "generateEndPortal(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/EndPortalFeature;generateIfValid(Lnet/minecraft/world/gen/feature/FeatureConfig;Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z", shift = At.Shift.BEFORE))
    private void adjustExitPortalLocation(boolean open, CallbackInfo ci) {
        //if (this.exitPortalLocation.getY() < 2)
        //exitPortalLocation = exitPortalLocation.up(2 - exitPortalLocation.getY());
        //exitPortalLocation = new BlockPos(exitPortalLocation.getX(), 2, exitPortalLocation.getZ());
        exitPortalLocation = new BlockPos(exitPortalLocation.getX(), 2, exitPortalLocation.getZ());
    }
}
