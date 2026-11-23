package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
/** A client may only dismount itself from its current, supported vehicle. */
public record GroundVehicleExit(int entity) {
 public static boolean supported(Entity e){return e instanceof VatagEntity||e instanceof Mpl01Entity||e instanceof Adv01Entity||e instanceof Mj03Entity||e instanceof MedicalTrailerEntity;}
 public static void encode(GroundVehicleExit m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);}
 public static GroundVehicleExit decode(net.minecraft.network.FriendlyByteBuf b){return new GroundVehicleExit(b.readVarInt());}
 public static void apply(GroundVehicleExit m,ServerPlayer p){
  if(p==null||!p.isAlive()||p.isSpectator())return;Entity v=p.getVehicle();
  if(v==null||v.getId()!=m.entity||!supported(v))return;
  if(v instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity vehicle&&vehicle.getControllingPassenger()==p){vehicle.processInput((short)0);vehicle.setFireInputDown(false);vehicle.setForwardInputDown(false);vehicle.setBackInputDown(false);vehicle.setLeftInputDown(false);vehicle.setRightInputDown(false);vehicle.setUpInputDown(true);}
  p.stopRiding();
 }
 public static void handle(GroundVehicleExit m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
