package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.util.*;

/** VC-01 replaces the old translucent five-face vehicle box. Ground nets are separate. */
public final class VehicleAntiDroneNetClient {
    private static final ResourceLocation METAL=new ResourceLocation(SbwDroneRangeConfig.MOD_ID,"textures/vc01/vc01_structure.png");
    private static final ResourceLocation NET=new ResourceLocation(SbwDroneRangeConfig.MOD_ID,"textures/vc01/vc01_net_welded.png");
    private static final Map<Integer,Long> ACTIVE_VEHICLE_NETS=new HashMap<>();
    private static final Map<Integer,Long> INACTIVE=new HashMap<>();
    private static final Map<Integer,net.minecraft.nbt.CompoundTag> HEALTH=new HashMap<>();
    private static ClientLevel world;
    private VehicleAntiDroneNetClient() {}
    public static void init(IEventBus bus){bus.addListener(VehicleAntiDroneNetClient::setup);}
    private static void setup(FMLClientSetupEvent e){MinecraftForge.EVENT_BUS.register(VehicleAntiDroneNetClient.class);}
    private static void world(ClientLevel current){if(world!=current){ACTIVE_VEHICLE_NETS.clear();INACTIVE.clear();HEALTH.clear();world=current;}}
    public static void handleVehicleNetState(VehicleAntiDroneNetStateMessage m){
        var level=Minecraft.getInstance().level;world(level);if(level==null)return;
        if(m.active()){if(!m.panels().isEmpty())HEALTH.put(m.entityId(),m.panels());ACTIVE_VEHICLE_NETS.put(m.entityId(),level.getGameTime()+40);INACTIVE.remove(m.entityId());}
        else{HEALTH.remove(m.entityId());ACTIVE_VEHICLE_NETS.remove(m.entityId());INACTIVE.put(m.entityId(),level.getGameTime()+40);}
    }
    @SubscribeEvent public static void clearWorldState(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)world(Minecraft.getInstance().level);}
    @SubscribeEvent public static void onRenderLevelStage(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;
        var mc=Minecraft.getInstance();var clientLevel=mc.level;world(clientLevel);
        if(clientLevel==null||!AddonConfig.vehicleAntiDroneNetEnabled())return;
        long now=clientLevel.getGameTime();
        ACTIVE_VEHICLE_NETS.entrySet().removeIf(x->x.getValue()<now||clientLevel.getEntity(x.getKey())==null);
        INACTIVE.entrySet().removeIf(x->x.getValue()<now||clientLevel.getEntity(x.getKey())==null);
        HEALTH.keySet().removeIf(id->!ACTIVE_VEHICLE_NETS.containsKey(id));
        var buffers=mc.renderBuffers().bufferSource();Vec3 camera=e.getCamera().getPosition();
        RenderType solid=RenderType.entityCutoutNoCull(METAL),net=RenderType.entityCutoutNoCull(NET);
        var poses=e.getPoseStack();float partial=e.getPartialTick();
        for(Entity entity:clientLevel.entitiesForRendering()){
            if(!SbwCompat.isVehicleNetHost(entity)||!entity.isAlive()||INACTIVE.containsKey(entity.getId()))continue;
            if(!ACTIVE_VEHICLE_NETS.containsKey(entity.getId())&&!SbwCompat.hasVehicleAntiDroneNetUpgrade(entity))continue;
            var state=HEALTH.remove(entity.getId());if(state!=null)Vc01Panels.receive(entity,state);
            var profile=Vc01Geometry.profile(entity);if(profile==null)continue;
            if(entity.position().distanceToSqr(camera)>192*192)continue;
            int light=mc.getEntityRenderDispatcher().getPackedLightCoords(entity,partial);
            boolean detailed=entity.position().distanceToSqr(camera)<40*40;
            for(var branch:profile.branches()){
                var transform=Vc01Geometry.matrix(entity,branch,partial);
                if(!e.getFrustum().isVisible(Vc01Geometry.bounds(branch,transform)))continue;
                Matrix4f local=new Matrix4f(transform);local.m30((float)(transform.m30()-camera.x));local.m31((float)(transform.m31()-camera.y));local.m32((float)(transform.m32()-camera.z));
                Matrix4f position=new Matrix4f(poses.last().pose()).mul(local);
                Matrix3f normal=new Matrix3f(position);
                for(boolean mesh:new boolean[]{false,true}){
                    var out=buffers.getBuffer(mesh?net:solid);
                    for(var f:branch.faces()){
                        if(f.net()!=mesh||!detailed&&f.detail())continue;
                        double fraction=f.net()?Vc01Panels.health(entity,f.panel()).fraction():1;
                        if(f.net()&&fraction<=0)continue;
                        int tint=(int)(255*(.35+.65*fraction));
                        // One shared buffer per material; immutable fitted faces are reused for every frame.
                        emit(out,position,normal,f.a(),f.normal(),light,tint);
                        emit(out,position,normal,f.b(),f.normal(),light,tint);
                        emit(out,position,normal,f.c(),f.normal(),light,tint);
                        emit(out,position,normal,f.c(),f.normal(),light,tint);
                    }
                }
            }
        }
        buffers.endBatch(solid);buffers.endBatch(net);
    }
    private static void emit(VertexConsumer out,Matrix4f pose,Matrix3f normal,Vc01Geometry.Vertex v,Vec3 n,int light,int tint){
        out.vertex(pose,(float)v.p().x,(float)v.p().y,(float)v.p().z).color(255,tint,tint,255).uv(v.u(),v.v()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal,(float)n.x,(float)n.y,(float)n.z).endVertex();
    }
}
