package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
public record DistanceWarPacket(boolean reset,long id,ResourceLocation dimension,long start,double x,double y,double z,int clip,float volume,float pitch,float range) {
    public static void encode(DistanceWarPacket m,FriendlyByteBuf b){b.writeBoolean(m.reset);b.writeLong(m.id);b.writeResourceLocation(m.dimension);b.writeLong(m.start);b.writeDouble(m.x);b.writeDouble(m.y);b.writeDouble(m.z);b.writeVarInt(m.clip);b.writeFloat(m.volume);b.writeFloat(m.pitch);b.writeFloat(m.range);}
    public static DistanceWarPacket decode(FriendlyByteBuf b){var m=new DistanceWarPacket(b.readBoolean(),b.readLong(),b.readResourceLocation(),b.readLong(),b.readDouble(),b.readDouble(),b.readDouble(),b.readVarInt(),b.readFloat(),b.readFloat(),b.readFloat());
        if(!Double.isFinite(m.x)||!Double.isFinite(m.y)||!Double.isFinite(m.z)||Math.abs(m.x)>3.1e7||Math.abs(m.y)>1e6||Math.abs(m.z)>3.1e7||m.clip<0||m.clip>=DistanceWarPlanner.VARIANTS||!Float.isFinite(m.volume)||m.volume<0||m.volume>1.1||!Float.isFinite(m.pitch)||m.pitch!=1f||!Float.isFinite(m.range)||m.range<64||m.range>4096)throw new IllegalArgumentException("Distance War sound packet");return m;}
    public static void handle(DistanceWarPacket m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistanceWarClient.receive(m));c.setPacketHandled(true);}
}
