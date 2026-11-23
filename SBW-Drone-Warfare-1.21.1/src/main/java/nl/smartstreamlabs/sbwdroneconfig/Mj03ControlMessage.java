package nl.smartstreamlabs.sbwdroneconfig;
public record Mj03ControlMessage(int entity,int action,String name) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<Mj03ControlMessage> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"mj03controlmessage"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}

 private static void apply(Mj03ControlMessage m,net.minecraft.server.level.ServerPlayer p){
  if(p==null || m.action<0 || m.action>4 || !(p.level().getEntity(m.entity) instanceof Mj03Entity t) || !t.isAlive() || p.distanceToSqr(t)>100 || !t.canConfigure(p))return;
  t.control(p,m.action,m.name);AddonNetwork.openMj03(p,new Mj03OpenMessage(t.getId(),java.util.List.copyOf(t.trusted())));
 }
 public static void encode(Mj03ControlMessage m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeByte(m.action);b.writeUtf(m.name,32);}
 public static Mj03ControlMessage decode(net.minecraft.network.FriendlyByteBuf b){return new Mj03ControlMessage(b.readVarInt(),b.readUnsignedByte(),b.readUtf(32));}
public static void handle(Mj03ControlMessage m,net.neoforged.neoforge.network.handling.IPayloadContext c){if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)apply(m,p);}
}
