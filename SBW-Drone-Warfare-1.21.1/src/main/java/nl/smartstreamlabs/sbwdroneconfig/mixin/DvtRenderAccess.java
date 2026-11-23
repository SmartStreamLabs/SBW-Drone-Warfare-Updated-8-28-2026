package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.LevelRenderer;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=Minecraft.class,remap=false)public interface DvtRenderAccess {@Mutable @Accessor("levelRenderer")void dvt$renderer(LevelRenderer value);@Accessor("cameraEntity")void dvt$camera(net.minecraft.world.entity.Entity value);}
