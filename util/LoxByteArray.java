package jblox.util;

public class LoxByteArray {
  private byte[] elements;
  private int capacity;
  private int count;

  //LoxByteArray()
  public LoxByteArray() {
    elements = new byte[0];
    capacity = 0;
    count = 0;
  }

  //count()
  public int count() {
    return count;
  }

  //get(int)
  public byte get(int index) {
    return elements[index];
  }

  //set(int, byte)
  public void set(int index, byte element) {
    elements[index] = element;
  }

  //add(byte)
  public int add(byte element) {
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

      byte[] newElements = new byte[capacity];

      //copy elements into new, larger array
      System.arraycopy(elements, 0, newElements, 0, count);

      elements = newElements;
    }
  }

  //toString()
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");

    for (int i = 0; i < count; i++) {
      sb.append(String.format("%02X", elements[i]));

      if (i < count - 1) sb.append(", ");
    }

    sb.append("]");

    return sb.toString();
  }
}
