package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
public record RubbleReply(BlockPos centre,int work,int required,int units,int cleared,int radius,int mode) {
    public static void encode(RubbleReply m,FriendlyByteBuf b){b.writeBlockPos(m.centre);b.writeVarInt(m.work);b.writeVarInt(m.required);b.writeVarInt(m.units);b.writeVarInt(m.cleared);b.writeVarInt(m.radius);b.writeVarInt(m.mode);}
    public static RubbleReply decode(FriendlyByteBuf b){return new RubbleReply(b.readBlockPos(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
    public static void handle(RubbleReply m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->RubbleClient.receive(m));c.setPacketHandled(true);}
}
