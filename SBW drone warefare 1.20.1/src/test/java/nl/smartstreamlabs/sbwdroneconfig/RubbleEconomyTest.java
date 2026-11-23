package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class RubbleEconomyTest {
 @Test void everyStoredUnitHasTheSameYieldAfterReload(){for(int i=0;i<100;i++){var id=new UUID(i*91436717L,i*643743139L);assertEquals(RubbleEconomy.salvage(id,.25),RubbleEconomy.salvage(UUID.fromString(id.toString()),.25));}}
 @Test void disabledSalvageNeverProducesAnItem(){for(int i=0;i<100;i++)assertFalse(RubbleEconomy.salvage(new UUID(i,i),0));}
 @Test void fullRecoveryCannotExceedOneOutcomePerUnit(){for(int i=0;i<100;i++)assertTrue(RubbleEconomy.salvage(new UUID(i,-i),1));}
 @Test void visuallyMergedUnitsNeverMakeAnUnboundedCollision(){assertEquals(1,RubbleEconomy.layers(1));assertEquals(2,RubbleEconomy.layers(2));assertEquals(8,RubbleEconomy.layers(256));assertEquals(8,RubbleEconomy.layers(Integer.MAX_VALUE));}
 @Test void partialCleanupShrinksMonotonically(){int last=8;for(int i=256;i>0;i--){int current=RubbleEconomy.layers(i);assertTrue(current<=last);last=current;}}
}
