package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** Bulk-aware capability. Inventory inspection exposes the complete count (within
 * this legacy int API); extraction and inserted stacks obey native stack limits. */
public final class DwfItemHandler implements IItemHandler {
    private final DwfBulkStore<DwfItemKey> storage;
    private final java.util.function.BooleanSupplier insert,extract,ready;
    private final Runnable changed;
    private final DwfVariantPool pool;
    public DwfItemHandler(DwfBulkStore<DwfItemKey> storage,java.util.function.BooleanSupplier insert,java.util.function.BooleanSupplier extract,java.util.function.BooleanSupplier ready,Runnable changed){this(storage,insert,extract,ready,changed,new DwfVariantPool());}
    public DwfItemHandler(DwfBulkStore<DwfItemKey> storage,java.util.function.BooleanSupplier insert,java.util.function.BooleanSupplier extract,java.util.function.BooleanSupplier ready,Runnable changed,DwfVariantPool pool){this.storage=storage;this.insert=insert;this.extract=extract;this.ready=ready;this.changed=changed;this.pool=pool;}
    @Override public int getSlots(){return ready.getAsBoolean()?storage.allocatedSlots()+1:0;}
    private boolean slot(int slot){return slot>=0&&slot<getSlots();}
    @Override public ItemStack getStackInSlot(int slot){if(!slot(slot))return ItemStack.EMPTY;var key=storage.keyAt(slot);return key==null?ItemStack.EMPTY:key.stack((int)Math.min(Integer.MAX_VALUE,storage.count(key)));}
    @Override public int getSlotLimit(int slot){return slot(slot)?(int)Math.min(Integer.MAX_VALUE,storage.capacity()):0;}
    @Override public boolean isItemValid(int slot,ItemStack stack){if(!slot(slot)||stack.isEmpty()||!insert.getAsBoolean())return false;var key=storage.keyAt(slot);return key==null||key.matches(stack);}
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
        if(!isItemValid(slot,stack))return stack;
        var probe=DwfItemKey.probe(stack);var key=storage.canonical(probe);int request=Math.min(stack.getCount(),stack.getMaxStackSize());
        long accepted=storage.insert(key==null?probe:key,request,true);
        if(accepted>0&&!simulate)storage.insert(key==null?pool.intern(stack):key,accepted,false);
        if(accepted==0)return stack;if(!simulate)changed.run();var remainder=stack.copy();remainder.shrink((int)accepted);return remainder;
    }
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
        if(!slot(slot)||amount<=0||!extract.getAsBoolean())return ItemStack.EMPTY;var key=storage.keyAt(slot);if(key==null)return ItemStack.EMPTY;
        int request=Math.min(amount,key.maxStackSize());long accepted=storage.extract(key,request,simulate);
        if(accepted==0)return ItemStack.EMPTY;if(!simulate)changed.run();return key.stack((int)accepted);
    }
}
