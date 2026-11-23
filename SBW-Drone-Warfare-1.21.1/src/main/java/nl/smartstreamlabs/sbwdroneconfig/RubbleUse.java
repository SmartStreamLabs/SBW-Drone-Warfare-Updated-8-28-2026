package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
/** A short input lease, not a client-provided progress value. */
public record RubbleUse(BlockPos pos,boolean held) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static void encode(RubbleUse m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeBoolean(m.held);}
    public static RubbleUse decode(FriendlyByteBuf b){return new RubbleUse(b.readBlockPos(),b.readBoolean());}
    public static final Type<RubbleUse> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","rubble_use"));
    public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void handle(RubbleUse m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->RubbleTools.heartbeat((net.minecraft.server.level.ServerPlayer)c.player(),m));}
}
