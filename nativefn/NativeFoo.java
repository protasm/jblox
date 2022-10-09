package jblox.nativefn;

public class NativeFoo extends NativeFn {
  //NativeFoo()
  public NativeFoo() {
    this.arity = 3;
  }

  //execute(Object[])
  public Object execute(Object[] args) {
    System.out.println("Did somebody say foo?");
    System.out.println("Foo1: " + args[0]);
    System.out.println("Foo2: " + args[1]);
    System.out.println("Foo3: " + args[2]);

    return null;
  }

  //toString()
  @Override
  public String toString() {
    return "<nativefn: foo>";
  }
}
