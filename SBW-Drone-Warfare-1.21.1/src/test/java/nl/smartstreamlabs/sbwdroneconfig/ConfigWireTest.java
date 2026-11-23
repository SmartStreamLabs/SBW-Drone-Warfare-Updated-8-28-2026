package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class ConfigWireTest {
 @Test void fullPrecisionNumericValuesRemainText(){assertEquals("9223372036854775807",ConfigWire.text(Long.MAX_VALUE));assertEquals("64000000",ConfigWire.text(64000000L));}
 @Test void multipartReassemblyIsExactAndDuplicateDeliveryDoesNotCompleteEarly(){String text="X".repeat(12000);var parts=ConfigWire.chunks(text);var a=new ConfigWire.Assembly(parts.size(),10);a.add(2,3,parts.get(2));a.add(2,3,parts.get(2));a.add(0,3,parts.get(0));assertFalse(a.complete());a.add(1,3,parts.get(1));assertEquals(text,a.finish());}
 @Test void conflictingDuplicatesAndMalformedHeadersAreRejected(){var a=new ConfigWire.Assembly(2,0);a.add(0,2,"first");assertThrows(IllegalArgumentException.class,()->a.add(0,2,"different"));assertThrows(IllegalArgumentException.class,()->a.add(1,3,"bad"));assertThrows(IllegalArgumentException.class,()->new ConfigWire.Assembly(ConfigWire.MAX_PARTS+1,0));assertThrows(IllegalStateException.class,a::finish);}
 @Test void emptyTransferStillHasOnePartAndExpiryIsBounded(){assertEquals(List.of(""),ConfigWire.chunks(""));var a=new ConfigWire.Assembly(1,50);assertFalse(a.expired(250));assertTrue(a.expired(251));}
 @Test void stringMapRejectsTypeCoercions(){var json=com.google.gson.JsonParser.parseString("{\"capacity\":9223372036854775807}").getAsJsonObject();assertThrows(IllegalArgumentException.class,()->ConfigWire.strings(json));var valid=com.google.gson.JsonParser.parseString("{\"capacity\":\"9223372036854775807\"}").getAsJsonObject();assertEquals("9223372036854775807",ConfigWire.strings(valid).get("capacity"));}
}
