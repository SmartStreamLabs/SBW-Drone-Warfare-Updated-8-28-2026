package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
public class Mj03Item extends Item {
    public Mj03Item(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getPlayer()==null)return InteractionResult.FAIL;
        if(c.getClickedFace()!=net.minecraft.core.Direction.UP || !c.getLevel().mayInteract(c.getPlayer(),c.getClickedPos()) || !c.getPlayer().mayUseItemAt(c.getClickedPos(),c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
        Vec3 at=c.getClickLocation().add(0,.1,0);
        double angle=Math.toRadians(c.getPlayer().getYRot()+180),cos=Math.abs(Math.cos(angle)),sin=Math.abs(Math.sin(angle));
        double halfX=cos*1.5+sin*5.2,halfZ=sin*1.5+cos*5.2;
        AABB body=new AABB(at.x-halfX,at.y,at.z-halfZ,at.x+halfX,at.y+5.3,at.z+halfZ);
        if(!c.getLevel().getWorldBorder().isWithinBounds(body))return InteractionResult.FAIL;
        if(!c.getLevel().hasChunksAt(net.minecraft.core.BlockPos.containing(body.minX,body.minY,body.minZ),net.minecraft.core.BlockPos.containing(body.maxX,body.maxY,body.maxZ)) || !c.getLevel().noCollision(null,body))return InteractionResult.FAIL;
        if(!c.getLevel().isClientSide){
            Mj03Entity e=AddonEntities.MJ03.get().create(c.getLevel());if(e==null)return InteractionResult.FAIL;
            ((Entity)e).moveTo(at.x,at.y,at.z,c.getPlayer().getYRot()+180,0);e.owner(c.getPlayer().getUUID());
            if(c.getPlayer().isCreative())e.setEnergy(e.getMaxEnergy());
            if(!c.getLevel().addFreshEntity(e))return InteractionResult.FAIL;
            if(!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
        }return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
    }
}
