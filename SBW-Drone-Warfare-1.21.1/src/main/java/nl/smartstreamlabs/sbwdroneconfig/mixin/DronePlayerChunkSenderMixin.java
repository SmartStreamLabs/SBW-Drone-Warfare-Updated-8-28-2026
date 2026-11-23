package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import nl.smartstreamlabs.sbwdroneconfig.DroneClientChunkSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserve vanilla batch quotas and acknowledgements; prioritize the actual camera. */
@Mixin(value=PlayerChunkSender.class,remap=false)
public abstract class DronePlayerChunkSenderMixin {
    @Redirect(method="sendNextChunks",remap=false,at=@At(value="INVOKE",
        target="Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    private ChunkPos drone$priorityOrigin(ServerPlayer player) {
        return DroneClientChunkSyncManager.trackingSection(player).chunk();
    }
}
