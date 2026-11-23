package nl.smartstreamlabs.sbwdroneconfig;
public record Mj03OpenMessage(int entity,java.util.List<String> names) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<Mj03OpenMessage> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"mj03openmessage"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static void encode(Mj03OpenMessage m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeVarInt(m.names.size());for(String n:m.names)b.writeUtf(n,32);}
 public static Mj03OpenMessage decode(net.minecraft.network.FriendlyByteBuf b){int id=b.readVarInt(),count=b.readVarInt();if(count<0||count>128)throw new IllegalArgumentException("MJ03 whitelist size");var names=new java.util.ArrayList<String>();for(int i=0;i<count;i++)names.add(b.readUtf(32));return new Mj03OpenMessage(id,java.util.List.copyOf(names));}
public static void handle(Mj03OpenMessage m,net.neoforged.neoforge.network.handling.IPayloadContext c){Mj03Screen.open(m);}
}
