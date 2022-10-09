package jblox.nativefn;

public class NativePrintLn extends NativeFn {
  //NativePrintLn()
  public NativePrintLn() {
    this.arity = -1; //variadic, 0 or 1 args
  }

  //execute(Object[])
  public Object execute(Object[] args) {
    if (args.length == 0)
      System.out.println();
    else if (args.length == 1) {
      Object o = args[0];

      if (o instanceof Number) {
        double d = (double)o;

        if (d == (long)d)
          System.out.println(String.format("%d", (long)d));
        else
          System.out.println(String.format("%s", d));
      } else
        System.out.println(o);
    }

    return null;
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: println>";
  }
}
