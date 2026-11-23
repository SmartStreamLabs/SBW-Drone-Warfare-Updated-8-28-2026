package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;import net.minecraftforge.network.NetworkEvent;import java.util.*;import java.util.function.Supplier;
/** Snapshot, remove or authoritative preview. Never a client-supplied collision mesh. */
public record NtReply(int kind,int sequence,UUID id,String dimension,String message,NtTunnel tunnel){
 public static final int PREVIEW=0,UPSERT=1,REMOVE=2,RESULT=3;
 public static void encode(NtReply m,FriendlyByteBuf b){b.writeVarInt(m.kind);b.writeVarInt(m.sequence);b.writeBoolean(m.id!=null);if(m.id!=null)b.writeUUID(m.id);b.writeUtf(m.dimension,128);b.writeUtf(m.message,512);b.writeBoolean(m.tunnel!=null);if(m.tunnel!=null)b.writeNbt(m.tunnel.save());}
 public static NtReply decode(FriendlyByteBuf b){int k=b.readVarInt(),seq=b.readVarInt();if(k<0||k>3)throw new IllegalArgumentException("Invalid NT-01 reply");UUID id=b.readBoolean()?b.readUUID():null;String dimension=b.readUtf(128),text=b.readUtf(512);return new NtReply(k,seq,id,dimension,text,b.readBoolean()?NtTunnel.load(Objects.requireNonNull(b.readNbt())):null);}
 public static void handle(NtReply m,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->NtClient.receive(m));c.get().setPacketHandled(true);}
}
