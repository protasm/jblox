package jblox.util;

public abstract class LoxArray {
  protected int capacity;
  protected int count;

  //LoxArray()
  public LoxArray() {
    reset();
  }

  //capacity()
  public int capacity() {
    return capacity;
  }

  //count()
  public int count() {
    return count;
  }

  //reset()
  public void reset() {
    capacity = 0;
    count = 0;
  }
}
