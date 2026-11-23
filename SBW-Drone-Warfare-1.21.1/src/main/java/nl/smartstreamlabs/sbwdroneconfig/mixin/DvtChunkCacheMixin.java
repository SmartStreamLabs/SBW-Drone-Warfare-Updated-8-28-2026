package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;import net.minecraft.client.multiplayer.*;import net.minecraft.world.level.chunk.*;import net.minecraft.world.level.chunk.status.ChunkStatus;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=ClientChunkCache.class,remap=false) public abstract class DvtChunkCacheMixin {
 @Shadow @Final ClientLevel level;@Shadow @Final private LevelChunk emptyChunk;
 @Inject(method="<init>",at=@At("RETURN"))private void dvt$init(ClientLevel level,int distance,CallbackInfo ci){DvtChunks.radius=Math.max(2,distance)+3;}
 @Inject(method="updateViewCenter",at=@At("HEAD"))private void dvt$center(int x,int z,CallbackInfo ci){DvtChunks.centerX=x;DvtChunks.centerZ=z;}
 @Inject(method="updateViewRadius",at=@At("HEAD"))private void dvt$radius(int r,CallbackInfo ci){DvtChunks.radius=Math.max(2,r)+3;}
 @Inject(method="replaceWithPacketData",at=@At("HEAD"),cancellable=true)private void dvt$receive(int x,int z,net.minecraft.network.FriendlyByteBuf b,net.minecraft.nbt.CompoundTag n,java.util.function.Consumer<net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput> c,CallbackInfoReturnable<LevelChunk> ci){if(DvtChunks.outside(x,z)&&DvtChunks.accepts(x,z))ci.setReturnValue(DvtChunks.receive(level,x,z,b,n,c));}
 @Inject(method="getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;",at=@At("RETURN"),cancellable=true)private void dvt$get(int x,int z,ChunkStatus status,boolean create,CallbackInfoReturnable<LevelChunk> ci){if(ci.getReturnValue()==null||ci.getReturnValue()==emptyChunk){var chunk=DvtChunks.get(x,z);if(chunk!=null)ci.setReturnValue(chunk);}}
 @Inject(method="onLightUpdate",at=@At("TAIL"))private void dvt$light(net.minecraft.world.level.LightLayer type,net.minecraft.core.SectionPos pos,CallbackInfo ci){DvtClient.dirty(pos.x(),pos.y(),pos.z(),false);}
}
