package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.resources.ResourceLocation;
/** Maximum 64 compact changes per packet, scoped to one watched chunk. */
public record Sk01Packet(ResourceLocation dimension,long chunk,long revision,boolean reset,boolean unload,List<Change>changes) {
 public record Change(UUID id,Sk01Entry entry){}
 public Sk01Packet {changes=List.copyOf(changes);if(changes.size()>64)throw new IllegalArgumentException("SK-01 page too large");}
 public static void encode(Sk01Packet p,FriendlyByteBuf b){b.writeResourceLocation(p.dimension);b.writeLong(p.chunk);b.writeVarLong(p.revision);b.writeBoolean(p.reset);b.writeBoolean(p.unload);b.writeVarInt(p.changes.size());for(var c:p.changes){b.writeUUID(c.id);var e=c.entry;b.writeBoolean(e!=null);if(e!=null){b.writeUUID(e.source());b.writeDouble(e.x());b.writeDouble(e.y());b.writeDouble(e.z());b.writeFloat(e.yaw());b.writeFloat(e.scale());b.writeUtf(e.model(),160);b.writeVarLong(e.created());b.writeBoolean(e.pending());}}}
 public static Sk01Packet decode(FriendlyByteBuf b){var dim=b.readResourceLocation();long chunk=b.readLong(),rev=b.readVarLong();boolean reset=b.readBoolean(),unload=b.readBoolean();int n=b.readVarInt();if(n<0||n>64)throw new IllegalArgumentException("Invalid SK-01 page");var out=new ArrayList<Change>(n);for(int i=0;i<n;i++){var id=b.readUUID();var e=b.readBoolean()?new Sk01Entry(id,b.readUUID(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat(),b.readUtf(160),b.readVarLong(),b.readBoolean()):null;if(e!=null&&e.chunk()!=chunk)throw new IllegalArgumentException("SK-01 chunk mismatch");out.add(new Change(id,e));}return new Sk01Packet(dim,chunk,rev,reset,unload,out);}
 public static void handle(Sk01Packet p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>s){var c=s.get();c.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->Sk01Client.apply(p)));c.setPacketHandled(true);}
}
