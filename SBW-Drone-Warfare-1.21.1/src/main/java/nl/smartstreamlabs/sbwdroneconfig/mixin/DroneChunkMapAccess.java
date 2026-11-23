package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=ChunkMap.class,remap=false)
public interface DroneChunkMapAccess {
    @Invoker(value="isChunkTracked",remap=false)
    boolean drone$isChunkTracked(ServerPlayer player,int x,int z);
}
