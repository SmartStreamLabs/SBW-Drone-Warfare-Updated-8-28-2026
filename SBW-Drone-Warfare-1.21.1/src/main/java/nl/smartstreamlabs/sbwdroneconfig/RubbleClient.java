package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;

public final class RubbleClient {
    private static RubbleReply status;private static long statusAt;private static boolean held;
    private static final ArrayList<Particle> PARTICLES=new ArrayList<>();private static net.minecraft.client.multiplayer.ClientLevel world;
    public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener(RubbleClient::models);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(RubbleClient.class);}
    private static void models(ModelEvent.ModifyBakingResult e){RubbleModel.clear();e.getModels().replaceAll((id,model)->id.toString().startsWith("sbwdroneconfig:rubble_pile#")?new RubbleModel(model):model);}
    public static void receive(RubbleReply m){status=m;var l=Minecraft.getInstance().level;statusAt=l==null?0:l.getGameTime();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var m=Minecraft.getInstance();if(world!=m.level){for(var p:PARTICLES)p.remove();PARTICLES.clear();world=m.level;status=null;held=false;}RubbleVisualBudget.tick();PARTICLES.removeIf(p->!p.isAlive());if(m.player==null||m.level==null)return;
        boolean down=m.screen==null&&m.isWindowActive()&&!VatagClient.active()&&m.options.keyUse.isDown()&&RubbleTools.shovel(m.player.getMainHandItem())&&m.hitResult instanceof BlockHitResult hit&&m.level.getBlockState(hit.getBlockPos()).getBlock()instanceof RubbleBlock;
        if(down&&m.player.tickCount%3==0){var hit=(BlockHitResult)m.hitResult;AddonNetwork.rubbleUse(new RubbleUse(hit.getBlockPos(),true));held=true;}else if(!down&&held){AddonNetwork.rubbleUse(new RubbleUse(net.minecraft.core.BlockPos.ZERO,false));held=false;}
    }
    @SubscribeEvent public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){if(RubbleTools.shovel(e.getItemStack())&&e.getLevel().getBlockState(e.getPos()).getBlock()instanceof RubbleBlock){e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);}}
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var m=Minecraft.getInstance();if(m.player==null||m.level==null||status==null||status.mode()==0)return;long age=m.level.getGameTime()-statusAt;if(age>12&&status.mode()==3)return;if(age>200&&status.mode()!=3)return;var g=e.getGuiGraphics();int w=m.getWindow().getGuiScaledWidth(),h=m.getWindow().getGuiScaledHeight(),x=w/2-92,y=h-72;
        String line=status.mode()==3?"RUBBLE "+status.units()+" units":status.mode()==1?"RCT-01: right-click again to start": "CLEARING | "+status.cleared()+" cleared | "+status.units()+" piles";
        g.fill(x-4,y-4,x+190,y+29,0xb0101719);g.drawString(m.font,line,x,y,0xffdbe4df);g.fill(x,y+15,x+184,y+21,0xff283331);int progress=status.required()>0?Math.min(184,184*status.work()/status.required()):0;g.fill(x,y+15,x+progress,y+21,0xffc2ad72);
    }
    @SubscribeEvent public static void preview(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;var m=Minecraft.getInstance();if(m.level==null||m.player==null)return;net.minecraft.core.BlockPos centre=null;int radius=RubbleConfig.AREA.get();
        if(status!=null&&(status.mode()==1||status.mode()==2)&&m.level.getGameTime()-statusAt<200){centre=status.centre();radius=status.radius();}
        else if(m.player.getMainHandItem().getItem()==AddonItems.RCT01.get()&&m.hitResult instanceof BlockHitResult hit)centre=hit.getBlockPos();if(centre==null)return;
        var camera=e.getCamera().getPosition();var p=e.getPoseStack();p.pushPose();p.translate(-camera.x,-camera.y,-camera.z);var buffers=m.renderBuffers().bufferSource();net.minecraft.client.renderer.LevelRenderer.renderLineBox(p,buffers.getBuffer(net.minecraft.client.renderer.RenderType.lines()),new AABB(centre).inflate(radius),.65f,.85f,.7f,.8f);p.popPose();buffers.endBatch(net.minecraft.client.renderer.RenderType.lines());
    }
    public static void burst(RubbleBurst m){var mc=Minecraft.getInstance();var l=mc.level;if(l==null||!l.dimension().location().equals(m.dimension()))return;var random=new Random(m.seed());int quality=mc.options.particles().get()==ParticleStatus.MINIMAL?1:mc.options.particles().get()==ParticleStatus.DECREASED?2:3;
        for(var s:m.samples()){if(mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(Vec3.atCenterOf(s.pos()))>RubbleClientConfig.DISTANCE.get()*RubbleClientConfig.DISTANCE.get())continue;var state=Block.stateById(s.state());for(int i=0;i<(RubbleClientConfig.SHARDS.get()?quality:0)&&PARTICLES.size()<RubbleClientConfig.PARTICLES.get();i++){var p=new TerrainParticle(l,s.pos().getX()+random.nextDouble(),s.pos().getY()+random.nextDouble(),s.pos().getZ()+random.nextDouble(),(random.nextDouble()-.5)*.22,.10+random.nextDouble()*.22,(random.nextDouble()-.5)*.22,state);p.scale(.35f+random.nextFloat()*.5f);if(RubbleVisualBudget.claim(p)){mc.particleEngine.add(p);PARTICLES.add(p);}}if(RubbleClientConfig.DUST.get()&&PARTICLES.size()<RubbleClientConfig.PARTICLES.get()){var frames=ExplosionVfxClient.hgSprite(2);if(frames!=null){var p=new RubbleDust(l,Vec3.atCenterOf(s.pos()),frames,random);if(RubbleVisualBudget.claim(p)){mc.particleEngine.add(p);PARTICLES.add(p);}}}}
    }
    private static final class RubbleDust extends TextureSheetParticle {
        RubbleDust(net.minecraft.client.multiplayer.ClientLevel l,Vec3 p,SpriteSet sprites,Random r){super(l,p.x,p.y,p.z);setSprite(sprites.get(8+r.nextInt(6),15));lifetime=35+r.nextInt(30);quadSize=.45f+r.nextFloat()*.6f;alpha=0;rCol=.34f;gCol=.33f;bCol=.30f;xd=(r.nextDouble()-.5)*.025;yd=.012;zd=(r.nextDouble()-.5)*.025;roll=oRoll=r.nextFloat()*6.28f;hasPhysics=false;}
        @Override public void tick(){xo=x;yo=y;zo=z;oRoll=roll;if(++age>=lifetime){remove();return;}x+=xd;y+=yd;z+=zd;quadSize*=1.012f;roll+=.004f;alpha=.32f*Math.min(1,age/5f)*Math.min(1,(lifetime-age)/18f);}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
    private RubbleClient(){}
}
