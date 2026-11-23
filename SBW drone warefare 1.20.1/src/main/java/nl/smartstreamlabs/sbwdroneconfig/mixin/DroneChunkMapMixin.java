package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.util.Mth;
import nl.smartstreamlabs.sbwdroneconfig.DroneClientChunkSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** One origin for PlayerMap, DistanceManager, cache centers and update watchers. */
@Mixin(ChunkMap.class)
public abstract class DroneChunkMapMixin {
    @Redirect(method={"move","updatePlayerPos","updatePlayerStatus"},at=@At(value="INVOKE",
        target="Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos drone$section(EntityAccess entity) {
        return entity instanceof ServerPlayer p ? DroneClientChunkSyncManager.trackingSection(p) : SectionPos.of(entity);
    }
    @Redirect(method={"move","updatePlayerStatus"},at=@At(value="INVOKE",
        target="Lnet/minecraft/server/level/ServerPlayer;getBlockX()I"))
    private int drone$x(ServerPlayer p) { return Mth.floor(DroneClientChunkSyncManager.trackingPosition(p).x); }
    @Redirect(method={"move","updatePlayerStatus"},at=@At(value="INVOKE",
        target="Lnet/minecraft/server/level/ServerPlayer;getBlockZ()I"))
    private int drone$z(ServerPlayer p) { return Mth.floor(DroneClientChunkSyncManager.trackingPosition(p).z); }
}
