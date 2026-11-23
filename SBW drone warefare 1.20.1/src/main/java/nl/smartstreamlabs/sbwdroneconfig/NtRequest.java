package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;import net.minecraft.world.phys.Vec3;import net.minecraftforge.network.NetworkEvent;import java.util.*;import java.util.function.Supplier;
public record NtRequest(int action,int sequence,UUID id,long revision,List<Vec3> points,NtGeometry.Settings settings,String fingerprint){
 public static final int PREVIEW=0,BUILD=1,REMOVE=2,UNDO=3;
 public NtRequest{points=List.copyOf(points);if(action<0||action>3||sequence<0||points.size()>64||fingerprint.length()>32)throw new IllegalArgumentException("Invalid NT-01 request");for(Vec3 p:points)if(!NtGeometry.finite(p.x,p.y,p.z)||Math.abs(p.x)>29999980||Math.abs(p.z)>29999980||p.y< -2048||p.y>4096)throw new IllegalArgumentException("Invalid point");}
 public static void encode(NtRequest m,FriendlyByteBuf b){b.writeVarInt(m.action);b.writeVarInt(m.sequence);b.writeBoolean(m.id!=null);if(m.id!=null)b.writeUUID(m.id);b.writeLong(m.revision);NtTunnel.writePoints(b,m.points);var s=m.settings;b.writeDouble(s.width());b.writeDouble(s.height());b.writeDouble(s.spacing());b.writeDouble(s.trim());b.writeBoolean(s.sides());b.writeUtf(m.fingerprint,32);}
 public static NtRequest decode(FriendlyByteBuf b){int a=b.readVarInt(),seq=b.readVarInt();UUID id=b.readBoolean()?b.readUUID():null;long rev=b.readLong();var points=NtTunnel.readPoints(b);var s=new NtGeometry.Settings(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readBoolean());return new NtRequest(a,seq,id,rev,points,s,b.readUtf(32));}
 public static void handle(NtRequest m,Supplier<NetworkEvent.Context> c){var ctx=c.get();ctx.enqueueWork(()->{if(ctx.getSender()!=null)NtServer.request(ctx.getSender(),m);});ctx.setPacketHandled(true);}
}
