package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class LucasPlatformScreen extends MissionPlatformScreen<LucasPlatformMenu> {
 public LucasPlatformScreen(LucasPlatformMenu menu,Inventory inventory,Component title){super(menu,inventory,title,"lp01");}
 @Override protected int phase(){return menu.phase();}
 @Override protected int reason(){return menu.reason();}
 @Override protected String reasonKey(){return LucasPlatformBlockEntity.reasonKey(menu.reason());}
 @Override protected Component details(){return Component.translatable("lp01.fuel",menu.fuel());}
}
