package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;import java.util.UUID;
public record DefenseAction(BlockPos root,int action,int slot,UUID value) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload{
 public static void encode(DefenseAction m,FriendlyByteBuf b){b.writeBlockPos(m.root);b.writeByte(m.action);b.writeByte(m.slot);b.writeUUID(m.value);}public static DefenseAction decode(FriendlyByteBuf b){return new DefenseAction(b.readBlockPos(),b.readUnsignedByte(),b.readUnsignedByte(),b.readUUID());}
 public static void apply(DefenseAction m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action<=13&&p.serverLevel().hasChunkAt(m.root)&&p.serverLevel().getBlockEntity(m.root)instanceof DefenseBlockEntity b)b.control(p,m.action,m.slot,m.value);}
 public static final Type<DefenseAction> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"defense_action"));public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}public static void handle(DefenseAction m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->apply(m,(net.minecraft.server.level.ServerPlayer)c.player()));}
}
