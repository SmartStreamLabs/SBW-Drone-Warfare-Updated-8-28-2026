package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.network.FriendlyByteBuf;
public record MoisturePacket(int entity,UUID id,String dimension,float wet,float lens,boolean rain,int stage)implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<MoisturePacket> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","moisture"));public Type<MoisturePacket> type(){return TYPE;}
 public static void encode(MoisturePacket m,FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeUUID(m.id);b.writeUtf(m.dimension,128);b.writeFloat(m.wet);b.writeFloat(m.lens);b.writeBoolean(m.rain);b.writeVarInt(m.stage);}
 public static MoisturePacket decode(FriendlyByteBuf b){return new MoisturePacket(b.readVarInt(),b.readUUID(),b.readUtf(128),b.readFloat(),b.readFloat(),b.readBoolean(),b.readVarInt());}
 public static void handle(MoisturePacket m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->MoistureClient.receive(m));}
}
