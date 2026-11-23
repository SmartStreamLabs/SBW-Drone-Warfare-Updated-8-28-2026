package nl.smartstreamlabs.sbwdroneconfig;
/** The cached baked block model is centered at its mounting base. Inventory/ground item transforms would bury this very flat mesh. */
public final class Lm01Display {
 public static void render(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,net.minecraft.world.level.Level level,float scale){
  pose.pushPose();pose.scale(scale,scale,scale);pose.translate(-.5,.006,-.5);
  net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(AddonBlocks.LM01.get().defaultBlockState().setValue(Lm01Block.STAGE,Lm01Block.Stage.SAFE),pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
  pose.popPose();
 }
 private Lm01Display(){}
}
