package nl.smartstreamlabs.sbwdroneconfig;

public record Rd01Input(int action,java.util.UUID token,long sequence,int keys,float yaw) {
    public static void encode(Rd01Input m,net.minecraft.network.FriendlyByteBuf b){b.writeByte(m.action);b.writeUUID(m.token);b.writeLong(m.sequence);b.writeByte(m.keys);b.writeFloat(m.yaw);}
    public static Rd01Input decode(net.minecraft.network.FriendlyByteBuf b){return new Rd01Input(b.readUnsignedByte(),b.readUUID(),b.readLong(),b.readUnsignedByte(),b.readFloat());}
    public static void handle(Rd01Input m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)Rd01Sessions.request(c.getSender(),m);});c.setPacketHandled(true);}
}
