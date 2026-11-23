package nl.smartstreamlabs.sbwdroneconfig;
public record Adv01Control(int entity,int action) {
    public static void encode(Adv01Control m,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(m.entity);b.writeByte(m.action);}
    public static Adv01Control decode(net.minecraft.network.FriendlyByteBuf b){return new Adv01Control(b.readVarInt(),b.readUnsignedByte());}
    private static void apply(Adv01Control m,net.minecraft.server.level.ServerPlayer p){if(p!=null&&m.action>=0&&m.action<=3&&p.serverLevel().getEntity(m.entity) instanceof Adv01Entity a&&(p.getVehicle()==a||VatagSessions.controlling(p,a)))a.control(p,m.action);}
    public static void handle(Adv01Control m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->apply(m,c.getSender()));c.setPacketHandled(true);}
}
