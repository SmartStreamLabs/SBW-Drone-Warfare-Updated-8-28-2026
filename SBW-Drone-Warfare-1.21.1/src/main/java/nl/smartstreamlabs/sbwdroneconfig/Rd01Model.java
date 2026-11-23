package nl.smartstreamlabs.sbwdroneconfig;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.animation.AnimationState;
import net.minecraft.resources.ResourceLocation;

public final class Rd01Model extends GeoModel<Rd01Entity> {
    private static final class Spin {double last=-1,angle;float speed;}
    private final java.util.Map<Rd01Entity,Spin> spins=new java.util.WeakHashMap<>();
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,s);}
    @Override public ResourceLocation getModelResource(Rd01Entity d){return id("geo/rescue_drone_rd01.geo.json");}
    @Override public ResourceLocation getTextureResource(Rd01Entity d){return id("textures/entity/rescue_drone_rd01.png");}
    @Override public ResourceLocation getAnimationResource(Rd01Entity d){return id("animations/rescue_drone_rd01.animation.json");}
    @Override public void setCustomAnimations(Rd01Entity d,long instance,AnimationState<Rd01Entity> state) {
        super.setCustomAnimations(d,instance,state);float p=state.getPartialTick();Spin s=spins.computeIfAbsent(d,k->new Spin());double now=d.tickCount+p;
        if(s.last>=0&&now>=s.last){double dt=Math.min(2,now-s.last);s.speed=net.minecraft.util.Mth.lerp((float)Math.min(1,dt*.12),s.speed,d.motor()?1:0);s.angle=(s.angle+dt*s.speed*2.35)%(2*Math.PI);}s.last=now;
        for(int i=1;i<=6;i++){final float angle=(float)s.angle*(i%2==1?1:-1);getBone("rotor_"+i).ifPresent(b->b.setRotY(angle));}
        getBone("harness_sway").ifPresent(b->{b.setPosY((.25f-d.extension(p))*16);b.setRotZ(d.swayZ(p));b.setRotX(d.swayX(p)+d.stow(p)*(float)Math.PI/2);});
        getBone("harness_frame").ifPresent(b->b.setTrackingMatrices(true));
        getBone("winch_frame").ifPresent(b->b.setTrackingMatrices(true));
        var eye=Rd01Pose.point(Rd01Pose.harnessLocal(d,p,d.extension(p),d.stow(p)),0,37.12,1);
        var cable=Rd01CablePose.fit(new org.joml.Vector3d(eye.x*16,eye.y*16,eye.z*16));
        getBone("suspension_cable").ifPresent(b->{
            b.setHidden(false);b.setPosX((float)-cable.offset().x);b.setPosY((float)cable.offset().y);b.setPosZ((float)cable.offset().z);
            b.setRotX((float)cable.rotation().x);b.setRotY((float)cable.rotation().y);b.setRotZ((float)cable.rotation().z);
            b.setScaleX(1);b.setScaleY((float)cable.scaleY());b.setScaleZ(1);
        });
        getBone("winch_drum").ifPresent(b->b.setRotX(d.extension(p)*12));
        getBone("harness_gate").ifPresent(b->b.setRotZ(d.occupied()?0:(float)Math.toRadians(-75)));
        // The supplied camera-look clip is a preview; a fixed lens stays aligned with the real monitor view.
        getBone("camera_pan").ifPresent(b->b.setRotY(0));getBone("camera_tilt").ifPresent(b->b.setRotX(0));
    }
}
