package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;import net.minecraft.client.multiplayer.ClientLevel;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientLevel.class) public abstract class DvtWorldDirtyMixin {
 @Inject(method="setSectionDirtyWithNeighbors",at=@At("TAIL"))private void dvt$dirty(int x,int y,int z,CallbackInfo ci){DvtClient.dirty(x,y,z,false);}
 @Inject(method="onChunkLoaded",at=@At("TAIL"))private void dvt$loaded(net.minecraft.world.level.ChunkPos p,CallbackInfo ci){DvtClient.dirty(p.x,0,p.z,true);}
 @Inject(method="setBlocksDirty",at=@At("TAIL"))private void dvt$block(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState old,net.minecraft.world.level.block.state.BlockState next,CallbackInfo ci){DvtClient.dirty(p.getX()>>4,p.getY()>>4,p.getZ()>>4,false);}
}
