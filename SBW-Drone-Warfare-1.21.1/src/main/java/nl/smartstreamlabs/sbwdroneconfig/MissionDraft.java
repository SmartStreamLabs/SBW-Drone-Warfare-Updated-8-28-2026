package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Raw drafts are retained even when invalid; rejected edits can never launch an old valid target. */
public final class MissionDraft {
    public boolean enabled, valid;
    public String x="",y="",z="",cruise="",message="",result="",status="Ready";
    public UUID last;
    public Vec3 target=Vec3.ZERO;
    public double altitude;
    public void read(CompoundTag t){
        enabled=t.getBoolean("Enabled");valid=t.getBoolean("Valid");x=t.getString("X");y=t.getString("Y");z=t.getString("Z");cruise=t.getString("Cruise");message=t.getString("Message");result=t.getString("Result");status=t.getString("Status");last=t.hasUUID("Last")?t.getUUID("Last"):null;
        if(valid)try{target=new Vec3(Double.parseDouble(x),Double.parseDouble(y),Double.parseDouble(z));altitude=cruise.isBlank()?target.y:Double.parseDouble(cruise);if(!MissionMath.finite(target)||!Double.isFinite(altitude))valid=false;}catch(NumberFormatException e){valid=false;}
    }
    public CompoundTag write(){var t=new CompoundTag();t.putBoolean("Enabled",enabled);t.putBoolean("Valid",valid);t.putString("X",x);t.putString("Y",y);t.putString("Z",z);t.putString("Cruise",cruise);t.putString("Message",message);t.putString("Result",result);t.putString("Status",status);if(last!=null)t.putUUID("Last",last);return t;}
}
