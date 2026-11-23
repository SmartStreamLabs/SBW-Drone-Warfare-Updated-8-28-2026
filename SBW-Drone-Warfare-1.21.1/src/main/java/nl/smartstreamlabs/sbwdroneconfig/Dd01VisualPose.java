package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;
/** Native DD-01 units are converted by the mesh baker once. Body and cargo share this pose. */
public final class Dd01VisualPose {
    public static final double CARGO_CENTER_Y=12.0/16, CENTER_Y=32.0/16;
    public static final double MIN_X=-43.1/16,MAX_X=43.1/16,MIN_Y=0,MAX_Y=41.2/16,MIN_Z=-38.947/16,MAX_Z=38.947/16;
    public static Matrix4d matrix(float yaw,float pitch,float roll){
        return new Matrix4d().translate(0,CENTER_Y,0).rotateY(Math.toRadians(-yaw))
            .rotateX(Math.toRadians(pitch)).rotateZ(Math.toRadians(roll)).rotateY(Math.PI).translate(0,-CENTER_Y,0);
    }
    private Dd01VisualPose(){}
}
