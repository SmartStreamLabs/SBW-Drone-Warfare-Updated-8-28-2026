package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.animation.AnimationState;
import java.util.WeakHashMap;

public final class BayraktarTb2Model extends GeoModel<BayraktarTb2Entity> {
    private final WeakHashMap<BayraktarTb2Entity,Bt01WheelMotion> wheels=new WeakHashMap<>();
    @Override public ResourceLocation getModelResource(BayraktarTb2Entity d){return BayraktarTb2Definition.model();}
    @Override public ResourceLocation getTextureResource(BayraktarTb2Entity d){return BayraktarTb2Definition.texture();}
    @Override public ResourceLocation getAnimationResource(BayraktarTb2Entity d){return BayraktarTb2Definition.animation();}
    @Override public void setCustomAnimations(BayraktarTb2Entity d,long instance,AnimationState<BayraktarTb2Entity> state){
        super.setCustomAnimations(d,instance,state);float partial=state==null?0:state.getPartialTick();
        getBone("propeller").ifPresent(b->b.setRotZ((float)Math.toRadians((d.tickCount+partial)*105))); // existing cosmetic rate
        var wheel=wheels.computeIfAbsent(d,ignored->new Bt01WheelMotion());wheel.update(d.tickCount,d.getX(),d.getZ(),d.getYaw(1),d.onGround());
        getBone("wheel_front").ifPresent(b->b.setRotX((float)wheel.front()));
        getBone("wheel_L").ifPresent(b->b.setRotX((float)wheel.main()));getBone("wheel_R").ifPresent(b->b.setRotX((float)wheel.main()));
        getBone("nose_steering").ifPresent(b->b.setRotY((float)wheel.steering()));
        // As with the existing LUCAS visuals, attitude is only a cosmetic input; no control forces are added.
        float pitch=d.getBodyPitch(partial),roll=d.getRoll(partial);
        getBone("aileron_L").ifPresent(b->b.setRotX(rad(roll*.35f)));getBone("aileron_R").ifPresent(b->b.setRotX(rad(-roll*.35f)));
        getBone("ruddervator_L").ifPresent(b->b.setRotX(rad(-pitch*.4f+roll*.15f)));getBone("ruddervator_R").ifPresent(b->b.setRotX(rad(pitch*.4f+roll*.15f)));
        // tail_hinge_basis_L/R retain the supplied diagonal bind rotations. Fixed gear/flaps/camera stay in bind pose.
    }
    private static float rad(float degrees){return (float)Math.toRadians(Math.max(-18,Math.min(18,degrees)));}
}
