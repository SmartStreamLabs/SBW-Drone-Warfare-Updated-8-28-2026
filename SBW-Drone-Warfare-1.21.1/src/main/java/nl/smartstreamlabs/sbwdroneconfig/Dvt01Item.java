package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;import net.minecraft.world.level.block.Block;
public final class Dvt01Item extends BlockItem {
 public Dvt01Item(Block b,Properties p){super(b,p);}
 @Override public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer){consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new Dvt01ItemRenderer();return renderer;}});}
 @Override public void appendHoverText(ItemStack s,TooltipContext l,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flags){super.appendHoverText(s,l,lines,flags);lines.add(net.minecraft.network.chat.Component.translatable("dvt01.description"));}
}
