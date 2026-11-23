package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;
import org.joml.Vector3d;

/** Uniform visual fit only: the top of a native item meets the supplied suspension socket. */
public record Hb01PayloadFit(Matrix4d matrix, double scale, double width, double height, double depth) {
    public static final double MAX_WIDTH=2.0/16, MAX_HEIGHT=5.6/16, MAX_DEPTH=3.1/16;
    public static Hb01PayloadFit fromCenteredBounds(double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        double x=maxX-minX,y=maxY-minY,z=maxZ-minZ;
        Matrix4d rotation=new Matrix4d();
        // Long native rocket/item meshes hang down; broad disk mines retain their native up axis.
        if(x>1.8*Math.max(y,z))rotation.rotateZ(Math.PI/2);
        else if(z>1.8*Math.max(x,y))rotation.rotateX(Math.PI/2);
        Vector3d min=new Vector3d(Double.POSITIVE_INFINITY),max=new Vector3d(Double.NEGATIVE_INFINITY);
        for(int i=0;i<8;i++){
            var v=rotation.transformPosition(new Vector3d((i&1)==0?minX:maxX,(i&2)==0?minY:maxY,(i&4)==0?minZ:maxZ));min.min(v);max.max(v);
        }
        double scale=Math.min(MAX_WIDTH/Math.max(.002,max.x-min.x),Math.min(MAX_HEIGHT/Math.max(.002,max.y-min.y),MAX_DEPTH/Math.max(.002,max.z-min.z)));
        Matrix4d matrix=new Matrix4d().translate(-(min.x+max.x)*.5*scale,-max.y*scale,-(min.z+max.z)*.5*scale).scale(scale).mul(rotation);
        return new Hb01PayloadFit(matrix,scale,(max.x-min.x)*scale,(max.y-min.y)*scale,(max.z-min.z)*scale);
    }
}
