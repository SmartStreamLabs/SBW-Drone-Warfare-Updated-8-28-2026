package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
public final class DwfIndexCheck{
 public static void main(String[]args)throws Exception{
  var index=new DwfIndex<Integer>(k->k%10);for(int i=0;i<65536;i++)index.update(i,i+1,"Item "+String.format("%06d",i),"Group "+(i%10));
  var snapshot=index.search(DwfIndex.Sort.NAME);var relevant=index.item(3);int count=0;while(relevant.hasNext()){if(relevant.next().key()%10!=3)throw new AssertionError();count++;}if(count!=6554)throw new AssertionError("Item-kind index must exclude irrelevant variants");
  for(int i=0;i<65536;i+=2)index.update(i,0,"","");for(int i=1;i<65536;i+=2)index.update(i,1,"Item "+String.format("%06d",i),"Group "+(i%10));
  count=0;while(snapshot.hasNext()){var row=snapshot.next();if(row.key()!=count||row.count()!=count+1)throw new AssertionError("Point-in-time index changed during edits");count++;}if(count!=65536||index.size()!=32768)throw new AssertionError();
  var current=index.search(DwfIndex.Sort.NAME);int key=1;while(current.hasNext()){if(current.next().key()!=key)throw new AssertionError("Name order");key+=2;}
  index.update(65535,1000000,"Item 065535","Group 5");if(index.search(DwfIndex.Sort.AMOUNT).next().key()!=65535)throw new AssertionError("Quantity index updated incrementally");
  var shared=index.search(DwfIndex.Sort.CATEGORY);final Throwable[]failure={null};Thread reader=new Thread(()->{try{String last="";while(shared.hasNext()){var row=shared.next();String category=row.category();if(category.compareTo(last)<0)throw new AssertionError("Category order");last=category;}}catch(Throwable e){failure[0]=e;}});reader.start();for(int i=1;i<10000;i+=2)index.update(i,0,"","");reader.join();if(failure[0]!=null)throw new AssertionError(failure[0]);
  System.out.println("PASS 65536-variant incremental name/amount/category/item-kind indices, frozen queries and safe concurrent background iteration");
 }
}
