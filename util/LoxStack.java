package jblox.util;

public class LoxStack {
  private Object[] elements;
  private int count;

  //LoxStack()
  public LoxStack(int capacity) {
    elements = new Object[capacity];
    count = 0;
  }

  //count()
  public int count() {
    return count;
  }

  //top()
  public int top() {
    return count - 1;
  }

  //peek()
  public Object peek() {
    return elements[count - 1];
  }

  //pop()
  public Object pop() {
    Object element = elements[count - 1];

    count--;

    return element;
  }

  //push(Object)
  public void push(Object element) {
    elements[count++] = element;
  }

  //peek(int, boolean)
  public Object[] peek(int num, boolean flip) {
    if (num == 0)
      return new Object[0];
    else if (num == 1)
      return new Object[]{ peek() };
    else {
      Object[] subelements = new Object[num];

      for (int i = 0; i < num; i++)
        if (flip)
          subelements[i] = elements[count - num + i];
        else
          subelements[i] = elements[count - 1 - i];

      return subelements;
    }
  }

  //pop(int, boolean)
  public Object[] pop(int num, boolean flip) {
    if (num == 0)
      return new Object[0];
    else if (num == 1)
      return new Object[] { pop() };
    else {
      Object[] subelements = peek(num, flip);

      count = count - num;

      return subelements;
    }
  }

  //push(Object[])
  public void push(Object[] elements) {
    for (int i = 0; i < elements.length; i++)
      push(elements[i]);
  }

  //get(int)
  public Object get(int index) {
    return elements[index];
  }

  //set(int, Object)
  public void set(int index, Object element) {
    elements[index] = element;
  }

  //reset()
  public void reset() {
    count = 0;
  }

  //truncate(int)
  public void truncate(int index) {
    count = index;
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
