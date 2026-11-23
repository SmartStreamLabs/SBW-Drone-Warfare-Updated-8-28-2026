package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
/** A client may only dismount itself from its current, supported vehicle. */
public record PlatformPilotRequest(int entity) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<PlatformPilotRequest> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"platform_pilot"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 public static boolean supported(Entity e){return e instanceof VatagEntity||e instanceof Mpl01Entity||e instanceof Adv01Entity||e instanceof Mj03Entity||e instanceof MedicalTrailerEntity;}
 public static void encode(PlatformPilotRequest m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);}
 public static PlatformPilotRequest decode(net.minecraft.network.FriendlyByteBuf b){return new PlatformPilotRequest(b.readVarInt());}
 public static void apply(PlatformPilotRequest m,ServerPlayer p){PlatformMonitor.request(p,m.entity==-1);}
 public static void handle(PlatformPilotRequest m,net.neoforged.neoforge.network.handling.IPayloadContext c){c.enqueueWork(()->apply(m,(ServerPlayer)c.player()));}
}
