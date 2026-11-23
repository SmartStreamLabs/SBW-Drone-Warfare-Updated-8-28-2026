package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.neoforged.neoforge.network.handling.IPayloadContext;import java.util.*;import java.util.function.Supplier;
/** Snapshot, remove or authoritative preview. Never a client-supplied collision mesh. */
public record NtReply(int kind,int sequence,UUID id,String dimension,String message,NtTunnel tunnel) implements CustomPacketPayload {
 public static final Type<NtReply> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"nt01_reply")); @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static final int PREVIEW=0,UPSERT=1,REMOVE=2,RESULT=3;
 public static void encode(NtReply m,FriendlyByteBuf b){b.writeVarInt(m.kind);b.writeVarInt(m.sequence);b.writeBoolean(m.id!=null);if(m.id!=null)b.writeUUID(m.id);b.writeUtf(m.dimension,128);b.writeUtf(m.message,512);b.writeBoolean(m.tunnel!=null);if(m.tunnel!=null)b.writeNbt(m.tunnel.save());}
 public static NtReply decode(FriendlyByteBuf b){int k=b.readVarInt(),seq=b.readVarInt();if(k<0||k>3)throw new IllegalArgumentException("Invalid NT-01 reply");UUID id=b.readBoolean()?b.readUUID():null;String dimension=b.readUtf(128),text=b.readUtf(512);return new NtReply(k,seq,id,dimension,text,b.readBoolean()?NtTunnel.load(Objects.requireNonNull(b.readNbt())):null);}
 public static void handle(NtReply m,IPayloadContext ctx){ctx.enqueueWork(()->NtClient.receive(m));}
}
