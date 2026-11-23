package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
public final class DefenseRenderer implements BlockEntityRenderer<DefenseBlockEntity>{
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(DefenseBlockEntity b){return b.getRenderBoundingBox();}
 @Override public boolean shouldRenderOffScreen(DefenseBlockEntity b){return true;}@Override public int getViewDistance(){return 192;}@Override public boolean shouldRender(DefenseBlockEntity b,net.minecraft.world.phys.Vec3 camera){return b.getRenderBoundingBox().getCenter().distanceToSqr(camera)<192*192;}
 @Override public void render(DefenseBlockEntity b,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-b.facing().toYRot()));double scan=b.active()?(Math.floorMod(b.getLevel().getGameTime(),200)+partial)/20d:-1;DefenseModel.get(b.kind()).draw(b.kind(),p,buffers,light,overlay,b.pose(partial),scan,b.covers(partial),false,true);p.popPose();}
}
