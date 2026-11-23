package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** One explosion event with dimension identity (plus channel framing), never one packet per particle. */
public record ExplosionVfxMessage(double x, double y, double z, float radius, int profile,
                                  long start, long seed, boolean suppress, net.minecraft.resources.ResourceLocation dimension,float intensity) implements CustomPacketPayload {
    public static final Type<ExplosionVfxMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"explosion_vfx"));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
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
    public static void handle(ExplosionVfxMessage m, IPayloadContext context) { ExplosionVfxClient.receive(m); }
}
