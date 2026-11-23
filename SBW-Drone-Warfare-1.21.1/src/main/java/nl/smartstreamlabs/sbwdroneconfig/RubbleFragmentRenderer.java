package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
public final class RubbleFragmentRenderer extends EntityRenderer<RubbleFragmentEntity> {
    public RubbleFragmentRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.15f;}
    @Override public ResourceLocation getTextureLocation(RubbleFragmentEntity e){return TextureAtlas.LOCATION_BLOCKS;}
    @Override public void render(RubbleFragmentEntity e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.translate(0,.18,0);p.mulPose(com.mojang.math.Axis.YP.rotationDegrees((e.tickCount+pt)*8));p.scale(.34f,.34f,.34f);p.translate(-.5,-.5,-.5);net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(e.state(),p,b,light,OverlayTexture.NO_OVERLAY);p.popPose();}
}
