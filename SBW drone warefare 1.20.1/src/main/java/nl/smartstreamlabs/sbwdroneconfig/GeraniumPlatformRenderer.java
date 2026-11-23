package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class GeraniumPlatformRenderer extends GeoBlockRenderer<GeraniumPlatformBlockEntity> implements FastMeshRenderer<GeraniumPlatformBlockEntity> {
    private static ResourceLocation id(String path){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,path);}
    private static final java.util.concurrent.atomic.AtomicInteger PREVIEW_IDS=new java.util.concurrent.atomic.AtomicInteger(-1);
    private record Preview(net.minecraft.world.item.ItemStack stack,Geranium2Entity entity){}
    public GeraniumPlatformRenderer(){super(new GeoModel<>(){
        @Override public ResourceLocation getModelResource(GeraniumPlatformBlockEntity p){return id("geo/geranium2_launch_platform_lp02.geo.json");}
        @Override public ResourceLocation getTextureResource(GeraniumPlatformBlockEntity p){return id("textures/block/geranium2_launch_platform_lp02.png");}
        @Override public ResourceLocation getAnimationResource(GeraniumPlatformBlockEntity p){return id("animations/geranium2_launch_platform_lp02.animation.json");}
        @Override public void setCustomAnimations(GeraniumPlatformBlockEntity p,long instanceId,software.bernie.geckolib.core.animation.AnimationState<GeraniumPlatformBlockEntity> state){
            super.setCustomAnimations(p,instanceId,state);
            float amount=p.openness(state.getPartialTick());
            getBone("console_lid").ifPresent(bone->bone.setRotX(GeraniumPlatformClampAnimation.LID_BASE_X+GeraniumPlatformClampAnimation.LID_CLOSE_X*p.lidClosedAmount(state.getPartialTick())));
            getBone("clamp_left").ifPresent(bone->bone.setRotZ(GeraniumPlatformClampAnimation.LEFT_RADIANS*amount));
            getBone("clamp_right").ifPresent(bone->bone.setRotZ(GeraniumPlatformClampAnimation.RIGHT_RADIANS*amount));
        }
    });}
    @Override protected void rotateBlock(Direction facing,PoseStack pose){pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));pose.translate(1,0,0);}
    @Override public boolean shouldRenderOffScreen(GeraniumPlatformBlockEntity be){return true;}
    @Override public int getViewDistance(){return 128;}
    @Override public void actuallyRender(PoseStack pose,GeraniumPlatformBlockEntity be,BakedGeoModel baked,RenderType type,MultiBufferSource buffers,VertexConsumer vertex,boolean rerender,float partial,int light,int overlay,float r,float g,float b,float a){
        if(rerender){super.actuallyRender(pose,be,baked,type,buffers,vertex,true,partial,light,overlay,r,g,b,a);return;}
        pose.pushPose();
        super.actuallyRender(pose,be,baked,type,buffers,vertex,false,partial,light,overlay,r,g,b,a);
        pose.popPose();
        if(!be.previewDrone().isEmpty()) {
            Preview cached=be.renderProxy() instanceof Preview value?value:null;
            if(cached==null || !net.minecraft.world.item.ItemStack.matches(cached.stack(),be.previewDrone())) {
                var proxy=AddonEntities.GERANIUM_2.get().create(be.getLevel());if(proxy==null)return;
                var data=be.previewEntityData();for(String key:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})data.remove(key);
                var defaults=new net.minecraft.nbt.CompoundTag();proxy.saveWithoutId(defaults);defaults.merge(data);
                if(defaults.contains("Item",10) && defaults.getCompound("Item").isEmpty())defaults.remove("Item");
                proxy.load(defaults);
                ((net.minecraft.world.entity.Entity)proxy).setId(PREVIEW_IDS.getAndDecrement());
                cached=new Preview(be.previewDrone().copy(),proxy);be.renderProxy(cached);
            }
            // Client-only detached render proxy. Never added to a level, never ticked, no physics or control session.
            // The LP-specific branch of the existing renderer shares the Fit Scene scale and rail attitude.
            Geranium2Entity proxy=cached.entity();net.minecraft.world.entity.Entity entity=proxy;
            float travel=be.previewTravel(partial);
            var at=GeraniumPlatformLayout.mount(be.getBlockPos(),be.facing()).add(GeraniumPlatformLayout.forward(be.facing()).scale(travel*GeraniumPlatformLayout.LAUNCH_SPEED)).add(0,GeraniumPlatformLayout.LAUNCH_RISE*travel,0);
            proxy.setLp02Mount(be.getBlockPos());entity.moveTo(at.x,at.y,at.z,be.facing().toYRot(),(float)GeraniumPlatformLayout.RAIL_PITCH);entity.yRotO=entity.getYRot();entity.xRotO=entity.getXRot();entity.setYBodyRot(entity.getYRot());
            entity.tickCount=(int)(be.getLevel().getGameTime()&0x3fffffff);entity.setOnGround(be.phase()!=GeraniumPlatformBlockEntity.Phase.LAUNCHING && travel==0);
            // GeckoLib 4.7 centres blocks in preRender; 4.4 centres them in actuallyRender.
            // Start from the captured block origin in both versions, before applying the shared world mount.
            pose.pushPose();pose.last().pose().set(blockRenderTranslations);pose.translate(at.x-be.getBlockPos().getX(),at.y-be.getBlockPos().getY(),at.z-be.getBlockPos().getZ());
            var renderer=net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(proxy);
            renderer.render(proxy,entity.getYRot(),partial,pose,buffers,light);
            pose.popPose();
        }
    }
}
