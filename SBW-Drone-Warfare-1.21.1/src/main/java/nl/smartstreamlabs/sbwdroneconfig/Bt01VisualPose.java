package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;
import org.joml.Vector3d;
/** Native model coordinates only. No physical dimensions or flight state are changed. */
public final class Bt01VisualPose {
    public static final double CENTER_Y=20.0/16,MIN_X=-6,MAX_X=6,MIN_Y=0,MAX_Y=2.2,MIN_Z=-3.25,MAX_Z=3.25;
    // Rotation-invariant culling also covers interpolated attitudes and the complete propeller sweep.
    public static final double CULL_RADIUS=7.1;
    private static final double[] SOCKET_X={-20,-30,20,30};
    public static Matrix4d matrix(float yaw,float pitch,float roll){return new Matrix4d().translate(0,CENTER_Y,0).rotateY(Math.toRadians(-yaw)).rotateX(Math.toRadians(pitch)).rotateZ(Math.toRadians(roll)).rotateY(Math.PI).translate(0,-CENTER_Y,0);}
    public static Vector3d socket(int index){return new Vector3d(SOCKET_X[index]/16,1,1.5/16);}
    private Bt01VisualPose(){}
}
