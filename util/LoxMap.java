package jblox.util;

import java.util.HashMap;
import java.util.Map;

public class LoxMap {
  private Map<String, Object> elements;

  //LoxMap
  public LoxMap() {
    this.elements = new HashMap<>();
  }

  //containsKey(String)
  public boolean containsKey(String key) {
    return elements.containsKey(key);
  }

  //get(String)
  public Object get(String key) {
    return elements.get(key);
  }

  //put(String, Object)
  public void put(String key, Object value) {
    elements.put(key, value);
  }

  //toString
  @Override
  public String toString() {
    if (elements.size() == 0)
      return "[ empty ]";
    else {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, Object> entry : elements.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();

        sb.append("[ \"" + key + "\"=" + value + " ]");
      }

      return sb.toString();
    }
  }
}
