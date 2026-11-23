package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
/** A short input lease, not a client-provided progress value. */
public record RubbleUse(BlockPos pos,boolean held) {
    public static void encode(RubbleUse m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeBoolean(m.held);}
    public static RubbleUse decode(FriendlyByteBuf b){return new RubbleUse(b.readBlockPos(),b.readBoolean());}
    public static void handle(RubbleUse m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)RubbleTools.heartbeat(c.getSender(),m);});c.setPacketHandled(true);}
}
