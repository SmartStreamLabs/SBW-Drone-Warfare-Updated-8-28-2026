package nl.smartstreamlabs.sbwdroneconfig;
/** Rotate a vanilla player's head axis toward vehicle +Z and face toward vehicle +Y. */
public final class PatientPose {
 private PatientPose(){}public static org.joml.Quaternionf orientation(org.joml.Matrix4d vehicle){return new org.joml.Quaternionf(vehicle.getNormalizedRotation(new org.joml.Quaterniond())).rotateX((float)(Math.PI/2));}
 public static net.minecraft.world.phys.AABB bounds(org.joml.Matrix4d vehicle){double minX=Double.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;for(double x:new double[]{-.55,.55})for(double y:new double[]{.95,1.58})for(double z:new double[]{-.82+MedicalTrailerEntity.AXLE_Z,1.18+MedicalTrailerEntity.AXLE_Z}){var point=vehicle.transformPosition(new org.joml.Vector3d(x,y,z));minX=Math.min(minX,point.x);minY=Math.min(minY,point.y);minZ=Math.min(minZ,point.z);maxX=Math.max(maxX,point.x);maxY=Math.max(maxY,point.y);maxZ=Math.max(maxZ,point.z);}return new net.minecraft.world.phys.AABB(minX,minY,minZ,maxX,maxY,maxZ);}
 public static final float HEAD_LIFT_PIXELS=(float)((1.025-(1.0875-.25*.9375))*16/.9375);
}
