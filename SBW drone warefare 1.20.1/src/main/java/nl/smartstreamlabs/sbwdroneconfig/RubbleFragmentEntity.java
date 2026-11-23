package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;

/** Temporary native moving collider. Settling creates rubble, never an original block or an item. */
public final class RubbleFragmentEntity extends Entity {
    private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(RubbleFragmentEntity.class,EntityDataSerializers.INT);
    private UUID receipt,owner;private boolean settled,impacted;private int age,steps;private double lx,ly,lz;
    public RubbleFragmentEntity(EntityType<? extends RubbleFragmentEntity> type,Level l){super(type,l);}
    @Override protected void defineSynchedData(){entityData.define(STATE,Block.getId(Blocks.STONE.defaultBlockState()));}
    public BlockState state(){return Block.stateById(entityData.get(STATE));}
    public void init(RubbleTasks.Deposit d,Vec3 at,net.minecraft.core.BlockPos centre){receipt=d.id();owner=d.owner();entityData.set(STATE,Block.getId(d.state()));moveTo(at.x,at.y-.2,at.z,0,0);var outward=at.subtract(Vec3.atCenterOf(centre)).normalize().scale(.10);setDeltaMovement(outward.x,.08,outward.z);}
    @Override public void tick(){super.tick();if(level().isClientSide){if(steps>0){setPos(getX()+(lx-getX())/steps,getY()+(ly-getY())/steps,getZ()+(lz-getZ())/steps);steps--;}return;}if(settled){discard();return;}if(receipt==null)receipt=getUUID();age++;var v=getDeltaMovement().add(0,-.045,0);if(v.lengthSqr()>2)v=v.normalize().scale(1.4);move(MoverType.SELF,v);setDeltaMovement(v.multiply(.96,.98,.96));
        if(onGround()||age>=100||getY()<level().getMinBuildHeight()+1){
            if(onGround()&&!impacted){impacted=true;if(RubbleConfig.IMPACT.get()>0&&v.y<-.2)for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.15)))e.hurt(damageSources().fallingBlock(this),(float)Math.min(RubbleConfig.IMPACT.get(),-v.y*2));}
            if(level()instanceof ServerLevel l){RubbleServer.rest(l,new RubbleTasks.Deposit(receipt,blockPosition(),state(),owner));RubbleEvents.burst(l,blockPosition(),state(),1);}settled=true;discard();
        }
    }
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int ticks,boolean teleport){lx=x;ly=y;lz=z;steps=Math.max(1,ticks);}
    @Override protected void addAdditionalSaveData(CompoundTag n){n.put("Source",RubbleMaterials.write(state()));if(owner!=null)n.putUUID("Owner",owner);if(receipt!=null)n.putUUID("Receipt",receipt);n.putBoolean("Settled",settled);n.putBoolean("Impacted",impacted);n.putInt("Age",age);}
    @Override protected void readAdditionalSaveData(CompoundTag n){entityData.set(STATE,Block.getId(RubbleMaterials.read(n.getCompound("Source"))));receipt=n.hasUUID("Receipt")?n.getUUID("Receipt"):getUUID();owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;settled=n.getBoolean("Settled");impacted=n.getBoolean("Impacted");age=Math.min(100,n.getInt("Age"));if(settled)discard();}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(){return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);}
}
