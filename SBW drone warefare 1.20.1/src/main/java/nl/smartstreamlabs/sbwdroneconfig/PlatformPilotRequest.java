package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
/** A client may only dismount itself from its current, supported vehicle. */
public record PlatformPilotRequest(int entity) {
 public static boolean supported(Entity e){return e instanceof VatagEntity||e instanceof Mpl01Entity||e instanceof Adv01Entity||e instanceof Mj03Entity||e instanceof MedicalTrailerEntity;}
 public static void encode(PlatformPilotRequest m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);}
 public static PlatformPilotRequest decode(net.minecraft.network.FriendlyByteBuf b){return new PlatformPilotRequest(b.readVarInt());}
 public static void apply(PlatformPilotRequest m,ServerPlayer p){PlatformMonitor.request(p,m.entity==-1);}
 public static void handle(PlatformPilotRequest m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
