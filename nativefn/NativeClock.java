package jblox.nativefn;

import jblox.vm.Value;
import jblox.vm.Value.ValueType;

public class NativeClock extends NativeFn {
  //execute(Value[])
  public Value execute(Value[] args) {
    //Long clock = System.nanoTime() / 1000000000;
    Long clock = System.currentTimeMillis();

    //return new Value(ValueType.VAL_NUMBER, (double)clock);
    return new Value((double)clock);
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: clock>";
  }
}
