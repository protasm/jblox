package jblox.util;

public abstract class LoxStack {
  protected int count;

  //LoxStack()
  public LoxStack() {
    count = 0;
  }

  //count()
  public int count() {
    return count;
  }

  //reset()
  public void reset() {
    count = 0;
  }

  //top()
  public int top() {
    return count - 1;
  }

  //truncate(int)
  public void truncate(int keep) {
    count = keep;
  }
}
