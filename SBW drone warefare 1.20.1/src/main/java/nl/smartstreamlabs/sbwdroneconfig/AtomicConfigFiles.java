package nl.smartstreamlabs.sbwdroneconfig;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.*;

/** Pure file I/O. Call with already validated bytes, outside render/tick loops. */
public final class AtomicConfigFiles {
 private AtomicConfigFiles(){}
 public static synchronized boolean replace(Path target,byte[] bytes,boolean migrationBackup)throws IOException {
  target=target.toAbsolutePath().normalize();Path parent=target.getParent();Files.createDirectories(parent);
  byte[] old=Files.exists(target)?Files.readAllBytes(target):null;
  if(old!=null&&Arrays.equals(old,bytes))return false;
  if(migrationBackup&&old!=null){
   Path backup=Files.createTempFile(parent,"sbwdroneconfig-config-backup-"+target.getFileName()+"-",".bak");
   try{writeSynced(backup,old);}catch(IOException e){Files.deleteIfExists(backup);throw e;}
   prune(parent,"sbwdroneconfig-config-backup-");
  }
  Path temporary=Files.createTempFile(parent,target.getFileName()+".pending-",".tmp");
  try{writeSynced(temporary,bytes);Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
  finally{Files.deleteIfExists(temporary);}
  return true;
 }
 private static void writeSynced(Path path,byte[] bytes)throws IOException {
  try(var c=FileChannel.open(path,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)){var buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())c.write(buffer);c.force(true);}
 }
 private static void prune(Path parent,String prefix)throws IOException {
  var backups=new ArrayList<Path>();try(var files=Files.list(parent)){files.filter(p->p.getFileName().toString().startsWith(prefix)&&p.getFileName().toString().endsWith(".bak")&&Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)).forEach(backups::add);}
  backups.sort(Comparator.<Path>comparingLong(p->{try{return Files.getLastModifiedTime(p).toMillis();}catch(IOException e){throw new java.io.UncheckedIOException(e);}}).thenComparing(p->p.getFileName().toString()));
  for(int i=0;i<backups.size()-3;i++)Files.delete(backups.get(i));
 }
}
