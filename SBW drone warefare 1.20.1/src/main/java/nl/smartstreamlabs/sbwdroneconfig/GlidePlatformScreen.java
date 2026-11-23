package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class GlidePlatformScreen extends MissionPlatformScreen<GlidePlatformMenu> {
 public GlidePlatformScreen(GlidePlatformMenu menu,Inventory inventory,Component title){super(menu,inventory,title,"lp03");}
 @Override protected int phase(){return menu.phase();}
 @Override protected int reason(){return menu.reason();}
 @Override protected String reasonKey(){return GlidePlatformBlockEntity.reasonKey(menu.reason());}
 @Override protected Component details(){return Component.translatable("lp03.control");}
}
