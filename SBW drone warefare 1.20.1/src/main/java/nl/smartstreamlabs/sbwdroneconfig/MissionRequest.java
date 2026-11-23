package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record MissionRequest(int window,BlockPos pos,boolean enabled,boolean cancel,String x,String y,String z,String cruise) {
    public static void encode(MissionRequest m,FriendlyByteBuf b){b.writeVarInt(m.window);b.writeBlockPos(m.pos);b.writeBoolean(m.enabled);b.writeBoolean(m.cancel);b.writeUtf(m.x,32);b.writeUtf(m.y,32);b.writeUtf(m.z,32);b.writeUtf(m.cruise,32);}
    public static MissionRequest decode(FriendlyByteBuf b){return new MissionRequest(b.readVarInt(),b.readBlockPos(),b.readBoolean(),b.readBoolean(),b.readUtf(32),b.readUtf(32),b.readUtf(32),b.readUtf(32));}
    public static void apply(MissionRequest m,ServerPlayer player){
        if(player==null||player.containerMenu.containerId!=m.window||!(player.containerMenu instanceof MissionMenu menu)||!menu.missionPos().equals(m.pos)||!player.containerMenu.stillValid(player))return;
        var p=menu.missionPlatform(player);if(p==null)return;
        if(m.cancel){MissionManager.get(player.server).cancel(p.mission().last,player);p.missionRefresh();}
        else p.missionEdit(player,m.enabled,m.x,m.y,m.z,m.cruise);
    }
    public static void handle(MissionRequest m,Supplier<NetworkEvent.Context> supplier){var ctx=supplier.get();ctx.enqueueWork(()->apply(m,ctx.getSender()));ctx.setPacketHandled(true);}
}
