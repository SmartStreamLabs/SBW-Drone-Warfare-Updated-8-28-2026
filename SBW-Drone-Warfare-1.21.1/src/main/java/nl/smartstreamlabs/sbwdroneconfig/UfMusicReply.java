package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record UfMusicReply(BlockPos pos,String dimension,UUID identity,UUID session,boolean show,boolean removed,boolean edit,boolean operator,int state,long position,long duration,int volume,int range,boolean repeat,String title,String notice) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<UfMusicReply> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","uf_music_reply"));
    @Override public Type<UfMusicReply> type(){return TYPE;}
    public static void encode(UfMusicReply m,FriendlyByteBuf b){b.writeBlockPos(m.pos);b.writeUtf(m.dimension,128);b.writeUUID(m.identity);b.writeUUID(m.session);b.writeBoolean(m.show);b.writeBoolean(m.removed);b.writeBoolean(m.edit);b.writeBoolean(m.operator);b.writeVarInt(m.state);b.writeVarLong(m.position);b.writeVarLong(m.duration);b.writeVarInt(m.volume);b.writeVarInt(m.range);b.writeBoolean(m.repeat);b.writeUtf(m.title,160);b.writeUtf(m.notice,80);}
    public static UfMusicReply decode(FriendlyByteBuf b){return new UfMusicReply(b.readBlockPos(),b.readUtf(128),b.readUUID(),b.readUUID(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readUtf(160),b.readUtf(80));}
    public static void handle(UfMusicReply m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->UfMusicClient.receive(m));}
}
