package nl.smartstreamlabs.sbwdroneconfig;
public final class Sk01DecorationItem extends net.minecraft.world.item.BlockItem {
 public Sk01DecorationItem(net.minecraft.world.level.block.Block block,Properties properties){super(block,properties);}
 @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new Sk01DecorationItemRenderer();return renderer;}});}
}
