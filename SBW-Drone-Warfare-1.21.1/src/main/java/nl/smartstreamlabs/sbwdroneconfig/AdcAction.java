package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;
public record AdcAction(BlockPos root,int action,int slot,String player) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload{
 public static final Type<AdcAction>TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"adc01_action"));public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload>type(){return TYPE;}
 public AdcAction(BlockPos root,int action,int slot){this(root,action,slot,"");}
 public static void encode(AdcAction m,FriendlyByteBuf b){b.writeBlockPos(m.root);b.writeByte(m.action);b.writeByte(m.slot);b.writeUtf(m.player,36);}public static AdcAction decode(FriendlyByteBuf b){return new AdcAction(b.readBlockPos(),b.readUnsignedByte(),b.readUnsignedByte(),b.readUtf(36));}
 public static void apply(AdcAction m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action<=8&&m.slot<=2&&p.serverLevel().hasChunkAt(m.root)&&p.serverLevel().getBlockEntity(m.root)instanceof AdcBlockEntity b){if(m.action>=7)b.whitelist(p,m.player,m.action==8);else b.control(p,m.action,m.slot);}}
 public static void handle(AdcAction m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->apply(m,(net.minecraft.server.level.ServerPlayer)c.player()));}
}
