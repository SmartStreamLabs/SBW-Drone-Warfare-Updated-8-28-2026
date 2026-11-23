package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Uses vanilla window/button validation; no custom control protocol or per-animation-frame messages. */
public class GlidePlatformMenu extends AbstractContainerMenu implements MissionMenu,PlatformInventoryState {
    @Override public BlockPos missionPos(){return pos;}
    private final GlidePlatformBlockEntity platform;
    private final BlockPos pos;
    private final ContainerData data;private final ContainerData inventoryState;public ContainerData inventoryState(){return inventoryState;}
    public GlidePlatformMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,buffer.readBlockPos(),null);}
    public GlidePlatformMenu(int id,Inventory inventory,GlidePlatformBlockEntity platform){this(id,inventory,platform.getBlockPos(),platform);}
    private GlidePlatformMenu(int id,Inventory inventory,BlockPos pos,GlidePlatformBlockEntity platform){
        super(AddonMenus.GLIDE_PLATFORM.get(),id);this.pos=pos;this.platform=platform;
        var contents=new SimpleContainer(1){@Override public ItemStack getItem(int slot){return platform==null?super.getItem(slot):platform.drone().copy();}};
        addSlot(new Slot(contents,0,22,49){@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player player){return false;}});
        data=platform==null?new SimpleContainerData(3):new ContainerData(){
            @Override public int get(int index){return switch(index){case 0->platform.phase().ordinal();case 1->platform.displayReason(inventory.player);default->platform.facing().get2DDataValue();};}
            @Override public void set(int i,int v){}
            @Override public int getCount(){return 3;}
        };
        addDataSlots(data);inventoryState=PlatformInventory.status(platform,()->0);addDataSlots(inventoryState);
    }
    public int phase(){return data.get(0);}
    public int reason(){return data.get(1);}
    public net.minecraft.core.Direction facing(){return net.minecraft.core.Direction.from2DDataValue(data.get(2));}
    @Override public boolean stillValid(Player player){return player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=64 && player.level().getBlockState(pos).is(AddonBlocks.GLIDE_PLATFORM.get());}
    @Override public void removed(Player player){super.removed(player);if(platform!=null)platform.viewerClosed(player);}
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int action){
        if(platform==null || !stillValid(player))return false;
        if(action==10){PlatformInventory.open(platform,0,player);return true;}
        switch(action){case 0->platform.requestLaunch(player);case 1->platform.requestReturn(player);case 2->platform.cancel(player);default->{return false;}}
        return true;
    }
}
