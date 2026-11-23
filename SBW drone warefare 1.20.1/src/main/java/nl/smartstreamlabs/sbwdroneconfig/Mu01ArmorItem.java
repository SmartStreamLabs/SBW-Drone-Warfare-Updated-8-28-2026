package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import java.util.List;
public final class Mu01ArmorItem extends ArmorItem {
 public final EquipmentSlot muSlot;
 public Mu01ArmorItem(Type type){super(ArmorMaterials.IRON,type,new Item.Properties().durability(switch(type){case HELMET->165;case CHESTPLATE->240;case LEGGINGS->225;case BOOTS->195;default->0;}));muSlot=type.getSlot();}
 @Override public int getMaxDamage(ItemStack stack){return UniformArmorConfig.durability("mu01",super.getMaxDamage(stack));}
 @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack stack){var result=com.google.common.collect.HashMultimap.create(super.getAttributeModifiers(slot,stack));if(slot==muSlot){var armor=java.util.List.copyOf(result.get(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR));result.removeAll(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);for(var modifier:armor)result.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,new net.minecraft.world.entity.ai.attributes.AttributeModifier(modifier.getId(),modifier.getName(),modifier.getAmount()*UniformArmorConfig.settings("mu01").armor(),modifier.getOperation()));double value=UniformArmorConfig.bullet("mu01",slot,stack.getDamageValue(),stack.getMaxDamage());result.put(com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE.get(),new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.nameUUIDFromBytes(("dw_ballistic_"+slot).getBytes(java.nio.charset.StandardCharsets.UTF_8)),"DW ballistic protection",value,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));}return result;}
 @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,level,lines,flag);lines.add(Component.translatable("mu01.description."+muSlot.getName()));lines.add(Component.translatable("mu01.cosmetic"));}
}
