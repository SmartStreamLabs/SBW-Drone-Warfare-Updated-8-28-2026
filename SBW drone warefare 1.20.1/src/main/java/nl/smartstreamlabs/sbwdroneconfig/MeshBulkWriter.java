package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;
import java.lang.reflect.Field;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.nio.*;
/** Forge's native bulk append, guarded by the exact standard entity vertex layout.
 * Other consumers, outlines and modified formats retain normal VertexConsumer submission.
 */
final class MeshBulkWriter {
 // Resolve both development and production field names once; no reflection searches per frame.
 private static final Field FORMAT=field("format","f_85658_");
 private static final Field MODE=field("mode","f_85657_");
 private static Field field(String... names){for(String name:names)try{Field f=BufferBuilder.class.getDeclaredField(name);if(f.trySetAccessible())return f;}catch(NoSuchFieldException ignored){}return null;}
 private static boolean standardLayout(BufferBuilder builder){if(FORMAT==null||MODE==null)return false;try{return FORMAT.get(builder)==DefaultVertexFormat.NEW_ENTITY&&MODE.get(builder)==VertexFormat.Mode.QUADS;}catch(IllegalAccessException e){throw new IllegalStateException("Previously accessible mesh buffer layout became inaccessible",e);}}
 private static final ThreadLocal<ByteBuffer> SCRATCH=ThreadLocal.withInitial(()->ByteBuffer.allocateDirect(1024).order(ByteOrder.LITTLE_ENDIAN));
 static boolean draw(VertexConsumer consumer,MeshRenderData data,Matrix4f pose,Matrix3f normal,int light,int overlay,float r,float g,float b,float a){
  if(ByteOrder.nativeOrder()!=ByteOrder.LITTLE_ENDIAN || !(consumer instanceof BufferBuilder builder) || !standardLayout(builder))return false;
  int size=data.values.length/23*4*36;ByteBuffer bytes=SCRATCH.get();
  if(bytes.capacity()<size){bytes=ByteBuffer.allocateDirect(Integer.highestOneBit(size-1)<<1).order(ByteOrder.LITTLE_ENDIAN);SCRATCH.set(bytes);}
  bytes.clear();encode(bytes,data,pose,normal,light,overlay,r,g,b,a);builder.putBulkData(bytes);return true;
 }
 static byte normalByte(float v){return (byte)(int)(Math.max(-1,Math.min(1,v))*127);}
 static void encode(ByteBuffer out,MeshRenderData data,Matrix4f m,Matrix3f n,int light,int overlay,float red,float green,float blue,float alpha){
  int color=((int)(red*255)&255)|(((int)(green*255)&255)<<8)|(((int)(blue*255)&255)<<16)|(((int)(alpha*255)&255)<<24);var v=data.values;
  for(int at=0;at<v.length;at+=23){float a=v[at],b=v[at+1],c=v[at+2];
   float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;
   if(data.flipX&&nx<0)nx=-nx;if(data.flipY&&ny<0)ny=-ny;if(data.flipZ&&nz<0)nz=-nz;
   int packedNormal=(normalByte(nx)&255)|((normalByte(ny)&255)<<8)|((normalByte(nz)&255)<<16);
   for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];
    out.putFloat(m.m00()*x+m.m10()*y+m.m20()*z+m.m30());out.putFloat(m.m01()*x+m.m11()*y+m.m21()*z+m.m31());out.putFloat(m.m02()*x+m.m12()*y+m.m22()*z+m.m32());
    out.putInt(color).putFloat(v[i+3]).putFloat(v[i+4]).putInt(overlay).putInt(light).putInt(packedNormal);
   }
  }out.flip();
 }
}
