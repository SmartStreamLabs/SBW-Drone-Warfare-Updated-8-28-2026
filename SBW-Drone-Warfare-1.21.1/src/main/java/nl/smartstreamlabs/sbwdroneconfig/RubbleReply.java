package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
public record RubbleReply(BlockPos centre,int work,int required,int units,int cleared,int radius,int mode) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static void encode(RubbleReply m,FriendlyByteBuf b){b.writeBlockPos(m.centre);b.writeVarInt(m.work);b.writeVarInt(m.required);b.writeVarInt(m.units);b.writeVarInt(m.cleared);b.writeVarInt(m.radius);b.writeVarInt(m.mode);}
    public static RubbleReply decode(FriendlyByteBuf b){return new RubbleReply(b.readBlockPos(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
    public static final Type<RubbleReply> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","rubble_reply"));
    public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void handle(RubbleReply m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->RubbleClient.receive(m));}
}
