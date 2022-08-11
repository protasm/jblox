package jblox.util;

import java.util.Arrays;

import jblox.compiler.Local;

public class LoxLocalStack extends LoxStack {
  private Local[] elements;

  //LoxLocalStack()
  public LoxLocalStack() {
    super();

    elements = new Local[8192];
  }

  //peek()
  public Local peek() {
    return elements[count - 1];
  }

  //pop()
  public Local pop() {
    Local local = elements[count - 1];

    count--;

    return local;
  }

  //push(Local)
  public void push(Local local) {
    elements[count++] = local;
  }

  //get(int)
  public Local get(int index) {
    return elements[index];
  }

  //set(int, Local)
  public void set(int index, Local local) {
    elements[index] = local;
  }

  //reset()
  public void reset() {
    super.reset();

    elements = new Local[8192];
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
