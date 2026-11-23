package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;

public final class Rd01Item extends Item {
    public Rd01Item(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c) {
        if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        if(c.getClickedFace()!=Direction.UP||!p.mayUseItemAt(c.getClickedPos(),c.getClickedFace(),c.getItemInHand())||!c.getLevel().mayInteract(p,c.getClickedPos()))return InteractionResult.FAIL;
        Rd01Entity d=AddonEntities.RD01.get().create(c.getLevel());if(d==null)return InteractionResult.FAIL;
        var tag=c.getItemInHand().getTag();
        if(tag!=null&&tag.contains("Rd01Saved")){var n=tag.getCompound("Rd01Saved").copy();for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers"})n.remove(k);d.load(n);}
        d.setPos(c.getClickedPos().getX()+.5,c.getClickLocation().y,c.getClickedPos().getZ()+.5);d.setYRot(p.getYRot());d.yRotO=d.getYRot();
        if(d.ownerId()==null)d.assign(p);
        if(!Rd01Geometry.parts(d).stream().allMatch(b->c.getLevel().noCollision(d,b))||!Rd01Geometry.harnessClear(d,d.extension(1),net.minecraft.world.phys.Vec3.ZERO))return InteractionResult.FAIL;
        if(!c.getLevel().addFreshEntity(d))return InteractionResult.FAIL;
        if(!p.getAbilities().instabuild)c.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
