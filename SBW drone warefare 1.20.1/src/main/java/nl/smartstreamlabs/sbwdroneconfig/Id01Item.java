package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
public final class Id01Item extends Item {
    public Id01Item(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(c.getClickedFace()!=Direction.UP||!p.mayUseItemAt(c.getClickedPos(),c.getClickedFace(),c.getItemInHand())||!c.getLevel().mayInteract(p,c.getClickedPos()))return InteractionResult.FAIL;
        var d=AddonEntities.ID01.get().create(c.getLevel());if(d==null)return InteractionResult.FAIL;
        var saved=c.getItemInHand().getTag();
        if(saved!=null&&saved.contains("Id01Saved")){var tag=saved.getCompound("Id01Saved").copy();tag.remove("UUID");tag.remove("Pos");tag.remove("Motion");tag.putInt("Id01Mode",0);tag.putFloat("Id01Tilt",90);d.load(tag);}
        var at=c.getClickLocation();d.setPos(c.getClickedPos().getX()+.5,at.y,c.getClickedPos().getZ()+.5);d.setYRot(p.getYRot());d.yRotO=d.getYRot();if(d.ownerId()==null)d.assign(p);else d.relocateHome();
        if(!Id01Geometry.parts(d).stream().allMatch(b->c.getLevel().noCollision(d,b))||Id01Geometry.parts(d).stream().allMatch(b->c.getLevel().noCollision(d,b.move(0,-.08,0))))return InteractionResult.FAIL;
        if(!c.getLevel().addFreshEntity(d))return InteractionResult.FAIL;
        if(!p.getAbilities().instabuild)c.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
