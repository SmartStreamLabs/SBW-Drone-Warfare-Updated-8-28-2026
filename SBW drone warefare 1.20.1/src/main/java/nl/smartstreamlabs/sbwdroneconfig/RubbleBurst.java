package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
/** One bounded batch per level tick; clients expand it to visual particles locally. */
public record RubbleBurst(ResourceLocation dimension,long seed,List<Sample> samples) {
    public record Sample(BlockPos pos,int state,int kind){}
    public static void encode(RubbleBurst m,FriendlyByteBuf b){b.writeResourceLocation(m.dimension);b.writeLong(m.seed);b.writeVarInt(m.samples.size());for(var s:m.samples){b.writeBlockPos(s.pos);b.writeVarInt(s.state);b.writeByte(s.kind);}}
    public static RubbleBurst decode(FriendlyByteBuf b){var dimension=b.readResourceLocation();long seed=b.readLong();int n=b.readVarInt();if(n<0||n>96)throw new IllegalArgumentException("Rubble burst bound");var list=new ArrayList<Sample>();for(int i=0;i<n;i++)list.add(new Sample(b.readBlockPos(),b.readVarInt(),b.readUnsignedByte()));return new RubbleBurst(dimension,seed,List.copyOf(list));}
    public static void handle(RubbleBurst m,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->RubbleClient.burst(m));c.setPacketHandled(true);}
}
