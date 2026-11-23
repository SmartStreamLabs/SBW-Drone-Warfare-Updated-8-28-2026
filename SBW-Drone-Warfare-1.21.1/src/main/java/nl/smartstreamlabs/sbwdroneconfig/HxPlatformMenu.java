package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.network.chat.Component;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.SimpleMenuProvider;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;
public class HxPlatformMenu extends AbstractContainerMenu implements PlatformInventoryState {
    final BlockPos pos;final HxPlatformBlockEntity platform;final ContainerData state;
    public HxPlatformMenu(int id,Inventory inv,FriendlyByteBuf b){this(id,inv,b.readBlockPos(),null);}
    public HxPlatformMenu(int id,Inventory inv,HxPlatformBlockEntity p){this(id,inv,p.getBlockPos(),p);}
    private HxPlatformMenu(int id,Inventory inv,BlockPos pos,HxPlatformBlockEntity p){super(AddonMenus.HX_PLATFORM.get(),id);this.pos=pos;platform=p;state=PlatformInventory.status(p,()->0);addDataSlots(state);}
    public ContainerData inventoryState(){return state;}
    public static void open(ServerPlayer p,HxPlatformBlockEntity pad){if(pad.canUse(p))p.openMenu(new SimpleMenuProvider((id,inv,w)->new HxPlatformMenu(id,inv,pad),Component.literal("HX Launch Platform")),b->b.writeBlockPos(pad.getBlockPos()));}
    public boolean stillValid(Player p){return platform==null?p.level().getBlockState(pos).getBlock() instanceof HxPlatformBlock:!platform.isRemoved()&&platform.canUse(p);}
    public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
    public boolean clickMenuButton(Player p,int a){if(platform==null||!stillValid(p))return false;if(a==10)PlatformInventory.open(platform,0,p);else if(a==0)platform.launch(p);else if(a==1)platform.unload(p);else return false;return true;}
}
