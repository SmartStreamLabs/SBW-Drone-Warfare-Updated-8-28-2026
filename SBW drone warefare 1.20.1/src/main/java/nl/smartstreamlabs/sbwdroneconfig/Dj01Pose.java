package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.joml.*;
import java.lang.ref.WeakReference;
import java.lang.Math;

/** Matches both native player hand attachment points to the two authored grips without scaling the prop or player. */
public final class Dj01Pose {
    private static WeakReference<LivingEntity> holder=new WeakReference<>(null);
    private static final Matrix4f correction=new Matrix4f();
    private static boolean valid;
    public static boolean firstPersonHand;
    public static void capture(LivingEntity p){if(holder.get()!=p){holder=new WeakReference<>(p);valid=false;}}
    public static void pose(Player p,ModelPart head,ModelPart left,ModelPart right){
        if(firstPersonHand||!Dj01Item.is(p.getMainHandItem())||p.isSleeping()||p.getVehicle() instanceof MedicalTrailerEntity||p.getVehicle() instanceof Rd01Entity)return;
        capture(p);boolean dominantRight=p.getMainArm()==HumanoidArm.RIGHT;ModelPart main=dominantRight?right:left,support=dominantRight?left:right;float sign=dominantRight?1:-1;
        var look=new Matrix4f().rotateY(head.yRot).rotateX(head.xRot);
        // Native model coordinates: +Y down, head forward -Z. Grip separation is 0.02 up and 0.495 forward blocks.
        Vector3f delta=look.transformDirection(new Vector3f(0,-.32f,-7.92f));
        var shoulder=new Vector3f(main.x,main.y,main.z);var other=new Vector3f(support.x,support.y,support.z);
        var secondCentre=new Vector3f(other).sub(delta);var axis=new Vector3f(secondCentre).sub(shoulder);float distance=axis.length();if(distance<1e-5)return;axis.div(distance);
        var centre=new Vector3f(shoulder).add(secondCentre).mul(.5f);
        var desired=look.transformDirection(new Vector3f(-2*sign,3,-3)).add(0,2,0).sub(centre);
        desired.sub(new Vector3f(axis).mul(desired.dot(axis)));if(desired.lengthSquared()<1e-6)desired.set(axis.z,0,-axis.x);desired.normalize();
        // The native held-item origin in an arm's local pixel frame is (+/-1,10,-2), length sqrt(105).
        float radius=(float)Math.sqrt(Math.max(0,105-distance*distance*.25f));
        var grip=new Vector3f(centre).add(desired.mul(radius));var fore=new Vector3f(grip).add(delta);
        arm(main,new Vector3f(-sign,10,-2),new Vector3f(grip).sub(shoulder));
        arm(support,new Vector3f(sign,10,-2),new Vector3f(fore).sub(other));
        var attachment=new Matrix4f().translate(main.x/16,main.y/16,main.z/16).rotateZYX(main.zRot,main.yRot,main.xRot).scale(main.xScale,main.yScale,main.zScale)
                .rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).translate(sign/16,.125f,-.625f);
        var target=new Matrix4f().translate(new Vector3f(grip).div(16)).mul(look).rotateZ((float)Math.PI).translate(0,.035f,-.195f);
        correction.set(attachment).invert().mul(target);valid=true;
    }
    private static void arm(ModelPart part,Vector3f local,Vector3f target){
        var q=new Quaternionf().rotationTo(local.normalize(),target.normalize());var r=q.getEulerAnglesZYX(new Vector3f());part.xRot=r.x;part.yRot=r.y;part.zRot=r.z;
    }
    public static boolean apply(com.mojang.blaze3d.vertex.PoseStack pose){
        var p=holder.get();if(!valid||p==null||!Dj01Item.is(p.getMainHandItem()))return false;
        pose.last().pose().mul(correction);pose.last().normal().mul(new Matrix3f(correction));return true;
    }
    public static boolean emitting(){var p=holder.get();return p!=null&&p.isUsingItem()&&p.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND&&Dj01Item.is(p.getUseItem())&&Dj01Item.emitting(p.getMainHandItem());}
    private Dj01Pose(){}
}
