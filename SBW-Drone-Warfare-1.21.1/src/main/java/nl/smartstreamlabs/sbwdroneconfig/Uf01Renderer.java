package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
public final class Uf01Renderer implements BlockEntityRenderer<Uf01BlockEntity>{
 @Override public boolean shouldRenderOffScreen(Uf01BlockEntity b){return true;}
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(Uf01BlockEntity be){return be.getRenderBoundingBox();}
 @Override public int getViewDistance(){return 256;}
 @Override public boolean shouldRender(Uf01BlockEntity b,net.minecraft.world.phys.Vec3 camera){return b.getRenderBoundingBox().getCenter().distanceToSqr(camera)<256*256;}
 @Override public void render(Uf01BlockEntity be,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){var l=be.getLevel();if(l==null||be.getBlockState().getValue(Uf01Block.PART)!=0)return;poses.pushPose();poses.translate(.5,0,.5);poses.mulPose(Axis.YP.rotationDegrees(270-be.getBlockState().getValue(Uf01Block.FACING).toYRot()));double seconds=(Math.floorMod(l.getGameTime(),80)+partial)/20d;float weather=Math.max(l.getRainLevel(partial),l.getThunderLevel(partial));Uf01Model.get().draw(poses,buffers,light,overlay,seconds,weather,false,true);poses.popPose();}
}
