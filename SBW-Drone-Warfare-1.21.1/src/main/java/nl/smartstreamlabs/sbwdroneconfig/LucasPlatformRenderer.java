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

public class LucasPlatformRenderer extends GeoBlockRenderer<LucasPlatformBlockEntity> implements FastMeshRenderer<LucasPlatformBlockEntity> {
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,path);}
    private static final java.util.concurrent.atomic.AtomicInteger PREVIEW_IDS=new java.util.concurrent.atomic.AtomicInteger(-1);
    private record Preview(net.minecraft.world.item.ItemStack stack,LucasDroneEntity entity){}
    public LucasPlatformRenderer(){super(new GeoModel<>(){
        @Override public ResourceLocation getModelResource(LucasPlatformBlockEntity p){return id("geo/lucas_launch_platform_lp01.geo.json");}
        @Override public ResourceLocation getTextureResource(LucasPlatformBlockEntity p){return id("textures/block/lucas_launch_platform_lp01.png");}
        @Override public ResourceLocation getAnimationResource(LucasPlatformBlockEntity p){return id("animations/lucas_launch_platform_lp01.animation.json");}
        @Override public void setCustomAnimations(LucasPlatformBlockEntity p,long instanceId,software.bernie.geckolib.animation.AnimationState<LucasPlatformBlockEntity> state){
            super.setCustomAnimations(p,instanceId,state);
            float amount=p.openness(state.getPartialTick());
            getBone("clamp_left").ifPresent(bone->bone.setRotZ(LucasPlatformClampAnimation.LEFT_RADIANS*amount));
            getBone("clamp_right").ifPresent(bone->bone.setRotZ(LucasPlatformClampAnimation.RIGHT_RADIANS*amount));
        }
    });}
    @Override protected void rotateBlock(Direction facing,PoseStack pose){pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));pose.translate(.5,0,0);}
    @Override public boolean shouldRenderOffScreen(LucasPlatformBlockEntity be){return true;}
    @Override public int getViewDistance(){return 128;}
    @Override public void actuallyRender(PoseStack pose,LucasPlatformBlockEntity be,BakedGeoModel baked,RenderType type,MultiBufferSource buffers,VertexConsumer vertex,boolean rerender,float partial,int light,int overlay,int color){
        if(rerender){super.actuallyRender(pose,be,baked,type,buffers,vertex,true,partial,light,overlay,color);return;}
        pose.pushPose();
        super.actuallyRender(pose,be,baked,type,buffers,vertex,false,partial,light,overlay,color);
        pose.popPose();
        if(!be.previewDrone().isEmpty()) {
            Preview cached=be.renderProxy() instanceof Preview value?value:null;
            if(cached==null || !net.minecraft.world.item.ItemStack.matches(cached.stack(),be.previewDrone())) {
                var proxy=AddonEntities.LUCAS_DRONE.get().create(be.getLevel());if(proxy==null)return;
                var data=be.previewEntityData();for(String key:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})data.remove(key);
                var defaults=new net.minecraft.nbt.CompoundTag();proxy.saveWithoutId(defaults);defaults.merge(data);
                if(defaults.contains("Item",10) && defaults.getCompound("Item").isEmpty())defaults.remove("Item");
                proxy.load(defaults);
                ((net.minecraft.world.entity.Entity)proxy).setId(PREVIEW_IDS.getAndDecrement());
                cached=new Preview(be.previewDrone().copy(),proxy);be.renderProxy(cached);
            }
            // Client-only detached render proxy. Never added to a level, never ticked, no physics or control session.
            // The LP-specific branch of the existing renderer shares the Fit Scene scale and rail attitude.
            LucasDroneEntity proxy=cached.entity();net.minecraft.world.entity.Entity entity=proxy;
            float travel=be.previewTravel(partial);
            var at=LucasPlatformLayout.mount(be.getBlockPos(),be.facing()).add(LucasPlatformLayout.forward(be.facing()).scale(travel*LucasPlatformLayout.LAUNCH_SPEED)).add(0,LucasPlatformLayout.LAUNCH_RISE*travel,0);
            proxy.setLp01Mount(be.getBlockPos());entity.moveTo(at.x,at.y,at.z,be.facing().toYRot(),(float)LucasPlatformLayout.RAIL_PITCH);entity.yRotO=entity.getYRot();entity.xRotO=entity.getXRot();entity.setYBodyRot(entity.getYRot());
            entity.tickCount=(int)(be.getLevel().getGameTime()&0x3fffffff);entity.setOnGround(be.phase()!=LucasPlatformBlockEntity.Phase.LAUNCHING && travel==0);
            // GeckoLib 4.7 centres the block in preRender, outside the saved model pose.
            // The proxy offset below is already world-relative: undo that centring once.
            pose.pushPose();pose.translate(-.5,0,-.5);pose.translate(at.x-be.getBlockPos().getX(),at.y-be.getBlockPos().getY(),at.z-be.getBlockPos().getZ());
            var renderer=net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(proxy);
            renderer.render(proxy,entity.getYRot(),partial,pose,buffers,light);
            pose.popPose();
        }
    }
}
