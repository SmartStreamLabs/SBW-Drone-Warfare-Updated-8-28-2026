package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.*;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Continuous drone-volume sweep against translating/rotating VC-01 panels.
 * Rotation is conservatively subdivided into <=.04m vertex travel intervals;
 * each interval uses continuous SAT and its residual rotation envelope.
 */
public final class Vc01Sweep {
    private Vc01Sweep() {}
    public static Matrix4d pose(Matrix4dc a,Matrix4dc b,double t) {
        var qa=a.getUnnormalizedRotation(new Quaterniond()); var qb=b.getUnnormalizedRotation(new Quaterniond());
        var pa=a.getTranslation(new Vector3d()); var pb=b.getTranslation(new Vector3d());
        return new Matrix4d().translation(pa.lerp(pb,t)).rotate(qa.slerp(qb,t));
    }
    public static double hit(AABB start, Vec3 motion, NtGeometry.Triangle panel, Matrix4dc old, Matrix4dc now) {
        var qa=old.getUnnormalizedRotation(new Quaterniond()); var qb=now.getUnnormalizedRotation(new Quaterniond());
        double angle=2*Math.acos(Math.min(1,Math.abs(qa.dot(qb))));
        if(angle<1e-7) {
            var face=Vc01Geometry.triangle(panel,old);
            Vec3 drift=Vc01Geometry.transform(now,Vec3.ZERO).subtract(Vc01Geometry.transform(old,Vec3.ZERO));
            Vec3 relative=motion.subtract(drift);
            if(!start.expandTowards(relative).intersects(face.bounds()))return Double.NaN;
            return NtGeometry.sweep(start,relative,face);
        }
        double radius=Math.max(panel.a().length(),Math.max(panel.b().length(),panel.c().length()));
        int count=Math.min(2048,Math.max(1,(int)Math.ceil(angle*radius/.04)));
        Vec3 oldOrigin=Vc01Geometry.transform(old,Vec3.ZERO),newOrigin=Vc01Geometry.transform(now,Vec3.ZERO);
        for(int i=0;i<count;i++) {
            double a=i/(double)count,b=(i+1)/(double)count,mid=(a+b)/2;
            Matrix4d m=pose(old,now,mid); var face=Vc01Geometry.triangle(panel,m);
            Vec3 drift=newOrigin.subtract(oldOrigin).scale(1.0/count);
            double rotationEnvelope=2*radius*Math.sin(angle/(4*count));
            Vec3 half=drift.scale(-.5);
            face=new NtGeometry.Triangle(face.a().add(half),face.b().add(half),face.c().add(half));
            AABB box=start.move(motion.scale(a)).inflate(rotationEnvelope);
            Vec3 relative=motion.scale(1.0/count).subtract(drift);
            if(!box.expandTowards(relative).intersects(face.bounds().inflate(rotationEnvelope)))continue;
            double t=NtGeometry.sweep(box,relative,face);
            if(Double.isFinite(t))return a+t/count;
        }
        return Double.NaN;
    }
}
