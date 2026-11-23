package nl.smartstreamlabs.sbwdroneconfig;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.animation.AnimationState;
import net.minecraft.resources.ResourceLocation;
public final class Id01Model extends GeoModel<Id01Entity>{
    private static final java.util.Map<Id01Entity,Id01Renderer.Spin> SPINS=new java.util.WeakHashMap<>();
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,s);}
    public ResourceLocation getModelResource(Id01Entity e){return id("geo/interceptor_drone_id01.geo.json");}
    public ResourceLocation getTextureResource(Id01Entity e){return id("textures/entity/interceptor_drone_id01.png");}
    public ResourceLocation getAnimationResource(Id01Entity e){return id("animations/interceptor_drone_id01.animation.json");}
    @Override public void setCustomAnimations(Id01Entity e,long instance,AnimationState<Id01Entity> state){
        super.setCustomAnimations(e,instance,state);
        getBone("flight_attitude").ifPresent(b->b.setRotX((float)Math.toRadians(e.tilt(state.getPartialTick()))));
        // Integrate only while powered, not absolute age * speed (which jumps when motors restart).
        var spin=SPINS.computeIfAbsent(e,ignored->new Id01Renderer.Spin());
        double now=e.tickCount+state.getPartialTick();if(spin.last>=0&&now>=spin.last){double dt=Math.min(2,now-spin.last);spin.speed=net.minecraft.util.Mth.lerp((float)Math.min(1,dt*.12),spin.speed,(e.motor()||e.getPersistentData().getBoolean("sbwdroneconfigDockPreview"))?1f:0f);spin.angle=(spin.angle+dt*spin.speed*2.2)%(Math.PI*2);}spin.last=now;
        final float a=(float)spin.angle;getBone("rotor_L1").ifPresent(b->b.setRotZ(a));getBone("rotor_L2").ifPresent(b->b.setRotZ(-a));getBone("rotor_R1").ifPresent(b->b.setRotZ(-a));getBone("rotor_R2").ifPresent(b->b.setRotZ(a));
    }
}
