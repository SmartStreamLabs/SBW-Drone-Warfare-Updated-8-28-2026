package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.network.FriendlyByteBuf;
public record Mpl01Action(int entity,int action,int slot,UUID target){
 public static void encode(Mpl01Action m,FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeByte(m.action);b.writeByte(m.slot);b.writeUUID(m.target);}
 public static Mpl01Action decode(FriendlyByteBuf b){return new Mpl01Action(b.readVarInt(),b.readUnsignedByte(),b.readUnsignedByte(),b.readUUID());}
 public static void apply(Mpl01Action m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action<=6&&p.getVehicle()instanceof Mpl01Entity v&&v.getId()==m.entity)v.control(p,m.action,m.slot,m.target);}
 public static void handle(Mpl01Action m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
