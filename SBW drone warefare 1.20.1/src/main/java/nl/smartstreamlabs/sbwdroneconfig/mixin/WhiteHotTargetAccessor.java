package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Minecraft.class)
public interface WhiteHotTargetAccessor {
    @Mutable @Accessor("mainRenderTarget") void drone$setVideoTarget(RenderTarget target);
}
