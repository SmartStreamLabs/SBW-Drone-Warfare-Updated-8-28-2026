package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.Vec3;

public final class MissionMath {
    private MissionMath(){}
    public static boolean finite(Vec3 v){return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
    public static Vec3 closest(Vec3 a,Vec3 b,Vec3 p){var d=b.subtract(a);double t=d.lengthSqr()<1e-12?0:Math.max(0,Math.min(1,p.subtract(a).dot(d)/d.lengthSqr()));return a.add(d.scale(t));}
    /** Exact unpowered controller recurrence, including SBW's post-travel -0.06 bias.
     * No cruise, lift assist, throttle or imaginary propulsion is added to LP-03 missions. */
    public static boolean glideReachable(Vec3 start,Vec3 forward,Vec3 target){
        Vec3 p=start,velocity=forward.scale(GlidePlatformLayout.LAUNCH_SPEED).add(0,GlidePlatformLayout.LAUNCH_RISE,0);
        float pitch=(float)GlidePlatformLayout.RAIL_PITCH;
        for(int tick=0;tick<600;tick++){
            Vec3 next;
            if(tick<8)next=forward.scale(GlidePlatformLayout.LAUNCH_SPEED).add(0,GlidePlatformLayout.LAUNCH_RISE,0);
            else {
                pitch*=.88F;
                double decay=(1-Math.max(AddonConfig.lucasAcceleration(),.085)*.55)*Math.max(AddonConfig.lucasDrag(),.992);
                double sx=velocity.x*decay,sz=velocity.z*decay,speed=Math.hypot(sx,sz);
                float stall=AddonConfig.lucasStallEnabled()?LucasFlightPhysicsMath.computeStallSeverity(speed,AddonConfig.lucasMinLiftSpeed()):0;
                double lift=-Math.sin(Math.toRadians(pitch))*speed*.022*(1-stall*.45);
                double vy=velocity.y*.84+lift-.014-.030-(AddonConfig.lucasStallEnabled()?AddonConfig.lucasStallGravity()*stall:0);
                next=new Vec3(sx,Math.max(-.72,Math.min(.42,vy))-.06,sz);
            }
            Vec3 end=p.add(next);
            if(closest(p,end,target).distanceToSqr(target)<=1.25*1.25)return true;
            p=end;velocity=next;
            if(p.y<target.y-2 && next.y<0)return false;
        }
        return false;
    }
}
