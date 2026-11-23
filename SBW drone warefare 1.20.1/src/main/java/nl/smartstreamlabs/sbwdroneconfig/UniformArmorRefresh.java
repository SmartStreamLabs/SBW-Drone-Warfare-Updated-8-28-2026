package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.lang.ref.WeakReference;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
/** Server-thread weak index and shared bounded refresh queue; no full level scan and no entity retained after unload. */
@Mod.EventBusSubscriber(modid="sbwdroneconfig")
public final class UniformArmorRefresh {
 private static final Set<LivingEntity> WEARERS=Collections.newSetFromMap(new WeakHashMap<>());
 private record Work(UUID id,WeakReference<LivingEntity> entity){}
 private static final ArrayDeque<Work> QUEUE=new ArrayDeque<>();private static final Set<UUID> PENDING=new HashSet<>();
 private static final List<EquipmentSlot> SLOTS=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
 private static boolean ours(ItemStack stack){return stack.getItem() instanceof Mu01ArmorItem||stack.getItem() instanceof Us01ArmorItem;}
 public static void changed(){var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server!=null)server.execute(()->{for(var entity:WEARERS)if(PENDING.add(entity.getUUID()))QUEUE.add(new Work(entity.getUUID(),new WeakReference<>(entity)));});}
 @SubscribeEvent public static void equipment(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event){var entity=event.getEntity();if(entity.level().isClientSide())return;if(SLOTS.stream().anyMatch(slot->ours(entity.getItemBySlot(slot))))WEARERS.add(entity);else WEARERS.remove(entity);}
 @SubscribeEvent public static void leave(net.minecraftforge.event.entity.EntityLeaveLevelEvent event){if(event.getEntity() instanceof LivingEntity entity&&!entity.level().isClientSide())WEARERS.remove(entity);}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent event){WEARERS.clear();QUEUE.clear();PENDING.clear();}
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event){if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;int budget=UniformArmorConfig.SPEC.isLoaded()?UniformArmorConfig.REFRESH.get():4;for(int i=0;i<budget&&!QUEUE.isEmpty();i++){var work=QUEUE.remove();PENDING.remove(work.id());var entity=work.entity().get();if(entity!=null&&!entity.isRemoved()&&entity.isAlive())refresh(entity);}}
 public static void refresh(LivingEntity entity){
  var bullet=entity.getAttribute(com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE.get());
  for(var slot:SLOTS){if(bullet!=null)bullet.removeModifier(UUID.nameUUIDFromBytes(("dw_ballistic_"+slot).getBytes(java.nio.charset.StandardCharsets.UTF_8)));var stack=entity.getItemBySlot(slot);if(!ours(stack))continue;
   for(var entry:stack.getAttributeModifiers(slot).entries())if(entry.getKey()==Attributes.ARMOR||entry.getKey()==com.atsuishio.superbwarfare.init.ModAttributes.BULLET_RESISTANCE.get()){var attribute=entity.getAttribute(entry.getKey());if(attribute!=null){attribute.removeModifier(entry.getValue().getId());attribute.addTransientModifier(entry.getValue());}}
  }
 }
 private UniformArmorRefresh(){}
}
