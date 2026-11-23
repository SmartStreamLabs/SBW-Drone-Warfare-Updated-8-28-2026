package nl.smartstreamlabs.sbwdroneconfig;

public record Rd01State(int entity,java.util.UUID token,boolean active) {
    public static void encode(Rd01State m,net.minecraft.network.FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.token);b.writeBoolean(m.active);}
    public static Rd01State decode(net.minecraft.network.FriendlyByteBuf b){return new Rd01State(b.readInt(),b.readUUID(),b.readBoolean());}
    public static void handle(Rd01State m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->Rd01Client.state(m)));c.setPacketHandled(true);}
}
