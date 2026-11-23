package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;
/** Read-only presentation snapshot, scoped to one entity and dimension. */
public record DroneOsdTelemetry(UUID drone,int entity,String dimension,String mode,String payload,String link,
        double originX,double originY,double originZ,int energy,int capacity,float fuel,
        boolean lowEnergy,boolean lowFuel,boolean jammed,double range,int target,float signal,java.util.List<Integer> markers) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<DroneOsdTelemetry> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"drone_osd"));
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public DroneOsdTelemetry {markers=java.util.List.copyOf(markers);if(markers.size()>16)throw new IllegalArgumentException("marker bound");}
    public static void encode(DroneOsdTelemetry m,FriendlyByteBuf b){
        b.writeUUID(m.drone);b.writeInt(m.entity);b.writeUtf(m.dimension,128);b.writeUtf(m.mode,48);b.writeUtf(m.payload,128);b.writeUtf(m.link,24);
        b.writeDouble(m.originX);b.writeDouble(m.originY);b.writeDouble(m.originZ);b.writeInt(m.energy);b.writeInt(m.capacity);b.writeFloat(m.fuel);
        b.writeBoolean(m.lowEnergy);b.writeBoolean(m.lowFuel);b.writeBoolean(m.jammed);b.writeDouble(m.range);b.writeInt(m.target);b.writeFloat(m.signal);b.writeVarInt(m.markers.size());for(int id:m.markers)b.writeInt(id);
    }
    public static DroneOsdTelemetry decode(FriendlyByteBuf b){return new DroneOsdTelemetry(b.readUUID(),b.readInt(),b.readUtf(128),b.readUtf(48),b.readUtf(128),b.readUtf(24),b.readDouble(),b.readDouble(),b.readDouble(),b.readInt(),b.readInt(),b.readFloat(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readDouble(),b.readInt(),b.readFloat(),readMarkers(b));}
    private static java.util.List<Integer> readMarkers(FriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>16)throw new IllegalArgumentException("marker bound");var result=new java.util.ArrayList<Integer>(n);for(int i=0;i<n;i++)result.add(b.readInt());return result;}
    public static void handle(DroneOsdTelemetry m,net.neoforged.neoforge.network.handling.IPayloadContext context){context.enqueueWork(()->DroneOsdClient.receive(m));}
}
