package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.security.MessageDigest;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;
class ModLogoAssetTest {
 @Test void selectedLogoIsPackagedAsReadablePng() throws Exception {
  try(var stream=getClass().getClassLoader().getResourceAsStream("cacc.png")) {
   assertNotNull(stream,"The logo resource referenced by mod metadata must exist.");
   var image=ImageIO.read(stream);assertNotNull(image,"The selected logo must decode as PNG.");
   assertEquals(1536,image.getWidth());assertEquals(1024,image.getHeight());
  }
 }
 @Test void packagedLogoMatchesTheExactUserSelectedFile() throws Exception {
  try(var stream=getClass().getClassLoader().getResourceAsStream("cacc.png")) {
   assertNotNull(stream);
   assertEquals("7fb8d74b6edb2ccfe4502a60f1233a332deca97b45da1d7b0cc16aaabcb1c5d8",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())),"Do not crop, recolor, regenerate or substitute the selected logo.");
  }
 }
}
