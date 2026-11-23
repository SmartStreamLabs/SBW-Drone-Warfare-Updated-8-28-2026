package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.level.Level;import net.minecraft.world.item.*;import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** GUI thumbnails for our existing SBW containers. Held and placed containers remain native SBW. */
@Mixin(value=GuiGraphics.class,remap=false) public abstract class VehicleContainerGuiMixin {
 @Inject(method="renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",at=@At("HEAD"),cancellable=true)
 private void dronewarfare$vehicleIcon(LivingEntity entity,Level level,ItemStack stack,int x,int y,int seed,int offset,CallbackInfo ci){
  if(!(stack.getItem() instanceof com.atsuishio.superbwarfare.item.container.ContainerBlockItem))return;
  var data=stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);if(data==null)return;var tag=data.copyTag();
  String id=tag.getString("EntityType");
  // SBW final renders an orange crate where 0.8.9.1 renders its vehicle thumbnail.
  // Draw our supplied native thumbnail directly, independently of that renderer difference.
  if(id.startsWith("sbwdroneconfig:")){
   var icon=ResourceLocation.tryParse("superbwarfare:textures/vehicle_icon/container/"+id.substring(id.indexOf(':')+1)+".png");
   if(icon!=null&&net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(icon).isPresent()){
    var graphics=(GuiGraphics)(Object)this;graphics.pose().pushPose();graphics.pose().translate(0,0,150+offset);graphics.blit(icon,x,y,0,0,16,16,16,16);graphics.pose().popPose();ci.cancel();return;
   }
  }
  String name=switch(id){case "sbwdroneconfig:mobile_patriot_launcher_mpl01"->"mobile_patriot_launcher_mpl01";case "sbwdroneconfig:anti_drone_vehicle_adv01"->"anti_drone_vehicle_adv01";case "sbwdroneconfig:heavy_ground_drone_hgd01"->"heavy_ground_drone_hgd01";case "sbwdroneconfig:quadruped_drone_qd01"->"quadruped_drone_qd01";case "sbwdroneconfig:water_drone_wd01"->"water_drone_wd01";case "sbwdroneconfig:drone_carrier_dc01"->"drone_carrier_dc01";case "sbwdroneconfig:mobile_jammer_truck_mj03"->"mobile_jammer_truck_mj03";case "sbwdroneconfig:vatag_ugv_v01"->"vatag_ugv_v01";case "sbwdroneconfig:supply_rover_sr01"->"supply_rover_sr01";case "sbwdroneconfig:medical_trailer_mt01"->"medical_trailer_mt01";case "sbwdroneconfig:mine_rover_mr01"->"mine_rover_mr01";default->null;};
  if(name==null)return;
  var texture=ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","textures/item/"+name+".png");
  var g=(GuiGraphics)(Object)this;g.pose().pushPose();g.pose().translate(0,0,150+offset);g.blit(texture,x,y,0,0,16,16,16,16);g.pose().popPose();ci.cancel();
 }
}
