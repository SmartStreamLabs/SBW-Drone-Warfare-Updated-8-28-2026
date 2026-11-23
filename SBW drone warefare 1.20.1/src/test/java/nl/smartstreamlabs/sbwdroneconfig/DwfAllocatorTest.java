package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class DwfAllocatorTest{
 @Test void broadIngredientsYieldStockToSpecificOnes(){var stock=List.of(new DwfAllocator.Stock<>("specific",1,3),new DwfAllocator.Stock<>("alternative",1,1));var plan=DwfAllocator.allocate(stock,new long[]{1,1});assertEquals(Map.of("specific",1L,"alternative",1L),plan);}
 @Test void overlappingTagsRequireResidualReassignment(){var stock=List.of(new DwfAllocator.Stock<>("a",3,3),new DwfAllocator.Stock<>("b",3,6),new DwfAllocator.Stock<>("c",3,1));assertEquals(Map.of("a",3L,"b",3L,"c",3L),DwfAllocator.allocate(stock,new long[]{3,3,3}));}
 @Test void absentIngredientDoesNotReturnPartialConsumption(){assertNull(DwfAllocator.allocate(List.of(new DwfAllocator.Stock<>("iron",64,1)),new long[]{8,1}));}
 @Test void highBitAndLongCountsRemainExact(){assertEquals(Map.of("lots",64000000L),DwfAllocator.allocate(List.of(new DwfAllocator.Stock<>("lots",64000000,1)),new long[]{64000000}));long[]needs=new long[64];Arrays.fill(needs,1);assertEquals(Map.of("all",64L),DwfAllocator.allocate(List.of(new DwfAllocator.Stock<>("all",64,-1L)),needs));}
}
