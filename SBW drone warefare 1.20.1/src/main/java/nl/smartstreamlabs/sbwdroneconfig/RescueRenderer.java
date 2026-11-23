package nl.smartstreamlabs.sbwdroneconfig;
public class RescueRenderer<T extends com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity> extends CachedVehicleRenderer<T>{
 private long hitchDebugTick=-1;
 public RescueRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c,String id){super(c,new RescueModel<T>(id));shadowRadius=1f;}
 @Override public void render(T entity,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
  pose.pushPose();if(entity instanceof MedicalTrailerEntity trailer){var offset=trailer.hitchRenderOffset(partial);pose.translate(offset.x,offset.y,offset.z);}super.render(entity,yaw,partial,pose,buffers,light);pose.popPose();if(!(entity instanceof SupplyRoverEntity r))return;
  if(HitchJoint.DEBUG&&r.partner()!=null){for(var en:net.minecraft.client.Minecraft.getInstance().level.entitiesForRendering())if(en instanceof MedicalTrailerEntity t&&HitchJoint.paired(r,t)){var base=r.getPosition(partial);var rh=r.transformPosition(r.getVehicleTransform(partial),0,.65625,-2.15625);var th=t.transformPosition(t.getVehicleTransform(partial),0,.65625,2.25+MedicalTrailerEntity.AXLE_Z);var a=new net.minecraft.world.phys.Vec3(rh.x,rh.y,rh.z).subtract(base);var b=new net.minecraft.world.phys.Vec3(th.x,th.y,th.z).subtract(base);if(hitchDebugTick!=r.level().getGameTime()&&r.level().getGameTime()%5==0){hitchDebugTick=r.level().getGameTime();SbwDroneRangeConfig.LOGGER.info("HITCH render tick={} gap={} R={} T={}",r.tickCount,a.distanceTo(b),a.add(base),b.add(base));}var lines=buffers.getBuffer(net.minecraft.client.renderer.RenderType.lines());net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose,lines,new net.minecraft.world.phys.AABB(a,a).inflate(.045),1,.1f,.1f,1);net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose,lines,new net.minecraft.world.phys.AABB(b,b).inflate(.035),.1f,1,.1f,1);break;}}

  var matrix=r.getVehicleTransform(partial);pose.pushPose();
  pose.translate(matrix.m30()-net.minecraft.util.Mth.lerp(partial,r.xo,r.getX()),matrix.m31()-net.minecraft.util.Mth.lerp(partial,r.yo,r.getY()),matrix.m32()-net.minecraft.util.Mth.lerp(partial,r.zo,r.getZ()));pose.mulPose(matrix.getUnnormalizedRotation(new org.joml.Quaternionf()));
  var items=net.minecraft.client.Minecraft.getInstance().getItemRenderer();
  for(int i=0;i<SupplyRoverEntity.DISPLAY_SLOTS;i++){var stack=r.displayedCargo(i);if(stack.isEmpty())continue;boolean solid=items.getModel(stack,r.level(),null,i).isGui3d();pose.pushPose();pose.translate((i%3-1)*.42,.940625+(solid?.17:.012),-1.05+(new int[]{1,2,0,3}[i/3])*.65);pose.scale(.34f,.34f,.34f);if(!solid)pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));items.renderStatic(stack,net.minecraft.world.item.ItemDisplayContext.NONE,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,r.level(),i);pose.popPose();}
  pose.popPose();
 }
}
