package nl.smartstreamlabs.sbwdroneconfig;
public record Mj03ControlMessage(int entity,int action,String name) {

 private static void apply(Mj03ControlMessage m,net.minecraft.server.level.ServerPlayer p){
  if(p==null || m.action<0 || m.action>4 || !(p.level().getEntity(m.entity) instanceof Mj03Entity t) || !t.isAlive() || p.distanceToSqr(t)>100 || !t.canConfigure(p))return;
  t.control(p,m.action,m.name);AddonNetwork.openMj03(p,new Mj03OpenMessage(t.getId(),java.util.List.copyOf(t.trusted())));
 }
 public static void encode(Mj03ControlMessage m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeByte(m.action);b.writeUtf(m.name,32);}
 public static Mj03ControlMessage decode(net.minecraft.network.FriendlyByteBuf b){return new Mj03ControlMessage(b.readVarInt(),b.readUnsignedByte(),b.readUtf(32));}
public static void handle(Mj03ControlMessage m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
