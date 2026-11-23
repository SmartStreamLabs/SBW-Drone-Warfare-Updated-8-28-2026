package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.network.FriendlyByteBuf;
public record MoisturePacket(int entity,UUID id,String dimension,float wet,float lens,boolean rain,int stage) {
 public static void encode(MoisturePacket m,FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeUUID(m.id);b.writeUtf(m.dimension,128);b.writeFloat(m.wet);b.writeFloat(m.lens);b.writeBoolean(m.rain);b.writeVarInt(m.stage);}
 public static MoisturePacket decode(FriendlyByteBuf b){return new MoisturePacket(b.readVarInt(),b.readUUID(),b.readUtf(128),b.readFloat(),b.readFloat(),b.readBoolean(),b.readVarInt());}
 public static void handle(MoisturePacket m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->MoistureClient.receive(m));c.setPacketHandled(true);}
}
