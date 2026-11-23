package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.server.level.ServerPlayer;import net.minecraft.core.BlockPos;import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
public interface RescueVehicle {
 VehicleEntity unit();UUID partner();void partner(UUID id,BlockPos last);BlockPos lastPartnerPosition();boolean permitted(ServerPlayer p);boolean occupied();void claim(ServerPlayer p);
 default net.minecraft.world.phys.Vec3 hitch(){return RescueCoupling.point(unit(),0,.65625,this instanceof SupplyRoverEntity?-2.15625:2.25+MedicalTrailerEntity.AXLE_Z);}
}
