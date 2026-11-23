package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;
/** Visual model basis only; no vehicle movement or physical bounds. */
public final class Hb01VisualPose {
    public static final double CENTER_Y=11.5/16, MIN_X=-18.237/16,MAX_X=18.237/16,MIN_Y=0,MAX_Y=15.85/16,MIN_Z=-17.601/16,MAX_Z=17.601/16;
    private static final double[][] SOCKETS={{-1.15,6.9,-1.75},{1.15,6.9,-1.75},{-1.15,6.9,1.75},{1.15,6.9,1.75}};
    public static Matrix4d matrix(float yaw,float pitch,float roll){
        return new Matrix4d().translate(0,CENTER_Y,0).rotateY(Math.toRadians(-yaw))
            .rotateX(Math.toRadians(pitch)).rotateZ(Math.toRadians(roll)).rotateY(Math.PI).translate(0,-CENTER_Y,0);
    }
    public static org.joml.Vector3d socket(int index){double[] v=SOCKETS[index];return new org.joml.Vector3d(v[0]/16,v[1]/16,v[2]/16);}
    private Hb01VisualPose(){}
}
