package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;

/** Unsaved interaction target at the real harness; visual geometry stays on the parent model. */
public final class Rd01HarnessEntity extends Entity {
    private static final EntityDataAccessor<Integer> PARENT=SynchedEntityData.defineId(Rd01HarnessEntity.class,EntityDataSerializers.INT);
    public Rd01HarnessEntity(EntityType<? extends Rd01HarnessEntity> t,Level l){super(t,l);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(PARENT,-1);}
    public void attach(Rd01Entity parent){entityData.set(PARENT,parent.getId());follow();}
    public Rd01Entity parent(){return level().getEntity(entityData.get(PARENT)) instanceof Rd01Entity d?d:null;}
    public void follow(){Rd01Entity d=parent();if(d!=null){var at=Rd01Pose.hips(d,1);setPos(at.x,at.y-.6,at.z);}}
    @Override public void tick(){super.tick();Rd01Entity d=parent();if(d==null||d.isRemoved()){if(!level().isClientSide())discard();return;}follow();}
    @Override public boolean isPickable(){return parent()!=null;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public InteractionResult interact(Player p,InteractionHand h){return parent()!=null&&p.getItemInHand(h).isEmpty()&&!p.isShiftKeyDown()?parent().board(p):InteractionResult.PASS;}
    @Override protected void readAdditionalSaveData(CompoundTag n){}
    @Override protected void addAdditionalSaveData(CompoundTag n){}
}
