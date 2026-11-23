package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;import java.util.UUID;
public record DefenseAction(BlockPos root,int action,int slot,UUID value){
 public static void encode(DefenseAction m,FriendlyByteBuf b){b.writeBlockPos(m.root);b.writeByte(m.action);b.writeByte(m.slot);b.writeUUID(m.value);}public static DefenseAction decode(FriendlyByteBuf b){return new DefenseAction(b.readBlockPos(),b.readUnsignedByte(),b.readUnsignedByte(),b.readUUID());}
 public static void apply(DefenseAction m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action<=13&&p.serverLevel().hasChunkAt(m.root)&&p.serverLevel().getBlockEntity(m.root)instanceof DefenseBlockEntity b)b.control(p,m.action,m.slot,m.value);}
 public static void handle(DefenseAction m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>s){var c=s.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
