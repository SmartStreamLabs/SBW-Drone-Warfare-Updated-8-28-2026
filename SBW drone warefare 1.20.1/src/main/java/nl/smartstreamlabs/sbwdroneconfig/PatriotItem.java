package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;import net.minecraft.network.chat.Component;
/** The launcher itself uses SBW's existing vehicle container. Only its missile is a new item. */
public final class PatriotItem extends Item{
 public PatriotItem(){super(new Properties().stacksTo(1));}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.level.Level l,java.util.List<Component> lines,TooltipFlag f){lines.add(Component.translatable("pm01.tooltip"));}
 @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> c){c.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return PatriotItemRenderer.instance();}});}
}
