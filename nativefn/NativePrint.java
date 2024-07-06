package nativefn;

public class NativePrint extends NativeFn {
  //NativePrint()
  public NativePrint() {
    this.arity = 1;
  }

  //execute(Object[])
  public Object execute(Object[] args) {
    Object o = args[0];

    if (o instanceof Number) {
      double d = (double)o;

      if (d == (long)d)
        System.out.print(String.format("%d", (long)d));
      else
        System.out.print(String.format("%s", d));
    } else
      System.out.print(o);

    return null;
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: print>";
  }
}
