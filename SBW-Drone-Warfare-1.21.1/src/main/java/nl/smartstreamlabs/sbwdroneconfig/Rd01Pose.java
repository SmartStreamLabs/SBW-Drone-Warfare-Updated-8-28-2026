package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector3d;

/** Native 16-unit model -> aircraft-centred attitude -> yaw -> world, once. */
public final class Rd01Pose {
    public static final double CENTRE_HEIGHT=22.5/16, MODEL_CENTRE=50.0/16;
    // Vanilla PlayerRenderer scale .9375 and model root 1.501, hip pivot 12/16.
    public static final double PLAYER_HIP=(1.501-.75)*.9375;
    public static Matrix4d aircraft(Rd01Entity d,float partial) {
        Vec3 p=d.getPosition(partial);
        return new Matrix4d().translate(p.x,p.y+CENTRE_HEIGHT,p.z)
                .rotateY(Math.toRadians(180-Mth.rotLerp(partial,d.yRotO,d.getYRot())))
                .rotateX(Math.toRadians(d.visualPitch(partial)))
                .rotateZ(Math.toRadians(-d.visualRoll(partial)))
                .translate(0,-MODEL_CENTRE,0);
    }
    public static Matrix4d harness(Rd01Entity d,float partial) {return harness(d,partial,d.extension(partial));}
    static Matrix4d harness(Rd01Entity d,float partial,float extension) {
        return harness(d,partial,extension,d.stow(partial));
    }
    static Matrix4d harness(Rd01Entity d,float partial,float extension,float stow) {
        return aircraft(d,partial).mul(harnessLocal(d,partial,extension,stow));
    }
    public static Matrix4d harnessLocal(Rd01Entity d,float partial,float extension,float stow) {
        return new Matrix4d().translate(0,.25-extension,0).translate(0,37.0/16,1.0/16)
                .rotateZ(d.swayZ(partial)).rotateX(d.swayX(partial)+stow*Math.PI/2).translate(0,-37.0/16,-1.0/16);
    }
    public static Vec3 point(Matrix4d matrix,double x,double y,double z) {
        Vector3d p=matrix.transformPosition(new Vector3d(x/16,y/16,z/16));
        return new Vec3(p.x,p.y,p.z);
    }
    public static Vec3 hips(Rd01Entity d,float partial) {return point(harness(d,partial),0,14,0);}
    public static Vec3 feet(Rd01Entity d,float partial) {return hips(d,partial).add(0,-PLAYER_HIP,0);}
    public static Vec3 head(Rd01Entity d,float partial) {return point(harness(d,partial),0,29.5,0);}
    public static Vec3 lens(Rd01Entity d,float partial) {
        Matrix4d m=aircraft(d,partial);
        Vec3 direction=point(m,0,43.8,-18.5).subtract(point(m,0,43.8,-17.5));
        return point(m,0,43.8,-17.5).add(direction.normalize().scale(.1));
    }
    private Rd01Pose() {}
}
