package jblox.nativefn;

import jblox.vm.Value;

public abstract class NativeFn {
  public abstract Value execute(Value[] args);

  //toString()
  @Override
  public String toString() {
    return "<nativefn>";
  }
}
