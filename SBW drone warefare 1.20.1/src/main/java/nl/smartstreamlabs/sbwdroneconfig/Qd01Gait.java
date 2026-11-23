package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.Vec3;import net.minecraft.util.Mth;
/** Per-entity presentation only: four planted world-space feet, bounded swing and analytical joint rotations. */
public final class Qd01Gait {
 private final Vec3[] planted=new Vec3[4],from=new Vec3[4],to=new Vec3[4],targets=new Vec3[4];
 private final float[][] old=new float[4][4],angles=new float[4][4];private final boolean[] swinging=new boolean[4];private final double[] support={Double.NaN,Double.NaN,Double.NaN,Double.NaN};
 private long tick=Long.MIN_VALUE;private Vec3 previous;private float previousYaw;private boolean moving;
 public int groundQueries;public boolean moving(){return moving;}public Vec3 target(int leg){return targets[leg];}
 public float angle(int leg,int joint,float partial){return Mth.lerp(partial,old[leg][joint],angles[leg][joint]);}
 public void update(Qd01Entity e,boolean detailed){
  long now=e.level().getGameTime();if(tick==now)return;long gap=tick==Long.MIN_VALUE?Long.MAX_VALUE:now-tick;tick=now;groundQueries=0;
  Vec3 delta=previous==null?Vec3.ZERO:e.position().subtract(previous);float turn=previous==null?0:Mth.wrapDegrees(e.getYRot()-previousYaw);
  moving=delta.horizontalDistanceSqr()>1e-6||Math.abs(turn)>.05;boolean reset=previous==null||gap>5||delta.lengthSqr()>4;
  previous=e.position();previousYaw=e.getYRot();var nativeFrame=e.nativeFrame(1);var inverse=new org.joml.Matrix4d(nativeFrame).invert();
  for(int i=0;i<4;i++){
   System.arraycopy(angles[i],0,old[i],0,4);double phase=e.gaitPhase()+((i==0||i==3)?0:.5);phase-=Math.floor(phase);
   var n=nativeFrame.transformPosition(new org.joml.Vector3d(Qd01Ground.x(i),0,Qd01Ground.z(i)));Vec3 rest=new Vec3(n.x,n.y,n.z);
   if(reset||planted[i]==null){planted[i]=ground(e,rest,detailed);from[i]=to[i]=planted[i];swinging[i]=false;}
   boolean swing=moving&&phase>=.5;
   if(swing&&!swinging[i]){
    from[i]=planted[i];Vec3 lead=delta.scale(3.5);if(lead.horizontalDistance()>.14)lead=lead.normalize().scale(.14);
    // Predict a small rotation around the body as well as translation; this includes turning in place.
    Vec3 relative=rest.subtract(e.position()).yRot(-Mth.clamp(turn*3.5f,-16,16)*Mth.DEG_TO_RAD);
    to[i]=ground(e,e.position().add(relative).add(lead),detailed);
   }
   Vec3 target;
   if(swing){double t=(phase-.5)*2,u=Mth.clamp((t-.18)/.64,0,1),s=u*u*(3-2*u),up=Mth.clamp(t/.22,0,1),down=Mth.clamp((1-t)/.22,0,1),apex=Math.max(from[i].y,to[i].y)+.10;target=from[i].lerp(to[i],s);double y=t<.5?Mth.lerp(up*up*(3-2*up),from[i].y,apex):Mth.lerp(down*down*(3-2*down),to[i].y,apex);target=new Vec3(target.x,y,target.z);}
   else {if(swinging[i])planted[i]=to[i];if(!moving)planted[i]=planted[i].lerp(detailed&&tick%2==0?ground(e,rest,true):new Vec3(rest.x,planted[i].y,rest.z),.18);target=planted[i];}
   // Replant only at the bounded edge of reach, rather than letting a sharp pivot pull a foot through the floor.
   Vec3 horizontal=new Vec3(target.x-rest.x,0,target.z-rest.z);if(horizontal.horizontalDistance()>.20){var bounded=horizontal.normalize().scale(.20);target=new Vec3(rest.x+bounded.x,target.y,rest.z+bounded.z);if(!swing)planted[i]=target;}
   if(detailed&&(tick%2==0||reset)){groundQueries++;support[i]=Qd01Ground.sole(e.level(),e,target);}
   if(detailed&&Double.isFinite(support[i])&&target.y<support[i]+.006){target=new Vec3(target.x,support[i]+.006,target.z);if(!swing)planted[i]=target;}
   swinging[i]=swing;targets[i]=target;
   var local=inverse.transformPosition(new org.joml.Vector3d(target.x,target.y+.075,target.z));
   var pose=Qd01Ik.solve(i==0||i==2,local.x-(i==0||i==2?-.49:.49),local.y-.82,local.z-(i<2?-.52:.51),Mth.lerp(1,e.xRotO,e.getXRot())*Mth.DEG_TO_RAD);
   double[] values={pose.hip(),pose.thigh(),pose.shin(),pose.foot()};for(int j=0;j<4;j++)angles[i][j]=(float)values[j];
  }
 }
 private Vec3 ground(Qd01Entity e,Vec3 at,boolean query){if(query){groundQueries++;var hit=Qd01Ground.contact(e.level(),e,at,.65,.9);if(hit!=null)return hit;}return new Vec3(at.x,e.getY(),at.z);}
}
