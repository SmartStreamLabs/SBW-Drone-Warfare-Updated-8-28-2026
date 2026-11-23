package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
public final class HeavyBomberDroneModel extends GeoModel<HeavyBomberDroneEntity> {
    @Override public ResourceLocation getModelResource(HeavyBomberDroneEntity d){return HeavyBomberDroneDefinition.model();}
    @Override public ResourceLocation getTextureResource(HeavyBomberDroneEntity d){return HeavyBomberDroneDefinition.texture();}
    @Override public ResourceLocation getAnimationResource(HeavyBomberDroneEntity d){return HeavyBomberDroneDefinition.animation();}
    @Override public void setCustomAnimations(HeavyBomberDroneEntity d,long instance,AnimationState<HeavyBomberDroneEntity> state){
        super.setCustomAnimations(d,instance,state);
        // Preserve the former renderer's cosmetic 64 degrees/tick; only the mesh/bindings change.
        float angle=(float)Math.toRadians((d.tickCount+(state==null?0:state.getPartialTick()))*64);
        for(int i=1;i<=6;i++){var bone=getAnimationProcessor().getBone("rotor_"+i);if(bone!=null)bone.setRotY((i%2==1?1:-1)*angle);}
    }
}
