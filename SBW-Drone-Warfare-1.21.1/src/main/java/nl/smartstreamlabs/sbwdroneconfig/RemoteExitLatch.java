package nl.smartstreamlabs.sbwdroneconfig;
/** One exit per rising edge; the click that opened control must first be released. */
final class RemoteExitLatch {
 private boolean held=true;
 void reset(){held=true;}
 boolean update(boolean down){boolean pressed=down&&!held;held=down;return pressed;}
}
