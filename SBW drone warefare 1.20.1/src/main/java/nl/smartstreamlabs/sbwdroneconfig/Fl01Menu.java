package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Uses vanilla window/button validation; no custom control protocol or per-animation-frame messages. */
public class Fl01Menu extends AbstractContainerMenu implements MissionMenu,PlatformInventoryState {
    @Override public BlockPos missionPos(){return pos;}
    private final Fl01PlatformBlockEntity platform;
    private final BlockPos pos;
    private final ContainerData data;private final ContainerData inventoryState;public ContainerData inventoryState(){return inventoryState;}
    public Fl01Menu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,buffer.readBlockPos(),null);}
    public Fl01Menu(int id,Inventory inventory,Fl01PlatformBlockEntity platform){this(id,inventory,platform.getBlockPos(),platform);}
    private Fl01Menu(int id,Inventory inventory,BlockPos pos,Fl01PlatformBlockEntity platform){
        super(AddonMenus.FL_LP.get(),id);this.pos=pos;this.platform=platform;
        var contents=new SimpleContainer(1){@Override public ItemStack getItem(int slot){return platform==null?super.getItem(slot):platform.drone().copy();}};
        addSlot(new Slot(contents,0,22,49){@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player player){return false;}});
        data=platform==null?new SimpleContainerData(4):new ContainerData(){
            @Override public int get(int index){return switch(index){case 0->platform.phase().ordinal();case 1->platform.displayReason(inventory.player);case 2->Math.round(platform.fuel());default->platform.facing().get2DDataValue();};}
            @Override public void set(int i,int v){}
            @Override public int getCount(){return 4;}
        };
        addDataSlots(data);inventoryState=PlatformInventory.status(platform,()->0);addDataSlots(inventoryState);
    }
    public int phase(){return data.get(0);}
    public int reason(){return data.get(1);}
    public int fuel(){return data.get(2);}
    public net.minecraft.core.Direction facing(){return net.minecraft.core.Direction.from2DDataValue(data.get(3));}
    @Override public boolean stillValid(Player player){return player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=64 && player.level().getBlockState(pos).is(AddonBlocks.FL_LP.get());}
    @Override public void removed(Player player){super.removed(player);if(platform!=null){}}
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int action){
        if(platform==null || !stillValid(player))return false;
        if(action==10){PlatformInventory.open(platform,0,player);return true;}
        switch(action){case 0->platform.requestLaunch(player);case 1->platform.requestReturn(player);case 2->platform.cancel(player);case 3->platform.loadInventory(player);case 4->platform.variant(player);default->{return false;}}
        return true;
    }
}
