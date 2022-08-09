package jblox.nativefn;

public abstract class NativeFn {
  public abstract Object execute(Object[] args);

  //toString()
  public String toString() {
    return "<nativefn>";
  }
}
