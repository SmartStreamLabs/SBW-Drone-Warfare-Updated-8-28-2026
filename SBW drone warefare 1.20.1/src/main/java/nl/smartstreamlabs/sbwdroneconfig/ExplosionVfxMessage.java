package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** One explosion event with dimension identity (plus channel framing), never one packet per particle. */
public record ExplosionVfxMessage(double x, double y, double z, float radius, int profile,
                                  long start, long seed, boolean suppress, net.minecraft.resources.ResourceLocation dimension,float intensity)  {
    
    public ExplosionVfxMessage(double x,double y,double z,float radius,int profile,long start,long seed,boolean suppress,net.minecraft.resources.ResourceLocation dimension){this(x,y,z,radius,profile,start,seed,suppress,dimension,1);}
    public ExplosionVfxMessage {if(!Float.isFinite(intensity)||intensity<0||intensity>6)throw new IllegalArgumentException("Explosion visual intensity");}
    public static void encode(ExplosionVfxMessage m, FriendlyByteBuf b) {
        b.writeDouble(m.x); b.writeDouble(m.y); b.writeDouble(m.z); b.writeFloat(m.radius);
        b.writeByte(m.profile); b.writeLong(m.start); b.writeLong(m.seed); b.writeBoolean(m.suppress); b.writeResourceLocation(m.dimension); b.writeFloat(m.intensity);
    }
    public static ExplosionVfxMessage decode(FriendlyByteBuf b) {
        return new ExplosionVfxMessage(b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),
                b.readUnsignedByte(),b.readLong(),b.readLong(),b.readBoolean(),b.readResourceLocation(),b.readFloat());
    }
    public static void handle(ExplosionVfxMessage m, Supplier<NetworkEvent.Context> context) { ExplosionVfxClient.receive(m); context.get().setPacketHandled(true); }
}
