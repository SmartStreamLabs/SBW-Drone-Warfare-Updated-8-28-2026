package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.server.level.*;import net.minecraft.world.entity.Entity;import net.minecraft.world.phys.Vec3;
/** Source-specific ammunition, transforms and readiness stay with the launcher. */
public interface PatriotLauncher{
 UUID defenseId();UUID defenseOwner();String defenseOwnerName();ServerLevel defenseLevel();Vec3 defensePosition();
 boolean defenseAvailable();boolean defenseRequest(ServerPlayer actor,UUID target,UUID radar);default Entity defenseEntity(){return null;}
}
