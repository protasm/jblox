package jblox.nativefn;

public class NativeClock extends NativeFn {
  //execute(Object[])
  public Object execute(Object[] args) {
    return (System.nanoTime() / 1000000000);
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: clock>";
  }
}
