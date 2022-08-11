package jblox.util;

import java.util.Arrays;

import jblox.vm.Value;

public class LoxValueStack extends LoxStack {
  private Value[] elements;

  //LoxValueStack()
  public LoxValueStack() {
    super();

    elements = new Value[8192];
  }

  //peek()
  public Value peek() {
    return elements[count - 1];
  }

  //pop()
  public Value pop() {
    Value value = elements[count - 1];

    count--;

    return value;
  }

  //push(Value)
  public void push(Value value) {
    elements[count++] = value;
  }

  //get(int)
  public Value get(int index) {
    return elements[index];
  }

  //set(int, Value)
  public void set(int index, Value value) {
    elements[index] = value;
  }

  //reset()
  public void reset() {
    super.reset();

    elements = new Value[8192];
  }

  //toString()
  @Override
  public String toString() {
    if (count == 0)
      return "[ empty ]";
    else
      return Arrays.toString(Arrays.copyOfRange(elements, 0, count));
  }
}
