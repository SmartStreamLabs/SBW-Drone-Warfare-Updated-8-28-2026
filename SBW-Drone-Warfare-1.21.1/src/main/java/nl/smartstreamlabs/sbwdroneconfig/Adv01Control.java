package nl.smartstreamlabs.sbwdroneconfig;
public record Adv01Control(int entity,int action) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
 public static final Type<Adv01Control> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"adv01_control"));
 @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
    public static void encode(Adv01Control m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeByte(m.action);}
    public static Adv01Control decode(net.minecraft.network.FriendlyByteBuf b){return new Adv01Control(b.readVarInt(),b.readUnsignedByte());}
    private static void apply(Adv01Control m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action>=0&&m.action<=3&&p.serverLevel().getEntity(m.entity) instanceof Adv01Entity a&&(p.getVehicle()==a||VatagSessions.controlling(p,a)))a.control(p,m.action);}
    public static void handle(Adv01Control m,net.neoforged.neoforge.network.handling.IPayloadContext c){if(c.player() instanceof net.minecraft.server.level.ServerPlayer p)apply(m,p);}
}
