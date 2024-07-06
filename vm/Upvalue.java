package vm;

public class Upvalue {
  private int location;
  private Object closedValue;
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
  public Object closedValue() {
    return closedValue;
  }

  //setClosedValue(Object)
  public void setClosedValue(Object closedValue) {
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
