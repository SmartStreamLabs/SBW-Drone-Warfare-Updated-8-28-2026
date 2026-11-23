package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
public final class RubbleShovelItem extends ShovelItem {
    public RubbleShovelItem(){super(Tiers.IRON,1.5f,-3,new Properties().durability(768));}
    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){return c.getLevel().getBlockState(c.getClickedPos()).is(AddonBlocks.RUBBLE.get())?net.minecraft.world.InteractionResult.sidedSuccess(c.getLevel().isClientSide):net.minecraft.world.InteractionResult.PASS;}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> c){c.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new Rs01ItemRenderer();return renderer;}});}
    @Override public void appendHoverText(ItemStack s,net.minecraft.world.level.Level l,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag f){lines.add(net.minecraft.network.chat.Component.translatable("tooltip.sbwdroneconfig.rubble_shovel_rs01"));lines.add(net.minecraft.network.chat.Component.literal("Right-click SK-01 remains to clear them."));}
}
