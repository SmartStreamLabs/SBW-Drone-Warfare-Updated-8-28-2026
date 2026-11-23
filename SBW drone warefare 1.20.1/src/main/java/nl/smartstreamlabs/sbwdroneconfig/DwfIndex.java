package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.util.function.Function;

/** Immutable-tree snapshots for worker search/sort without copying or locking an
 * entire storage on the server thread. Updated only by variant/count deltas. */
public final class DwfIndex<K>{
 public enum Sort{NAME,AMOUNT,CATEGORY}
 public record Row<K>(long id,K key,String name,String category,String search,long count){}
 private record Node<K>(Row<K> row,int priority,Node<K> left,Node<K> right){}
 private final Map<K,Row<K>> rows=new HashMap<>();private final Map<Object,Node<K>> kinds=new HashMap<>();
 private final Function<K,Object> kind;private long sequence;private Node<K>name,amount,category;
 public DwfIndex(Function<K,Object>kind){this.kind=kind;}
 private Comparator<Row<K>> order(Sort sort){return switch(sort){case NAME->Comparator.<Row<K>,String>comparing(Row::name,String.CASE_INSENSITIVE_ORDER).thenComparingLong(Row::id);case AMOUNT->Comparator.<Row<K>>comparingLong(Row::count).reversed().thenComparingLong(Row::id);case CATEGORY->Comparator.<Row<K>,String>comparing(Row::category).thenComparing(Row::name,String.CASE_INSENSITIVE_ORDER).thenComparingLong(Row::id);};}
 public void update(K key,long count,String label,String group){
  if(count<0)throw new IllegalArgumentException();var old=rows.get(key);if(old!=null){name=remove(name,old,order(Sort.NAME));amount=remove(amount,old,order(Sort.AMOUNT));category=remove(category,old,order(Sort.CATEGORY));}
  Object type=kind.apply(key);var byId=Comparator.<Row<K>>comparingLong(Row::id);
  if(count==0){if(old!=null){kinds.compute(type,(k,root)->remove(root,old,byId));rows.remove(key);}return;}
  var row=new Row<>(old==null?++sequence:old.id(),key,label,group,(label+" "+group).toLowerCase(Locale.ROOT),count);rows.put(key,row);
  name=put(name,row,order(Sort.NAME));amount=put(amount,row,order(Sort.AMOUNT));category=put(category,row,order(Sort.CATEGORY));
  if(old==null)kinds.put(type,put(kinds.get(type),row,byId));
 }
 public int size(){return rows.size();}public long count(K key){var row=rows.get(key);return row==null?0:row.count();}
 public Cursor<K> search(Sort sort){return new Cursor<>(switch(sort){case NAME->name;case AMOUNT->amount;case CATEGORY->category;});}
 public Cursor<K> item(Object type){return new Cursor<>(kinds.get(type));}
 public static final class Cursor<K>{
  private final ArrayDeque<Node<K>> stack=new ArrayDeque<>();private Cursor(Node<K>root){push(root);}
  private void push(Node<K>node){while(node!=null){stack.push(node);node=node.left();}}
  public boolean hasNext(){return !stack.isEmpty();}
  public Row<K> next(){var n=stack.pop();push(n.right());return n.row();}
 }
 private static int priority(long id){long v=id+0x9e3779b97f4a7c15L;v=(v^(v>>>30))*0xbf58476d1ce4e5b9L;v=(v^(v>>>27))*0x94d049bb133111ebL;return (int)(v^(v>>>31));}
 private Node<K> put(Node<K>node,Row<K>row,Comparator<Row<K>>order){
  if(node==null)return new Node<>(row,priority(row.id()),null,null);int comparison=order.compare(row,node.row());
  if(comparison==0)return new Node<>(row,node.priority(),node.left(),node.right());
  if(comparison<0){var left=put(node.left(),row,order);var result=new Node<>(node.row(),node.priority(),left,node.right());return left.priority()>node.priority()?new Node<>(left.row(),left.priority(),left.left(),new Node<>(result.row(),result.priority(),left.right(),result.right())):result;}
  var right=put(node.right(),row,order);var result=new Node<>(node.row(),node.priority(),node.left(),right);return right.priority()>node.priority()?new Node<>(right.row(),right.priority(),new Node<>(result.row(),result.priority(),result.left(),right.left()),right.right()):result;
 }
 private Node<K> remove(Node<K>node,Row<K>row,Comparator<Row<K>>order){if(node==null)return null;int comparison=order.compare(row,node.row());if(comparison<0)return new Node<>(node.row(),node.priority(),remove(node.left(),row,order),node.right());if(comparison>0)return new Node<>(node.row(),node.priority(),node.left(),remove(node.right(),row,order));return merge(node.left(),node.right());}
 private Node<K> merge(Node<K>a,Node<K>b){if(a==null)return b;if(b==null)return a;return a.priority()>b.priority()?new Node<>(a.row(),a.priority(),a.left(),merge(a.right(),b)):new Node<>(b.row(),b.priority(),merge(a,b.left()),b.right());}
}
