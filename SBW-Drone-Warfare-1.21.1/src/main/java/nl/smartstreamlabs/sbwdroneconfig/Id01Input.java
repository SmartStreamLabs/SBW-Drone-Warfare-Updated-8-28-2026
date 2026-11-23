package nl.smartstreamlabs.sbwdroneconfig;
/** One input/action packet, independent of renderer FPS. Token and monotonic sequence are validated server-side. */
public record Id01Input(int action,java.util.UUID token,long sequence,int keys,float yaw,java.util.UUID target) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<Id01Input> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"id01input"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static final java.util.UUID NONE=new java.util.UUID(0,0);
    public static void encode(Id01Input m,net.minecraft.network.FriendlyByteBuf b){b.writeByte(m.action);b.writeUUID(m.token);b.writeLong(m.sequence);b.writeByte(m.keys);b.writeFloat(m.yaw);b.writeUUID(m.target);}
    public static Id01Input decode(net.minecraft.network.FriendlyByteBuf b){return new Id01Input(b.readUnsignedByte(),b.readUUID(),b.readLong(),b.readUnsignedByte(),b.readFloat(),b.readUUID());}
    public static void handle(Id01Input m,net.neoforged.neoforge.network.handling.IPayloadContext c){if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)Id01Sessions.request(p,m);}
}
