package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class GeraniumPlatformScreen extends MissionPlatformScreen<GeraniumPlatformMenu> {
 public GeraniumPlatformScreen(GeraniumPlatformMenu menu,Inventory inventory,Component title){super(menu,inventory,title,"lp02");}
 @Override protected int phase(){return menu.phase();}
 @Override protected int reason(){return menu.reason();}
 @Override protected String reasonKey(){return GeraniumPlatformBlockEntity.reasonKey(menu.reason());}
 @Override protected Component details(){return Component.translatable("lp02.fuel",menu.fuel());}
}
