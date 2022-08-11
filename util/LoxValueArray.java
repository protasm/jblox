package jblox.util;

import jblox.vm.Value;

public class LoxValueArray extends LoxArray {
  private Value[] elements;

  //LoxValueArray()
  public LoxValueArray() {
    super();

    elements = new Value[0];
  }

  //get(int)
  public Value get(int index) {
    return elements[index];
  }

  //set(int, Value)
  public void set(int index, Value element) {
    elements[index] = element;
  }

  //add(Value)
  public int add(Value element) {
    checkCapacity();

    elements[count++] = element;

    //return index of newly added element
    return count - 1;
  }

  //checkCapacity()
  private void checkCapacity() {
    if (capacity < count + 1) {
      //grow capacity
      capacity = capacity < 8 ? 8 : capacity * 2;

      Value[] newElements = new Value[capacity];

      //copy elements into new, larger array
      System.arraycopy(elements, 0, newElements, 0, count);

      elements = newElements;
    }
  }

  //toString()
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");

    for (int i = 0; i < count; i++)
      sb.append(elements[i] + (i < count - 1 ? ", " : ""));

    sb.append("]");

    return sb.toString();
  }
}
