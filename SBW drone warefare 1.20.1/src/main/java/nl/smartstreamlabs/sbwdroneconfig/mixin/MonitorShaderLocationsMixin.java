package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import nl.smartstreamlabs.sbwdroneconfig.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Keep all uploads and drawing; cache only repeated location queries in authenticated camera contexts. */
@Mixin(ShaderInstance.class)
public abstract class MonitorShaderLocationsMixin {
 @Unique private ShaderUniformLocations drone$locations;
 @Redirect(method="apply",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/shaders/Uniform;glGetUniformLocation(ILjava/lang/CharSequence;)I"))
 private int drone$location(int program,CharSequence uniform){
  if(!VatagClient.active()&&!Rd01Client.active()&&!Id01Client.active())return Uniform.glGetUniformLocation(program,uniform);
  if(drone$locations==null)drone$locations=new ShaderUniformLocations();
  String key=uniform.toString();Integer found=drone$locations.lookup(program,key);if(found!=null)return found;
  int value=Uniform.glGetUniformLocation(program,uniform);drone$locations.remember(program,key,value);return value;
 }
 @Inject(method="updateLocations",at=@At("HEAD"))
 private void drone$relinked(CallbackInfo ci){if(drone$locations!=null)drone$locations.clear();}
}
