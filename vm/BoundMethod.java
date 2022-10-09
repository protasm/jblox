package jblox.vm;

public class BoundMethod {
  private Object receiver;
  private Closure method;

  //BoundMethod(Object, Closure)
  public BoundMethod(Object receiver, Closure method) {
    this.receiver = receiver;
    this.method = method;
  }

  //receiver()
  public Object receiver() {
    return receiver;
  }

  //method()
  public Closure method() {
    return method;
  }

  //toString()
  @Override
  public String toString() {
    return method.function().toString();
  }
}
