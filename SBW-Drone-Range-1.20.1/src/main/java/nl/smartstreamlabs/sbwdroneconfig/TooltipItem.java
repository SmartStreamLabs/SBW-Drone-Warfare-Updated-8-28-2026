package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TooltipItem extends Item {
    private final String[] tooltipKeys;

    public TooltipItem(Properties properties, String... tooltipKeys) {
        super(properties);
        this.tooltipKeys = tooltipKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        for (String tooltipKey : tooltipKeys) {
            tooltipComponents.add(Component.translatable(tooltipKey));
        }
    }
}
