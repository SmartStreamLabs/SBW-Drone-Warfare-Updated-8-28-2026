package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record DvtRequest(BlockPos pos,int action,UUID drone,String player){
 public static void encode(DvtRequest m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeVarInt(m.action);b.writeBoolean(m.drone!=null);if(m.drone!=null)b.writeUUID(m.drone);b.writeUtf(m.player,64);}
 public static DvtRequest decode(FriendlyByteBuf b){return new DvtRequest(b.readBlockPos(),b.readVarInt(),b.readBoolean()?b.readUUID():null,b.readUtf(64));}
 public static void handle(DvtRequest m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)DvtServer.request(c.getSender(),m);});c.setPacketHandled(true);}
}
