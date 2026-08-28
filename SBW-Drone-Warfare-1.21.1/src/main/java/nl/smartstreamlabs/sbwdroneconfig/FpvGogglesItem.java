package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FpvGogglesItem extends ArmorItem {
    private static final String ARMOR_TEXTURE = "minecraft:textures/block/white_wool.png";
    private final String[] tooltipKeys;

    public FpvGogglesItem(Properties properties, String... tooltipKeys) {
        super(ArmorMaterials.LEATHER, Type.HELMET, properties);
        this.tooltipKeys = tooltipKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        for (String tooltipKey : tooltipKeys) {
            tooltipComponents.add(Component.translatable(tooltipKey));
        }
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return slot == EquipmentSlot.HEAD ? ARMOR_TEXTURE : null;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(FpvGogglesClientExtensions.INSTANCE);
    }
}
