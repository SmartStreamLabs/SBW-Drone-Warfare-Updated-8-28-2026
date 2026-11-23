package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface MissionPlatform {
    java.util.UUID loadedId();
    void requestManualLaunch(Player player);
    MissionDraft mission();
    String missionKind();
    ItemStack drone();
    Direction facing();
    boolean canUse(Player player);
    boolean missionEditable();
    void missionSync();
    default BlockEntity block(){return (BlockEntity)this;}
    default Vec3 missionMount(){return switch(missionKind()){
        case "lucas"->LucasPlatformLayout.mount(block().getBlockPos(),facing());
        case "geranium"->GeraniumPlatformLayout.mount(block().getBlockPos(),facing());
        default->GlidePlatformLayout.mount(block().getBlockPos(),facing());};}
    default boolean missionValid(Player player){
        if(!mission().enabled)return true;
        String error=MissionManager.validate(this,mission().x,mission().y,mission().z,mission().cruise);
        if(!error.isEmpty()){mission().valid=false;mission().message=error;missionSync();return false;}
        return mission().valid;
    }
    default void missionEdit(Player player,boolean enabled,String x,String y,String z,String cruise){
        if(!canUse(player)||!missionEditable())return;
        var d=mission();d.enabled=enabled;d.x=x;d.y=y;d.z=z;d.cruise=cruise;
        d.message=MissionManager.validate(this,x,y,z,cruise);d.valid=d.message.isEmpty();
        if(d.valid){d.target=new Vec3(Double.parseDouble(x),Double.parseDouble(y),Double.parseDouble(z));d.altitude=cruise.isBlank()?d.target.y:Double.parseDouble(cruise);}
        if(!d.message.isEmpty())player.displayClientMessage(net.minecraft.network.chat.Component.literal(d.message),false);
        missionSync();
    }
    default void missionRefresh(){
        if(!(block().getLevel() instanceof ServerLevel l)||mission().last==null)return;
        var r=MissionManager.get(l.getServer()).find(mission().last);
        if(r==null)return;
        String status=r.state,reason=r.reason;
        if(!status.equals(mission().status)||!reason.equals(mission().result)) {mission().status=status;mission().result=reason;missionSync();}
    }
}
