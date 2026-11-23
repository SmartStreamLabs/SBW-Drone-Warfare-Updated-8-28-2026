package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.DvtTracker;import net.minecraft.server.level.ServerPlayer;import org.spongepowered.asm.mixin.*;
@Mixin(targets="net.minecraft.server.level.ChunkMap$TrackedEntity")public abstract class DvtTrackedMixin implements DvtTracker {@Shadow public abstract void updatePlayer(ServerPlayer p);public void dvt$refresh(ServerPlayer p){updatePlayer(p);}}
