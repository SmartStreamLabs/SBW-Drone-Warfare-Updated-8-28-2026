package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
/** Direct view of the native vehicle handler, shared by all viewers and its saved data. */
public record SupplyCargoInventory(SupplyRoverEntity rover) implements Container {
 public int getContainerSize(){return rover.getContainerSize();}
 public boolean isEmpty(){for(int i=0;i<getContainerSize();i++)if(!getItem(i).isEmpty())return false;return true;}
 public ItemStack getItem(int slot){return rover.getItem(slot);}
 public ItemStack removeItem(int slot,int amount){return rover.removeItem(slot,amount);}
 public ItemStack removeItemNoUpdate(int slot){return rover.removeItem(slot,Integer.MAX_VALUE);}
 public void setItem(int slot,ItemStack stack){rover.setItem(slot,stack);}
 public void setChanged(){rover.setChanged();}
 public boolean stillValid(Player p){return p.isAlive()&&rover.isAlive()&&!rover.isRemoved()&&p.level()==rover.level()&&p.distanceToSqr(rover)<=64&&(!(p instanceof net.minecraft.server.level.ServerPlayer sp)||rover.permitted(sp));}
 public boolean canPlaceItem(int slot,ItemStack stack){return rover.canPlaceItem(slot,stack);}
 public void clearContent(){for(int i=0;i<getContainerSize();i++)setItem(i,ItemStack.EMPTY);}
}
