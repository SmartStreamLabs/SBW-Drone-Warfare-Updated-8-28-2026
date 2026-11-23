package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Every completed click writes through to the loaded cargo; there is no second item owner. */
public class PlatformInventoryMenu extends AbstractContainerMenu {
    final PlatformInventory.Editor editor;
    private final DroneInventoryContainer bag;
    private final int droneSlots;
    private final ContainerData power;
    private boolean flushing,ready;
    public final boolean hasPayload;
    public PlatformInventoryMenu(int id,Inventory inv,FriendlyByteBuf b){this(id,inv,null,b.readBoolean());}
    PlatformInventoryMenu(int id,Inventory inv,PlatformInventory.Editor e){this(id,inv,e,e.payload());}
    private PlatformInventoryMenu(int id,Inventory inv,PlatformInventory.Editor e,boolean payload){
        super(AddonMenus.PLATFORM_INVENTORY.get(),id);editor=e;hasPayload=payload;
        bag=e==null?null:new DroneInventoryContainer(e.drone){@Override public void setChanged(){super.setChanged();if(ready)flush(false);}};
        Container items=bag==null?new SimpleContainer(DroneInventoryContainer.SIZE):bag;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(items,col+row*9,47+col*18,44+row*18){@Override public boolean mayPlace(ItemStack s){return editable()&&container.canPlaceItem(getContainerSlot(),s);}@Override public boolean mayPickup(Player p){return editable();}});
        if(payload){Container mount=e==null?new SimpleContainer(1):new SimpleContainer(1){
            @Override public ItemStack getItem(int s){return SbwCompat.getDroneCurrentItem(e.drone);}
            @Override public void setItem(int s,ItemStack stack){((CubedFpvDroneEntity)e.drone).platformPayload(stack);setChanged();}
            @Override public ItemStack removeItem(int s,int n){var stack=getItem(s);if(stack.isEmpty())return ItemStack.EMPTY;var out=stack.copyWithCount(1);setItem(s,ItemStack.EMPTY);return out;}
            @Override public ItemStack removeItemNoUpdate(int s){return removeItem(s,1);}
            @Override public boolean canPlaceItem(int s,ItemStack stack){return CubedFpvDroneEntity.platformPayloadAllowed(stack);}
            @Override public void setChanged(){if(ready)flush(false);}
        };addSlot(new Slot(mount,0,47,104){@Override public int getMaxStackSize(){return 1;}@Override public boolean mayPlace(ItemStack s){return editable()&&container.canPlaceItem(0,s);}@Override public boolean mayPickup(Player p){return editable();}});}
        droneSlots=slots.size();
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+col+row*9,47+col*18,139+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,47+col*18,197));
        power=e==null?new SimpleContainerData(6):new ContainerData(){public int get(int i){int v=i<4&&e.drone instanceof Fl01Entity?0:i<2?DroneBatterySystem.currentEnergy(e.drone):i<4?e.drone.getPersistentData().getInt("sbwdroneconfigMaxEnergy"):fuel();return i%2==0?v&65535:v>>>16;}public void set(int i,int v){}public int getCount(){return 6;}};
        addDataSlots(power);ready=true;if(e!=null)flush(true);
    }
    public int energy(){return (power.get(0)&65535)|((power.get(1)&65535)<<16);}
    public int capacity(){return (power.get(2)&65535)|((power.get(3)&65535)<<16);}
    public int shownFuel(){return (power.get(4)&65535)|((power.get(5)&65535)<<16);}
    private int fuel(){return editor.drone instanceof LucasDroneEntity d?Math.round(d.getLucasFuel()):editor.drone instanceof Geranium2Entity d?Math.round(d.getGeraniumFuel()):editor.drone instanceof Fl01Entity d?d.fuel():-1;}
    private boolean editable(){return editor==null||editor.valid();}
    @Override public boolean stillValid(Player p){return editor==null||editor.player==p&&editor.valid();}
    private void flush(boolean service){if(flushing||editor==null||!editor.valid())return;flushing=true;try{bag.saveToDrone();if(service)DroneBatterySystem.prepareOnPlatform(editor.drone,bag);editor.save();}finally{flushing=false;}}
    @Override public void clicked(int slot,int button,ClickType type,Player p){if(!stillValid(p)){p.closeContainer();return;}try{super.clicked(slot,button,type,p);}finally{flush(true);}}
    @Override public void broadcastChanges(){if(editor!=null&&!editor.valid()){editor.player.closeContainer();return;}super.broadcastChanges();}
    @Override public ItemStack quickMoveStack(Player p,int index){if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;var s=slots.get(index);if(!s.hasItem()||!s.mayPickup(p))return ItemStack.EMPTY;var stack=s.getItem();var copy=stack.copy();if(index<droneSlots){if(!moveItemStackTo(stack,droneSlots,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,droneSlots,false))return ItemStack.EMPTY;if(stack.isEmpty())s.setByPlayer(ItemStack.EMPTY);else s.setChanged();flush(true);return copy;}
    @Override public boolean clickMenuButton(Player p,int action){if(editor==null||!stillValid(p))return false;if(action==0){flush(true);p.closeContainer();editor.back();return true;}if(action==1){refuel();flush(true);broadcastChanges();return true;}return false;}
    private void refuel(){
        int old=fuel();if(old<0)return;int max=editor.drone instanceof Fl01Entity?Fl01Config.FUEL.get():AddonConfig.lucasMaxFuel();if(old>=max)return;
        for(int s=0;s<bag.getContainerSize();s++){var can=bag.getItem(s);if(!can.is(AddonItems.GASOLINE_JERRYCAN.get()))continue;
            int next=editor.drone instanceof Fl01Entity?max:Math.min(max,old+AddonConfig.gasolineJerrycanFuelAmount());
            // Reserve space for the empty can before consuming either the item or fuel.
            if(can.getCount()>1){editor.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Use a single gasoline jerrycan in a drone slot to refuel."),true);return;}
            if(editor.drone instanceof LucasDroneEntity d)d.setLucasFuel(next);else if(editor.drone instanceof Geranium2Entity d)d.setGeraniumFuel(next);else if(editor.drone instanceof Fl01Entity d)d.platformFuel(next);
            if(!editor.player.getAbilities().instabuild)bag.setItem(s,new ItemStack(AddonItems.EMPTY_JERRYCAN.get()));return;
        }
        editor.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Place a Gasoline Jerrycan in the existing drone inventory, then press Refuel / charge."),true);
    }
    @Override public void removed(Player p){flush(true);if(editor!=null){editor.refreshHost();editor.closed=true;PlatformInventory.closed(editor.drone);}super.removed(p);}
}
