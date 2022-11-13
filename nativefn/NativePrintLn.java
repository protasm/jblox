package jblox.nativefn;

public class NativePrintLn extends NativeFn {
  //NativePrintLn()
  public NativePrintLn() {
    this.arity = -1; //variadic, 0 or 1 args
  }

  //execute(Object[])
  public Object execute(Object[] args) {
    if (args.length == 1)
      new NativePrint().execute(args);

    System.out.println();

    return null;
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: println>";
  }
}
