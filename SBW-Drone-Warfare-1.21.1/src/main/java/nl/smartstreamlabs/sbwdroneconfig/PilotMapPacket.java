package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.network.FriendlyByteBuf;
public record PilotMapPacket(boolean replace,boolean hidden,List<UUID> players) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<PilotMapPacket> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"pilot_map"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static final int LIMIT=16384;
 public PilotMapPacket {players=List.copyOf(players);if(players.size()>LIMIT)throw new IllegalArgumentException("Pilot map snapshot too large");}
 public static void encode(PilotMapPacket p,FriendlyByteBuf b){b.writeBoolean(p.replace);b.writeBoolean(p.hidden);b.writeVarInt(p.players.size());for(var id:p.players)b.writeUUID(id);}
 public static PilotMapPacket decode(FriendlyByteBuf b){boolean r=b.readBoolean(),h=b.readBoolean();int n=b.readVarInt();if(n<0||n>LIMIT)throw new IllegalArgumentException("Pilot map snapshot too large");var ids=new ArrayList<UUID>(n);for(int i=0;i<n;i++)ids.add(b.readUUID());return new PilotMapPacket(r,h,ids);}
 public static void handle(PilotMapPacket p,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->PilotMapClient.apply(p));}
}
