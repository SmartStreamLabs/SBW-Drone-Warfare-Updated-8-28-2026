package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
/** One R3 pose shared by the renderer, camera and contact point. Native forward is +Z. */
public final class Id01Pose {
    public static final double ROOT_HEIGHT=21.25/16.0;
    public static Vec3 direction(Id01Entity e,float partial,Vec3 nativeVector){
        var p=Id01TransformMath.direction(nativeVector.x,nativeVector.y,nativeVector.z,Mth.rotLerp(partial,e.yRotO,e.getYRot()),e.posePitch(partial),e.tilt(partial));
        return new Vec3(p[0],p[1],p[2]);
    }
    public static Vec3 point(Id01Entity e,float partial,Vec3 nativePoint){
        return e.getPosition(partial).add(0,ROOT_HEIGHT,0).add(direction(e,partial,nativePoint));
    }
    public static Vec3 lens(Id01Entity e,float partial){
        Vec3 p=new Vec3(.522/16,-1.856/16,25.56/16);
        return point(e,partial,p).add(direction(e,partial,new Vec3(0,0,.08)));
    }
    public static Vec3 nose(Id01Entity e){return point(e,1,new Vec3(0,0,25.8/16));}
    /** Relative swept AABB intersection: both participants move during the sampled interval. */
    public static boolean contact(Vec3 previousNose,Vec3 nose,Vec3 previousTarget,net.minecraft.world.phys.AABB targetBox){
        Vec3 relativeStart=previousNose.add(targetBox.getCenter().subtract(previousTarget));
        var expanded=targetBox.inflate(.22);
        return Id01ContactMath.segment(new double[]{relativeStart.x,relativeStart.y,relativeStart.z},new double[]{nose.x,nose.y,nose.z},new double[]{expanded.minX,expanded.minY,expanded.minZ},new double[]{expanded.maxX,expanded.maxY,expanded.maxZ});
    }
}
