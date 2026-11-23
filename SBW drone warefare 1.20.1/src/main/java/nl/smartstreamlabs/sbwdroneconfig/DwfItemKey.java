package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;

/** Full Forge item/NBT identity. Never exposes the owned prototype for mutation. */
public final class DwfItemKey {
    private final ItemStack prototype;private final int hash;private final byte[] encoded;private final String label,category;
    public DwfItemKey(ItemStack stack){this(stack,true);}
    private DwfItemKey(ItemStack stack,boolean copy){
        if(stack.isEmpty())throw new IllegalArgumentException("Empty variant");
        prototype=copy?stack.copy():stack;if(copy)prototype.setCount(1);
        hash=31*System.identityHashCode(prototype.getItem())+(prototype.hasTag()?prototype.getTag().hashCode():0);
        if(copy){
            label=prototype.getHoverName().getString();var id=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(prototype.getItem());category=category(prototype,id);
            try(var bytes=new java.io.ByteArrayOutputStream();var out=new java.io.DataOutputStream(bytes)){net.minecraft.nbt.NbtIo.write(prototype.save(new net.minecraft.nbt.CompoundTag()),out);out.flush();encoded=bytes.toByteArray();}
            catch(java.io.IOException failure){throw new IllegalStateException("Cannot encode item variant",failure);}
        }else{encoded=null;label=category="";}
    }
    /** Ephemeral lookup only. Never retain a probe in the store. */
    static DwfItemKey probe(ItemStack stack){return new DwfItemKey(stack,false);}
    public boolean matches(ItemStack stack){return ItemStack.isSameItemSameTags(prototype,stack);}
    public ItemStack stack(int count){if(count<0)throw new IllegalArgumentException();var out=prototype.copy();out.setCount(count);return out;}
    public int maxStackSize(){return prototype.getMaxStackSize();}
    public net.minecraft.world.item.Item item(){return prototype.getItem();}
    public String label(){return label;}public String category(){return category;}
    /** The encoder receives a defensive byte copy; no Minecraft access off-thread. */
    public byte[] encoded(){if(encoded==null)throw new IllegalStateException("Lookup probe cannot be saved");return encoded.clone();}
    int encodedHash(){return java.util.Arrays.hashCode(encoded);}
    boolean encodedEquals(byte[] value){return encoded!=null&&java.util.Arrays.equals(encoded,value);}
    public net.minecraft.nbt.CompoundTag save(){return prototype.save(new net.minecraft.nbt.CompoundTag());}
    @Override public int hashCode(){return hash;}
    @Override public boolean equals(Object other){return other instanceof DwfItemKey k&&hash==k.hash&&ItemStack.isSameItemSameTags(prototype,k.prototype);}
 private static String category(ItemStack stack,net.minecraft.resources.ResourceLocation id){var item=stack.getItem();if(item instanceof net.minecraft.world.item.ArmorItem)return "Equipment";if(item instanceof net.minecraft.world.item.BlockItem)return id!=null&&id.getNamespace().equals(SbwDroneRangeConfig.MOD_ID)?"Installations":"Materials";String path=id==null?"":id.getPath();if(path.equals("container")||id!=null&&net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.containsKey(id))return "Vehicles";if(path.contains("missile")||path.contains("grenade")||path.contains("payload")||path.contains("launcher"))return "Payloads and weapons";if(item instanceof net.minecraft.world.item.DiggerItem)return "Tools";return id!=null&&id.getNamespace().equals("minecraft")?"Materials":"Components";}
}
