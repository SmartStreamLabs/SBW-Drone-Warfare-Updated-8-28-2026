package nl.smartstreamlabs.sbwdroneconfig;
/** One input/action packet, independent of renderer FPS. Token and monotonic sequence are validated server-side. */
public record Id01Input(int action,java.util.UUID token,long sequence,int keys,float yaw,java.util.UUID target){
    public static final java.util.UUID NONE=new java.util.UUID(0,0);
    public static void encode(Id01Input m,net.minecraft.network.FriendlyByteBuf b){b.writeByte(m.action);b.writeUUID(m.token);b.writeLong(m.sequence);b.writeByte(m.keys);b.writeFloat(m.yaw);b.writeUUID(m.target);}
    public static Id01Input decode(net.minecraft.network.FriendlyByteBuf b){return new Id01Input(b.readUnsignedByte(),b.readUUID(),b.readLong(),b.readUnsignedByte(),b.readFloat(),b.readUUID());}
    public static void handle(Id01Input m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)Id01Sessions.request(c.getSender(),m);});c.setPacketHandled(true);}
}
