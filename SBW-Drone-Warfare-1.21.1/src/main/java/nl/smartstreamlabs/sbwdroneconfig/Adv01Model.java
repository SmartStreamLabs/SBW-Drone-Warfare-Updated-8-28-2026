package nl.smartstreamlabs.sbwdroneconfig;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.animation.AnimationState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class Adv01Model extends GeoModel<Adv01Entity> {
    private final double[] shoe=new double[3];
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,path);}
    @Override public ResourceLocation getModelResource(Adv01Entity e){return id("geo/anti_drone_vehicle_adv01.geo.json");}
    @Override public ResourceLocation getTextureResource(Adv01Entity e){return id("textures/entity/anti_drone_vehicle_adv01.png");}
    @Override public ResourceLocation getAnimationResource(Adv01Entity e){return id("animations/anti_drone_vehicle_adv01.animation.json");}
    @Override public void setCustomAnimations(Adv01Entity e,long instance,AnimationState<Adv01Entity> state){
        super.setCustomAnimations(e,instance,state);float p=state.getPartialTick();
        float elevation=-Mth.lerp(p,e.getTurretXRotO(),e.getTurretXRot());
        getBone("turret_yaw").ifPresent(b->b.setRotY(Mth.rotLerp(p,e.getTurretYRotO(),e.getTurretYRot())*Mth.DEG_TO_RAD));
        // The parser's initial snapshot already contains the supplied +35-degree bind pose.
        getBone("gun_pitch").ifPresent(b->{b.setRotX(b.getInitialSnapshot().getRotX()+(elevation-35)*Mth.DEG_TO_RAD);double age=e.level().getGameTime()+p-e.shotTime();b.setPosZ(age>=0&&age<3?(float)(-.32*(1-age/3)):0);});
        getBone("sensor_yaw").ifPresent(b->b.setRotY(0));
        getBone("sensor_pitch").ifPresent(b->b.setRotX(b.getInitialSnapshot().getRotX()+elevation*Mth.DEG_TO_RAD));
        getBone("driver_hatch").ifPresent(b->b.setRotZ(e.hatch(p)*105*Mth.DEG_TO_RAD));
        getBone("commander_hatch").ifPresent(b->b.setRotZ(-e.hatch(p)*105*Mth.DEG_TO_RAD));
        getBone("service_door_left").ifPresent(b->b.setRotY(e.service(p)*90*Mth.DEG_TO_RAD));
        getBone("service_door_right").ifPresent(b->b.setRotY(-e.service(p)*90*Mth.DEG_TO_RAD));
        boolean detail=net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(e.position())<48*48;
        for(int side=0;side<2;side++)for(int i=0;i<92;i++){
            Adv01TrackPath.pose(i,detail?e.trackTravel(side==0,p):0,shoe);
            var bone=getBone(Adv01TrackPath.bone(side,i));if(bone.isPresent()){var b=bone.get();b.setPosY((float)shoe[0]);b.setPosZ((float)shoe[1]);b.setRotX((float)shoe[2]);}
        }
        for(String side:new String[]{"left","right"}){
            float rotation=(float)(-e.trackTravel(side.equals("left"),p)/.47);
            for(int i=1;i<=6;i++)getBone("roadwheel_"+side+"_"+i).ifPresent(b->b.setRotX(rotation));
            for(String prefix:new String[]{"front_idler_","rear_idler_"})getBone(prefix+side).ifPresent(b->b.setRotX(rotation*.75f));
        }
    }
}
