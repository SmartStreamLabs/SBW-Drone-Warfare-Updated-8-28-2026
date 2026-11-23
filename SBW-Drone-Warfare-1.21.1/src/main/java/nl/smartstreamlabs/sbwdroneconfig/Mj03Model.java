package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.animation.AnimationState;
public class Mj03Model extends GeoModel<Mj03Entity> {
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,s);}
    @Override public ResourceLocation getModelResource(Mj03Entity e){return id("geo/mobile_jammer_truck_mj03.geo.json");}
    @Override public ResourceLocation getTextureResource(Mj03Entity e){return id("textures/entity/mobile_jammer_truck_mj03.png");}
    @Override public ResourceLocation getAnimationResource(Mj03Entity e){return id("animations/mobile_jammer_truck_mj03.animation.json");}
    @Override public void setCustomAnimations(Mj03Entity e,long instance,AnimationState<Mj03Entity> s){
        super.setCustomAnimations(e,instance,s);float p=s.getPartialTick();float d=e.deployment(p);
        float steer=Mth.lerp(p,e.getRudderRotO(),e.getRudderRot());
        for(String side:new String[]{"L","R"}){
            float roll=1.5f*Mth.lerp(p,side.equals("L")?e.getLeftWheelRotO():e.getRightWheelRotO(),side.equals("L")?e.getLeftWheelRot():e.getRightWheelRot());
            for(int axle=0;axle<3;axle++)getBone("wheel_"+side+axle).ifPresent(b->b.setRotX(roll));
            getBone("steer_"+side).ifPresent(b->b.setRotY(steer));
            for(String z:new String[]{"F","B"}){
                getBone("outrigger_"+side+z).ifPresent(b->b.setPosX((side.equals("L")?1:-1)*12*d));
                getBone("foot_"+side+z).ifPresent(b->b.setPosY(-12*d));
            }
        }
        getBone("steering_wheel").ifPresent(b->b.setRotZ(-steer*4));
        // 20 ticks/second, 3.6 km/h per m/s; scale 0..120 km/h across 260 degrees.
        float kmh=(float)(e.getDeltaMovement().horizontalDistance()*72.0);
        getBone("speed_needle").ifPresent(b->b.setRotZ((float)Math.toRadians(Mth.clamp(kmh/120f,0,1)*260)));
        getBone("panel_lift").ifPresent(b->b.setPosY(12*d));
        getBone("panel_pitch").ifPresent(b->b.setRotX((float)Math.toRadians(75*(1-d))));
        getBone("panel_azimuth").ifPresent(b->b.setRotY(e.azimuth(p)));
    }
}
