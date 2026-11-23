package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;
/** Native LC-01 is already in 16-units/block. Only the -Z model basis is reversed, once. */
public final class Lc01VisualPose {
    public static final double SCALE=1.0, FREE_Y=.12, CULL_RADIUS=2.25;
    // Native airframe translated to the unchanged LP-01 cradle before rail rotation.
    // Contact was measured against the supplied fuselage and all eight cradle supports.
    public static final double PLATFORM_OFFSET_Y=(10.9658-14.45)/16.0;
    public static final double PLATFORM_OFFSET_Z=-2.0/16.0;
    public static Matrix4d matrix(float yaw,float pitch,float roll,boolean platform){
        var matrix=new Matrix4d().translate(0,platform?0:FREE_Y,0).rotateY(Math.toRadians(-yaw))
            .rotateX(Math.toRadians(pitch)).rotateZ(Math.toRadians(-roll));
        if(platform)matrix.translate(0,PLATFORM_OFFSET_Y,PLATFORM_OFFSET_Z);
        return matrix.rotateY(Math.PI);
    }
    private Lc01VisualPose(){}
}
