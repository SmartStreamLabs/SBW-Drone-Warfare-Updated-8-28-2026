package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record DwfAction(int menu,int stateId,int action,int slot,int button,int gesture,long revision,String text) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload{
 public static final Type<DwfAction> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","dwfaction"));
 @Override public Type<DwfAction>type(){return TYPE;}
 public static void encode(DwfAction m,FriendlyByteBuf b){b.writeVarInt(m.menu);b.writeVarInt(m.stateId);b.writeVarInt(m.action);b.writeVarInt(m.slot);b.writeVarInt(m.button);b.writeVarInt(m.gesture);b.writeVarLong(m.revision);b.writeUtf(m.text,160);}
 public static DwfAction decode(FriendlyByteBuf b){return new DwfAction(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarLong(),b.readUtf(160));}
 public static void handle(DwfAction m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->{var player=c.player();if(player.containerMenu instanceof DwfMenu menu&&menu.containerId==m.menu()&&menu.stillValid(player))menu.request(m);});}
}
