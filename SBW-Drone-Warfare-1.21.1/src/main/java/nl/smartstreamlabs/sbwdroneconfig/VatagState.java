package nl.smartstreamlabs.sbwdroneconfig;
public record VatagState(int entity,java.util.UUID token,boolean active,float yaw,float pitch) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public VatagState(int entity,java.util.UUID token,boolean active){this(entity,token,active,0,0);}
 public static final Type<VatagState> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"vatagstate"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static void encode(VatagState m,net.minecraft.network.FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.token);b.writeBoolean(m.active);b.writeFloat(m.yaw);b.writeFloat(m.pitch);}
 public static VatagState decode(net.minecraft.network.FriendlyByteBuf b){return new VatagState(b.readInt(),b.readUUID(),b.readBoolean(),b.readFloat(),b.readFloat());}
 public static void handle(VatagState m,net.neoforged.neoforge.network.handling.IPayloadContext c){VatagClient.state(m);}
}