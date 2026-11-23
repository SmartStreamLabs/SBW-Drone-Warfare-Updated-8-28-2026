package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
public record DwfAction(int menu,int stateId,int action,int slot,int button,int gesture,long revision,String text){
 public static void encode(DwfAction m,FriendlyByteBuf b){b.writeVarInt(m.menu);b.writeVarInt(m.stateId);b.writeVarInt(m.action);b.writeVarInt(m.slot);b.writeVarInt(m.button);b.writeVarInt(m.gesture);b.writeVarLong(m.revision);b.writeUtf(m.text,160);}
 public static DwfAction decode(FriendlyByteBuf b){return new DwfAction(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarLong(),b.readUtf(160));}
 public static void handle(DwfAction m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>ctx){var c=ctx.get();c.enqueueWork(()->{var player=c.getSender();if(player!=null&&player.containerMenu instanceof DwfMenu menu&&menu.containerId==m.menu()&&menu.stillValid(player))menu.request(m);});c.setPacketHandled(true);}
}
