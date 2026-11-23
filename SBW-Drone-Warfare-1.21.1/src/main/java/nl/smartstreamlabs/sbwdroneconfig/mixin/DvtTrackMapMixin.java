package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;import net.minecraft.server.level.*;import net.minecraft.world.level.ChunkPos;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import java.util.*;
@Mixin(value=ChunkMap.class,remap=false)public abstract class DvtTrackMapMixin implements DvtTrackMap {
 @Shadow @Final private it.unimi.dsi.fastutil.ints.Int2ObjectMap<?> entityMap;@Shadow @Final private ServerLevel level;
 public void dvt$refresh(ServerPlayer p){for(Object t:entityMap.values())((DvtTracker)t).dvt$refresh(p);}
 @Inject(method="getPlayers(Lnet/minecraft/world/level/ChunkPos;Z)Ljava/util/List;",at=@At("RETURN"),cancellable=true)private void dvt$players(ChunkPos pos,boolean edge,CallbackInfoReturnable<List<ServerPlayer>> ci){var list=new ArrayList<>(ci.getReturnValue());for(var p:level.players())if(!list.contains(p)&&DvtServer.watches(p,pos))list.add(p);ci.setReturnValue(list);}
 @Inject(method="isChunkTracked",at=@At("HEAD"),cancellable=true)private void dvt$tracked(ServerPlayer p,int x,int z,CallbackInfoReturnable<Boolean> ci){if(DvtServer.watches(p,new ChunkPos(x,z)))ci.setReturnValue(true);}
}
