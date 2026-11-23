package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.items.IItemHandler;import net.minecraft.world.item.ItemStack;
/** Physical transfer interface. The full bulk count is separate from physical
 * stack sizes; importers can never extract an over-sized Minecraft stack. */
public final class DwfPortHandler implements IItemHandler{
 private final DwfBlockEntity port;private final int chest;private DwfMachine owner;private IItemHandler delegate;
 public DwfPortHandler(DwfBlockEntity port,int chest){this.port=port;this.chest=chest;}
 private IItemHandler target(){var level=port.getLevel();if(level==null||port.isRemoved()||!level.hasChunkAt(DwfGeometry.root(port.getBlockPos(),port.facing(),port.getBlockState().getValue(DwfBlock.PART)))){owner=null;delegate=null;return null;}var root=port.root();var machine=root==null?null:root.machine();if(machine==null||!machine.ready()){owner=null;delegate=null;return null;}if(owner!=machine){owner=machine;delegate=machine.storage(chest).handler(()->chest==0?DwfConfig.INPUT_INSERT.get():DwfConfig.OUTPUT_INSERT.get(),()->chest==0?DwfConfig.INPUT_EXTRACT.get():DwfConfig.OUTPUT_EXTRACT.get(),machine::ready);}return delegate;}
 public int getSlots(){var d=target();return d==null?1:d.getSlots();}public ItemStack getStackInSlot(int slot){var d=target();return d==null?ItemStack.EMPTY:d.getStackInSlot(slot);}public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){var d=target();return d==null?stack:d.insertItem(slot,stack,simulate);}public ItemStack extractItem(int slot,int amount,boolean simulate){var d=target();return d==null?ItemStack.EMPTY:d.extractItem(slot,amount,simulate);}public int getSlotLimit(int slot){var d=target();return d==null?64:d.getSlotLimit(slot);}public boolean isItemValid(int slot,ItemStack stack){var d=target();return d!=null&&d.isItemValid(slot,stack);}
}
