package nl.smartstreamlabs.sbwdroneconfig;
public record VatagState(int entity,java.util.UUID token,boolean active,float yaw,float pitch){
 public VatagState(int entity,java.util.UUID token,boolean active){this(entity,token,active,0,0);}
 public static void encode(VatagState m,net.minecraft.network.FriendlyByteBuf b){b.writeInt(m.entity);b.writeUUID(m.token);b.writeBoolean(m.active);b.writeFloat(m.yaw);b.writeFloat(m.pitch);}
 public static VatagState decode(net.minecraft.network.FriendlyByteBuf b){return new VatagState(b.readInt(),b.readUUID(),b.readBoolean(),b.readFloat(),b.readFloat());}
 public static void handle(VatagState m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->VatagClient.state(m)));c.setPacketHandled(true);}
}
