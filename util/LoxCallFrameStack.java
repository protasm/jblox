package jblox.util;

import jblox.vm.CallFrame;

public class LoxCallFrameStack extends LoxStack {
  private CallFrame[] elements;

  //LoxCallFrameStack()
  public LoxCallFrameStack() {
    super();

    elements = new CallFrame[8192];
  }

  //peek()
  public CallFrame peek() {
    return elements[count - 1];
  }

  //pop()
  public CallFrame pop() {
    CallFrame frame = elements[count - 1];

    count--;

    return frame;
  }

  //push(CallFrame)
  public void push(CallFrame frame) {
    elements[count++] = frame;
  }

  //get(int)
  public CallFrame get(int index) {
    return elements[index];
  }

  //set(int, CallFrame)
  public void set(int index, CallFrame frame) {
    elements[index] = frame;
  }

  //reset()
  public void reset() {
    super.reset();

    elements = new CallFrame[8192];
  }

  //toString()
  @Override
  public String toString() {
    if (count == 0)
      return "[ empty ]";
    else {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < count; i++)
        sb.append("[ " + elements[i] + " ]");

      return sb.toString();
    }
  }
}
