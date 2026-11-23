package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
public record MonitorLinkSoundPacket(UUID id) {
 public static void encode(MonitorLinkSoundPacket p,FriendlyByteBuf b){b.writeUUID(p.id);}
 public static MonitorLinkSoundPacket decode(FriendlyByteBuf b){return new MonitorLinkSoundPacket(b.readUUID());}
 public static void handle(MonitorLinkSoundPacket p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->MonitorLinkSoundClient.receive(p));c.setPacketHandled(true);}
}
