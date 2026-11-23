package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record UfMusicRequest(BlockPos pos,UUID identity,int action,int value) {
    public static void encode(UfMusicRequest m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeUUID(m.identity);b.writeVarInt(m.action);b.writeVarInt(m.value);}
    public static UfMusicRequest decode(FriendlyByteBuf b){return new UfMusicRequest(b.readBlockPos(),b.readUUID(),b.readVarInt(),b.readVarInt());}
    public static void handle(UfMusicRequest m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{if(c.getSender()!=null)UfMusicServer.request(c.getSender(),m);});c.setPacketHandled(true);}
}
