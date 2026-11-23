package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;
/** The actual passenger is rendered by Minecraft once. Correct interpolation to the same carrier frame used by the server. */
public final class Dc01DockRender {
 public static void apply(Entity d,float partial,PoseStack pose){var c=Dc01Docking.carrier(d);if(c==null)return;int slot=c.slot(d);if(slot<0)return;var matrix=c.dockMatrix(slot,partial);var old=d.getPosition(partial);pose.translate(matrix.m30()-old.x,matrix.m31()-old.y,matrix.m32()-old.z);pose.mulPose(matrix.getUnnormalizedRotation(new org.joml.Quaternionf()));float yaw=((com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)d).getYaw(partial);pose.mulPose(Axis.YP.rotationDegrees(yaw));}
 private Dc01DockRender(){}
}
