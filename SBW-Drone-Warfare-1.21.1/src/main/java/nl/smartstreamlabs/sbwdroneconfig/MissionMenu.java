package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
public interface MissionMenu {
    BlockPos missionPos();
    default MissionPlatform missionPlatform(Player player){return player.level().getBlockEntity(missionPos()) instanceof MissionPlatform p?p:null;}
}
