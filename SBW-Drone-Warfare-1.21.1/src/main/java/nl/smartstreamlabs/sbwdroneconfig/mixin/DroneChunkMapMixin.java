package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.ChunkPos;
import nl.smartstreamlabs.sbwdroneconfig.DroneClientChunkSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value=ChunkMap.class,remap=false)
public abstract class DroneChunkMapMixin {
    @Redirect(remap=false,method={"move","updatePlayerPos","updatePlayerStatus"},at=@At(value="INVOKE",
        target="Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos drone$section(EntityAccess entity) {
        return entity instanceof ServerPlayer p ? DroneClientChunkSyncManager.trackingSection(p) : SectionPos.of(entity);
    }
    @Redirect(remap=false,method="updateChunkTracking",at=@At(value="INVOKE",
        target="Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    private ChunkPos drone$center(ServerPlayer player) { return DroneClientChunkSyncManager.trackingSection(player).chunk(); }
}
