package nl.smartstreamlabs.sbwdroneconfig;
/** Two index bytes leave a separate byte for degenerate-face normal flags. */
final class MeshPaletteIndex {
 static final int MAX_PARTS=4096;
 static int low(int index){check(index);return index&255;}
 static int high(int index){check(index);return index>>>8;}
 static int flags(boolean x,boolean y,boolean z){return(x?1:0)|(y?2:0)|(z?4:0);}
 private static void check(int index){if(index<0||index>=MAX_PARTS)throw new IllegalArgumentException("Mesh palette index outside budget");}
 private MeshPaletteIndex(){}
}
