package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import java.lang.reflect.*;
import java.util.*;
class FastMeshRendererTest {
 @org.junit.jupiter.api.BeforeAll static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @SuppressWarnings("unchecked") private static <T> T defaults(Class<T> type){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},(proxy,method,args)->InvocationHandler.invokeDefault(proxy,method,args));}
 private static VertexConsumer capture(List<Object[]> target){return (VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class[]{VertexConsumer.class},(proxy,method,args)->{if(method.getName().equals("vertex")&&args!=null&&args.length==14)target.add(args.clone());return method.getReturnType()==VertexConsumer.class?proxy:null;});}
 private static GeoCube cube(boolean flat){
  var a=new GeoVertex(new Vector3f(.1f,.2f,.3f),.1f,.2f);var b=new GeoVertex(new Vector3f(.7f,.2f,.3f),.8f,.2f);var c=new GeoVertex(new Vector3f(.7f,.8f,.4f),.8f,.9f);
  return new GeoCube(new GeoQuad[]{new GeoQuad(new GeoVertex[]{a,b,c,c},new Vector3f(-.4f,-.6f,-.2f),Direction.NORTH)},new Vec3(.2,.3,.4),new Vec3(.1,.2,.3),new Vec3(1,flat?0:1,1),0,false);
 }
 private static PoseStack pose(int yaw){var p=new PoseStack();p.translate(15.2,-7.1,18);p.mulPose(Axis.YP.rotationDegrees(yaw));p.mulPose(Axis.XP.rotationDegrees(-18));p.mulPose(Axis.ZP.rotationDegrees(13));p.scale(1.2f,.8f,-1);return p;}
 @Test void packedMeshMatchesGeckoVertexStreamUnderRotationScaleAndFlatNormalCorrection(){
  for(boolean flat:new boolean[]{false,true})for(int yaw:new int[]{0,90,180,270}){
   GeoCube c=cube(flat);MeshRenderData.register(c);var expected=new ArrayList<Object[]>();var actual=new ArrayList<Object[]>();
   GeoRenderer<GeoAnimatable> original=defaults(GeoRenderer.class);FastMeshRenderer<GeoAnimatable> fast=defaults(FastMeshRenderer.class);
   original.renderCube(pose(yaw),c,capture(expected),0x00c000d0,0x00010002,.6f,.7f,.8f,.9f);
   fast.renderCube(pose(yaw),c,capture(actual),0x00c000d0,0x00010002,.6f,.7f,.8f,.9f);
   assertEquals(4,expected.size());assertEquals(expected.size(),actual.size());
   for(int i=0;i<expected.size();i++)for(int k=0;k<expected.get(i).length;k++){Object a=expected.get(i)[k],b=actual.get(i)[k];if(a instanceof Float f)assertEquals(f,(Float)b,Math.max(0.00001f,Math.abs(f)*0.000001f),"vertex attribute "+k);else assertEquals(a,b);}
  }
 }
 @Test void packingRetainsTriangleDuplicateVertexAndUvCoordinates(){var c=cube(false);MeshRenderData.register(c);var data=MeshRenderData.get(c);assertSame(data,MeshRenderData.get(c));assertEquals(23,data.values.length);for(int i=0;i<5;i++)assertEquals(data.values[13+i],data.values[18+i]);}

 @Test void bulkEntityLayoutMatchesReferenceVertices(){
  var c=cube(false);MeshRenderData.register(c);var expected=new ArrayList<Object[]>();
  GeoRenderer<GeoAnimatable> original=defaults(GeoRenderer.class);original.renderCube(pose(90),c,capture(expected),0x00c000d0,0x00010002,.6f,.7f,.8f,.9f);
  var stack=pose(90);software.bernie.geckolib.util.RenderUtils.translateToPivotPoint(stack,c);software.bernie.geckolib.util.RenderUtils.rotateMatrixAroundCube(stack,c);software.bernie.geckolib.util.RenderUtils.translateAwayFromPivotPoint(stack,c);
  var bytes=java.nio.ByteBuffer.allocate(4*36).order(java.nio.ByteOrder.LITTLE_ENDIAN);
  MeshBulkWriter.encode(bytes,MeshRenderData.get(c),stack.last().pose(),stack.last().normal(),0x00c000d0,0x00010002,.6f,.7f,.8f,.9f);
  assertEquals(144,bytes.remaining());
  for(int i=0;i<4;i++){var v=expected.get(i);for(int k=0;k<3;k++)assertEquals((Float)v[k],bytes.getFloat(),.00001f);for(int k=3;k<7;k++)assertEquals((byte)(int)((Float)v[k]*255),bytes.get());assertEquals((Float)v[7],bytes.getFloat());assertEquals((Float)v[8],bytes.getFloat());assertEquals(v[9],bytes.getInt());assertEquals(v[10],bytes.getInt());for(int k=11;k<14;k++)assertEquals((byte)(int)(Math.max(-1,Math.min(1,(Float)v[k]))*127),bytes.get());assertEquals(0,bytes.get());}
 }
}
