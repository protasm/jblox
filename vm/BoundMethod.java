package jblox.vm;

public class BoundMethod {
  private Value receiver;
  private Closure method;

  //BoundMethod(Value, Closure)
  public BoundMethod(Value receiver, Closure method) {
    this.receiver = receiver;
    this.method = method;
  }

  //receiver()
  public Value receiver() {
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
