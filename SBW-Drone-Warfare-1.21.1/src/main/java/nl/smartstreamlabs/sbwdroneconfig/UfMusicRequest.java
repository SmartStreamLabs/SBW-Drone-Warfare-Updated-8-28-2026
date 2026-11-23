package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record UfMusicRequest(BlockPos pos,UUID identity,int action,int value) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<UfMusicRequest> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","uf_music_request"));
    @Override public Type<UfMusicRequest> type(){return TYPE;}
    public static void encode(UfMusicRequest m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeUUID(m.identity);b.writeVarInt(m.action);b.writeVarInt(m.value);}
    public static UfMusicRequest decode(FriendlyByteBuf b){return new UfMusicRequest(b.readBlockPos(),b.readUUID(),b.readVarInt(),b.readVarInt());}
    public static void handle(UfMusicRequest m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)UfMusicServer.request(p,m);});}
}
