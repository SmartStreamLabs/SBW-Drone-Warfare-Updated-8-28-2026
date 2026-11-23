package nl.smartstreamlabs.sbwdroneconfig;

public record Rd01Input(int action,java.util.UUID token,long sequence,int keys,float yaw) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<Rd01Input> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"rd01input"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void encode(Rd01Input m,net.minecraft.network.FriendlyByteBuf b){b.writeByte(m.action);b.writeUUID(m.token);b.writeLong(m.sequence);b.writeByte(m.keys);b.writeFloat(m.yaw);}
    public static Rd01Input decode(net.minecraft.network.FriendlyByteBuf b){return new Rd01Input(b.readUnsignedByte(),b.readUUID(),b.readLong(),b.readUnsignedByte(),b.readFloat());}
    public static void handle(Rd01Input m,net.neoforged.neoforge.network.handling.IPayloadContext c){if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)Rd01Sessions.request(p,m);}
}
