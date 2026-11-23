package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
public record MonitorLinkSoundPacket(UUID id) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<MonitorLinkSoundPacket> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"monitor_link_sound"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static void encode(MonitorLinkSoundPacket p,FriendlyByteBuf b){b.writeUUID(p.id);}
 public static MonitorLinkSoundPacket decode(FriendlyByteBuf b){return new MonitorLinkSoundPacket(b.readUUID());}
 public static void handle(MonitorLinkSoundPacket p,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->MonitorLinkSoundClient.receive(p));}
}
