package jblox.nativefn;

public class NativeClock extends NativeFn {
  //execute(Object[])
  public Object execute(Object[] args) {
    Long clock = System.currentTimeMillis();

    return (double)clock / 1000;
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: clock>";
  }
}
