package vm;

import compiler.Function;

public class Closure {
  private Function function;
  private Upvalue[] upvalues;

  //Closure()
  public Closure(Function function) {
    this.function = function;

    upvalues = new Upvalue[function.upvalueCount()];
  }

  //function()
  public Function function() {
    return function;
  }

  //upvalues()
  public Upvalue[] upvalues() {
    return upvalues;
  }

  //upvalueCount()
  public int upvalueCount() {
    return function.upvalueCount();
  }

  //toString()
  @Override
  public String toString() {
    return function.toString();
  }
}
