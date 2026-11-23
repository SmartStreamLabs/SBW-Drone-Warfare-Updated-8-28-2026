package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.inventory.*;import net.minecraft.world.entity.player.*;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.world.item.ItemStack;
public class Dc01Menu extends AbstractContainerMenu implements PlatformInventoryState {
 private final ContainerData[] bayPower=new ContainerData[3];private final ContainerData inventoryState;public ContainerData inventoryState(){return inventoryState;}
 public final int carrierId;private final Dc01Entity carrier;private final ContainerData data;
 public Dc01Menu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,buf.readInt(),null);}
 public Dc01Menu(int id,Inventory inv,Dc01Entity carrier){this(id,inv,carrier.getId(),carrier);}
 private Dc01Menu(int id,Inventory inv,int entity,Dc01Entity carrier){super(AddonMenus.DC01.get(),id);this.carrierId=entity;this.carrier=carrier;data=carrier==null?new SimpleContainerData(12):new ContainerData(){public int get(int i){if(i<9){int slot=i/3,value=carrier.dockId(slot);return switch(i%3){case 0->carrier.dockType(slot);case 1->value&65535;default->value>>>16;};}return i==9?carrier.stopped()?1:0:i==10?carrier.readyMask():carrier.lastFailure;}public void set(int i,int v){}public int getCount(){return 12;}};addDataSlots(data);inventoryState=PlatformInventory.status(carrier,()->0);addDataSlots(inventoryState);bayPower[0]=inventoryState;for(int s=1;s<3;s++){final int bay=s;bayPower[s]=PlatformInventory.status(carrier,()->bay);addDataSlots(bayPower[s]);}}
 public int droneEnergy(int s){return(bayPower[s].get(1)&65535)|((bayPower[s].get(2)&65535)<<16);}public int droneCapacity(int s){return(bayPower[s].get(3)&65535)|((bayPower[s].get(4)&65535)<<16);}
 public int type(int slot){return data.get(slot*3);}public int entity(int slot){return(data.get(slot*3+1)&65535)|((data.get(slot*3+2)&65535)<<16);}public int reason(){return data.get(11);}public boolean stopped(){return data.get(9)!=0;}public boolean ready(int s){return(data.get(10)&1<<s)!=0;}
 @Override public boolean stillValid(Player p){var c=p.level().getEntity(carrierId);return c instanceof Dc01Entity dc&&dc.isAlive()&&!dc.isRemoved()&&(p.level().isClientSide||p.distanceToSqr(dc)<=64||p instanceof net.minecraft.server.level.ServerPlayer sp&&(VatagSessions.controlling(sp,dc)||CarrierFleet.access(sp,dc)));}
 @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
 @Override public boolean clickMenuButton(Player p,int action){if(carrier==null||!stillValid(p)||!(p instanceof net.minecraft.server.level.ServerPlayer sp))return false;carrier.action(sp,action);return true;}
}
