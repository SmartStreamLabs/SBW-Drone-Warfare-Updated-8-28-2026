package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ShaderInstance.class)
public interface WhiteHotShaderAccessor {
    @Accessor("lastProgramId") static int drone$lastProgram(){throw new AssertionError();}
    @Accessor("lastProgramId") static void drone$lastProgram(int value){throw new AssertionError();}
    @Accessor("lastAppliedShader") static ShaderInstance drone$lastShader(){throw new AssertionError();}
    @Accessor("lastAppliedShader") static void drone$lastShader(ShaderInstance value){throw new AssertionError();}
}
