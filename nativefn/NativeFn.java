package jblox.nativefn;

import jblox.compiler.HasArity;

public abstract class NativeFn implements HasArity {
  protected int arity = 0;

  //arity()
  @Override
  public int arity() {
    return arity;
  }

  public abstract Object execute(Object[] args);
  @Override public abstract String toString();
}
