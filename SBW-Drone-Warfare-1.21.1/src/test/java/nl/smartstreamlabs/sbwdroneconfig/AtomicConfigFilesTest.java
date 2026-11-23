package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class AtomicConfigFilesTest {
 @TempDir Path directory;
 private byte[] bytes(String text){return text.getBytes(StandardCharsets.UTF_8);}
 @Test void unchangedConfigDoesNotCreateBackup()throws Exception{
  Path p=directory.resolve("server.toml");assertTrue(AtomicConfigFiles.replace(p,bytes("items=64000000\n"),true));assertFalse(AtomicConfigFiles.replace(p,bytes("items=64000000\n"),true));try(var files=Files.list(directory)){assertEquals(1,files.count());}
 }
 @Test void migrationPreservesPreviousBytesAndCapsBackups()throws Exception{
  Path p=directory.resolve("server.toml");Files.writeString(p,"first\n# Keep this comment\n");AtomicConfigFiles.replace(p,bytes("second"),true);
  try(var files=Files.list(directory)){var backup=files.filter(f->f.toString().endsWith(".bak")).findFirst().orElseThrow();assertEquals("first\n# Keep this comment\n",Files.readString(backup));}
  Files.writeString(directory.resolve("another.toml.config-backup-unrelated.bak"),"other config");
  for(int i=0;i<7;i++)AtomicConfigFiles.replace(p,bytes("version"+i),true);
  assertEquals("version6",Files.readString(p));assertEquals("other config",Files.readString(directory.resolve("another.toml.config-backup-unrelated.bak")));
  try(var files=Files.list(directory)){assertEquals(3,files.filter(f->f.getFileName().toString().startsWith("sbwdroneconfig-config-backup-")).count());}
  try(var files=Files.list(directory)){assertFalse(files.anyMatch(f->f.toString().endsWith(".tmp")));}
 }
 @Test void ordinaryApplyDoesNotMakeMigrationBackup()throws Exception{
  Path p=directory.resolve("client.toml");Files.writeString(p,"volume=.5");AtomicConfigFiles.replace(p,bytes("volume=.25"),false);assertEquals("volume=.25",Files.readString(p));try(var files=Files.list(directory)){assertEquals(1,files.count());}
 }
 @Test void rejectedDestinationIsNotRemoved()throws Exception{
  Path p=Files.createDirectory(directory.resolve("occupied.toml"));Files.writeString(p.resolve("keep"),"existing");assertThrows(java.io.IOException.class,()->AtomicConfigFiles.replace(p,bytes("wrong"),true));assertEquals("existing",Files.readString(p.resolve("keep")));
 }
 @Test void backupBudgetIsSharedAcrossConfigFiles()throws Exception{
  for(int i=0;i<8;i++){Path p=directory.resolve("config"+i+".toml");Files.writeString(p,"original"+i);AtomicConfigFiles.replace(p,bytes("new"+i),true);}
  try(var files=Files.list(directory)){assertEquals(3,files.filter(f->f.getFileName().toString().startsWith("sbwdroneconfig-config-backup-")).count());}
  for(int i=0;i<8;i++)assertEquals("new"+i,Files.readString(directory.resolve("config"+i+".toml")));
 }
}
