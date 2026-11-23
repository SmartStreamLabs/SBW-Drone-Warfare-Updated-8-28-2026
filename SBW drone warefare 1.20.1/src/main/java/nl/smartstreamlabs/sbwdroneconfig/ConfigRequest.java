package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record ConfigRequest(int request,String file,long revision,int part,int parts,String json){
 public ConfigRequest{if(file.length()>128||parts<1||parts>ConfigWire.MAX_PARTS||part<0||part>=parts||json.length()>ConfigWire.PIECE)throw new IllegalArgumentException("Invalid config request");}
 public static void encode(ConfigRequest m,FriendlyByteBuf b){b.writeVarInt(m.request);b.writeUtf(m.file,128);b.writeVarLong(m.revision);b.writeVarInt(m.part);b.writeVarInt(m.parts);b.writeUtf(m.json,ConfigWire.PIECE);}
 public static ConfigRequest decode(FriendlyByteBuf b){return new ConfigRequest(b.readVarInt(),b.readUtf(128),b.readVarLong(),b.readVarInt(),b.readVarInt(),b.readUtf(ConfigWire.PIECE));}
 public static void handle(ConfigRequest m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p!=null)ConfigServer.request(p,m);});c.setPacketHandled(true);}
}
