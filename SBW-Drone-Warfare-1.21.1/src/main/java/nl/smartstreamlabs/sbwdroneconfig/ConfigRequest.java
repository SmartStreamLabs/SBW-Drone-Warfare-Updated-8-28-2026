package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record ConfigRequest(int request,String file,long revision,int part,int parts,String json) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload{
 public static final Type<ConfigRequest> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","configrequest"));
 @Override public Type<ConfigRequest>type(){return TYPE;}
 public ConfigRequest{if(file.length()>128||parts<1||parts>ConfigWire.MAX_PARTS||part<0||part>=parts||json.length()>ConfigWire.PIECE)throw new IllegalArgumentException("Invalid config request");}
 public static void encode(ConfigRequest m,FriendlyByteBuf b){b.writeVarInt(m.request);b.writeUtf(m.file,128);b.writeVarLong(m.revision);b.writeVarInt(m.part);b.writeVarInt(m.parts);b.writeUtf(m.json,ConfigWire.PIECE);}
 public static ConfigRequest decode(FriendlyByteBuf b){return new ConfigRequest(b.readVarInt(),b.readUtf(128),b.readVarLong(),b.readVarInt(),b.readVarInt(),b.readUtf(ConfigWire.PIECE));}
 public static void handle(ConfigRequest m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)ConfigServer.request(p,m);});}
}
