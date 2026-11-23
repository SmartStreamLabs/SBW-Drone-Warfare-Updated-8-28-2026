package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record ConfigReply(int request,String file,long revision,boolean writable,int part,int parts,String status,String json){
 public ConfigReply{if(file.length()>128||parts<1||parts>ConfigWire.MAX_PARTS||part<0||part>=parts||json.length()>ConfigWire.PIECE||status.length()>512)throw new IllegalArgumentException("Invalid config reply");}
 public static void encode(ConfigReply m,FriendlyByteBuf b){b.writeVarInt(m.request);b.writeUtf(m.file,128);b.writeVarLong(m.revision);b.writeBoolean(m.writable);b.writeVarInt(m.part);b.writeVarInt(m.parts);b.writeUtf(m.status,512);b.writeUtf(m.json,ConfigWire.PIECE);}
 public static ConfigReply decode(FriendlyByteBuf b){return new ConfigReply(b.readVarInt(),b.readUtf(128),b.readVarLong(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readUtf(512),b.readUtf(ConfigWire.PIECE));}
 public static void handle(ConfigReply m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->ConfigRemoteClient.receive(m));c.setPacketHandled(true);}
}
