package jblox.vm;

public class Upvalue {
  private int location;
  private Value closedValue;
  private Upvalue next;

  //Upvalue(int)
  public Upvalue(int location) {
    this.location = location;
  }

  //location()
  public int location() {
    return location;
  }

  //setLocation(int)
  public void setLocation(int location) {
    this.location = location;
  }

  //closedValue()
  public Value closedValue() {
    return closedValue;
  }

  //setClosedValue(Value)
  public void setClosedValue(Value closedValue) {
    this.closedValue = closedValue;
  }

  //next()
  public Upvalue next() {
    return next;
  }

  //setNext(Upvalue)
  public void setNext(Upvalue next) {
    this.next = next;
  }

  //toString()
  @Override
  public String toString() {
    if (closedValue != null)
      return closedValue.toString();
    else
      return Integer.toString(location);
  }
}
