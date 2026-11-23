package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record DvtRequest(BlockPos pos,int action,UUID drone,String player)implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<DvtRequest> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"dvtrequest"));@Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}

 public static void encode(DvtRequest m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeVarInt(m.action);b.writeBoolean(m.drone!=null);if(m.drone!=null)b.writeUUID(m.drone);b.writeUtf(m.player,64);}
 public static DvtRequest decode(FriendlyByteBuf b){return new DvtRequest(b.readBlockPos(),b.readVarInt(),b.readBoolean()?b.readUUID():null,b.readUtf(64));}
 public static void handle(DvtRequest m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)DvtServer.request(p,m);});}
}