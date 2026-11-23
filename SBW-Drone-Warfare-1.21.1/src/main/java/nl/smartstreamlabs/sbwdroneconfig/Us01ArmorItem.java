package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import java.util.List;
public final class Us01ArmorItem extends ArmorItem {
 public final EquipmentSlot usSlot;
 public Us01ArmorItem(Type type){super(ArmorMaterials.IRON,type,new Item.Properties().durability(switch(type){case HELMET->165;case CHESTPLATE->240;case LEGGINGS->225;case BOOTS->195;default->0;}));usSlot=type.getSlot();}
 @Override public int getMaxDamage(ItemStack stack){return UniformArmorConfig.durability("us01",super.getMaxDamage(stack));}
 @Override public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack){var base=super.getDefaultAttributeModifiers(stack);var entries=new java.util.ArrayList<net.minecraft.world.item.component.ItemAttributeModifiers.Entry>();for(var entry:base.modifiers()){var modifier=entry.modifier();if(entry.attribute().value()==net.minecraft.world.entity.ai.attributes.Attributes.ARMOR.value())modifier=new net.minecraft.world.entity.ai.attributes.AttributeModifier(modifier.id(),modifier.amount()*UniformArmorConfig.settings("us01").armor(),modifier.operation());entries.add(new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(entry.attribute(),modifier,entry.slot()));}double value=UniformArmorConfig.bullet("us01",usSlot,stack.getDamageValue(),stack.getMaxDamage());entries.add(new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"ballistic_"+usSlot.getName()),value,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.bySlot(usSlot)));return new net.minecraft.world.item.component.ItemAttributeModifiers(entries,true);}
 @Override public void appendHoverText(ItemStack stack,TooltipContext level,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,level,lines,flag);lines.add(Component.translatable("us01.description."+usSlot.getName()));lines.add(Component.translatable("us01.cosmetic"));}
}
