package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RecoveringConfigTomlTest {
 @Test void validDocumentDoesNotNeedRecovery(){var r=RecoveringConfigToml.parse("# Keep\n[world]\nrange=10000\nids=[\"a:b\",\"c:d\"]\n");assertTrue(r.notices().isEmpty());assertEquals(10000,((Number)r.config().get("world.range")).intValue());assertEquals(List.of("a:b","c:d"),r.config().get("world.ids"));}
 @Test void malformedValuePreservesIndependentFieldsAndSections(){var r=RecoveringConfigToml.parse("before=12\n[world]\nbroken=???\nrange=10000\n[client]\ngain=0.25\n");assertFalse(r.notices().isEmpty());assertEquals(12,((Number)r.config().get("before")).intValue());assertEquals(10000,((Number)r.config().get("world.range")).intValue());assertEquals(.25,((Number)r.config().get("client.gain")).doubleValue());}
 @Test void unclosedQuoteDoesNotEraseFollowingSettings(){var r=RecoveringConfigToml.parse("broken=\"unfinished\nrange=10000\ngain=0.25\n");assertEquals(10000,((Number)r.config().get("range")).intValue());assertEquals(.25,((Number)r.config().get("gain")).doubleValue());}
 @Test void incompleteArrayDoesNotEraseFollowingSettings(){var r=RecoveringConfigToml.parse("broken=[\n \"one\",\nrange=10000\n");assertEquals(10000,((Number)r.config().get("range")).intValue());assertFalse(r.config().contains("broken"));}
 @Test void validMultilineArraySurvivesUnrelatedSyntaxError(){var r=RecoveringConfigToml.parse("ids=[\n\"minecraft:stone\",\n\"minecraft:dirt\"\n]\nbroken=???\ngain=0.25\n");assertEquals(List.of("minecraft:stone","minecraft:dirt"),r.config().get("ids"));assertEquals(.25,((Number)r.config().get("gain")).doubleValue());}
 @Test void validMultilineStringCanContainAssignmentLookingText(){var r=RecoveringConfigToml.parse("text=\"\"\"first\nfake=inside\nlast\"\"\"\nbroken=???\nafter=9\n");assertTrue(r.config().<String>get("text").contains("fake=inside"));assertFalse(r.config().contains("fake"));assertEquals(9,((Number)r.config().get("after")).intValue());}
 @Test void unterminatedTripleStringStillRecoversLaterIndependentValues(){var r=RecoveringConfigToml.parse("before=4\nbroken=\"\"\"unfinished\nignored body\nafter=17\n");assertEquals(4,((Number)r.config().get("before")).intValue());assertEquals(17,((Number)r.config().get("after")).intValue());assertFalse(r.config().contains("broken"));}
 @Test void largeBrokenListDoesNotNeedRecursiveRecovery(){var s=new StringBuilder("before=1\nbroken=[\n");for(int i=0;i<10000;i++)s.append("\"minecraft:item_").append(i).append("\",\n");s.append("after=2\n");var r=RecoveringConfigToml.parse(s.toString());assertEquals(1,((Number)r.config().get("before")).intValue());assertEquals(2,((Number)r.config().get("after")).intValue());}
}
