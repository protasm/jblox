package jblox.util;

public class LoxIntArray extends LoxArray {
  private int[] elements;

  //LoxIntArray()
  public LoxIntArray() {
    super();

    elements = new int[0];
  }

  //get(int)
  public int get(int index) {
    return elements[index];
  }

  //set(int, int)
  public void set(int index, int element) {
    elements[index] = element;
  }

  //add(int)
  public int add(int element) {
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

      int[] newElements = new int[capacity];

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
      sb.append(elements[i] + ((i < count - 1) ? ", " : ""));

    sb.append("]");

    return sb.toString();
  }
}
