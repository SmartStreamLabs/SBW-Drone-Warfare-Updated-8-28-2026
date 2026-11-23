package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.lang.ref.WeakReference;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Server-thread weak index and shared bounded refresh queue; no full level scan and no entity retained after unload. */
@EventBusSubscriber(modid="sbwdroneconfig")
public final class UniformArmorRefresh {
 private static final Set<LivingEntity> WEARERS=Collections.newSetFromMap(new WeakHashMap<>());
 private record Work(UUID id,WeakReference<LivingEntity> entity){}
 private static final ArrayDeque<Work> QUEUE=new ArrayDeque<>();private static final Set<UUID> PENDING=new HashSet<>();
 private static final List<EquipmentSlot> SLOTS=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
 private static boolean ours(ItemStack stack){return stack.getItem() instanceof Mu01ArmorItem||stack.getItem() instanceof Us01ArmorItem;}
 public static void changed(){var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server!=null)server.execute(()->{for(var entity:WEARERS)if(PENDING.add(entity.getUUID()))QUEUE.add(new Work(entity.getUUID(),new WeakReference<>(entity)));});}
 @SubscribeEvent public static void equipment(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event){var entity=event.getEntity();if(entity.level().isClientSide())return;if(SLOTS.stream().anyMatch(slot->ours(entity.getItemBySlot(slot))))WEARERS.add(entity);else WEARERS.remove(entity);}
 @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){if(event.getEntity() instanceof LivingEntity entity&&!entity.level().isClientSide())WEARERS.remove(entity);}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent event){WEARERS.clear();QUEUE.clear();PENDING.clear();}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){int budget=UniformArmorConfig.SPEC.isLoaded()?UniformArmorConfig.REFRESH.get():4;for(int i=0;i<budget&&!QUEUE.isEmpty();i++){var work=QUEUE.remove();PENDING.remove(work.id());var entity=work.entity().get();if(entity!=null&&!entity.isRemoved()&&entity.isAlive())refresh(entity);}}
 public static void refresh(LivingEntity entity){
  var bullet=entity.getAttribute(com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE);
  for(var slot:SLOTS){if(bullet!=null)bullet.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","ballistic_"+slot.getName()));var stack=entity.getItemBySlot(slot);if(!(stack.getItem() instanceof Mu01ArmorItem mu&&mu.muSlot==slot||stack.getItem() instanceof Us01ArmorItem us&&us.usSlot==slot))continue;
   for(var entry:stack.getItem().getDefaultAttributeModifiers(stack).modifiers())if(entry.attribute().value()==Attributes.ARMOR.value()||entry.attribute().value()==com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE.get()){var attribute=entity.getAttribute(entry.attribute());if(attribute!=null){attribute.removeModifier(entry.modifier().id());attribute.addTransientModifier(entry.modifier());}}
  }
 }
 private UniformArmorRefresh(){}
}
