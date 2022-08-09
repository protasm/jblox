package jblox.util;

import java.util.ArrayList;
import java.util.List;

public class LoxArray<E> {
  private List<E> elements;

  //LoxArray()
  public LoxArray() {
    elements = new ArrayList<>();
  }

  //count()
  public int count() {
    return elements.size();
  }

  //get(int)
  public E get(int index) {
    return elements.get(index);
  }

  //set(int, E)
  public void set(int index, E element) {
    elements.set(index, element);
  }

  //add(E)
  public int add(E element) {
    elements.add(element);

    //return index of newly added element
    return elements.size() - 1;
  }

  //toString()
  @Override
  public String toString() {
    return elements.toString();
  }
}
