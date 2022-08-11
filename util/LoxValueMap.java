package jblox.util;

import java.util.HashMap;
import java.util.Map;

import jblox.vm.Value;

public class LoxValueMap implements LoxMap {
  private Map<String, Value> entries;

  //LoxValueMap
  public LoxValueMap() {
    this.entries = new HashMap<>();
  }

  //containsKey(String)
  public boolean containsKey(String key) {
    return entries.containsKey(key);
  }

  //get(String)
  public Value get(String key) {
    return entries.get(key);
  }

  //put(String, Value)
  public void put(String key, Value value) {
    entries.put(key, value);
  }

  //toString
  @Override
  public String toString() {
    if (entries.size() == 0)
      return "[ empty ]";
    else {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, Value> entry : entries.entrySet()) {
        String key = entry.getKey();
        Value value = entry.getValue();

        sb.append("[ \"" + key + "\"=" + value + " ]");
      }

      return sb.toString();
    }
  }
}
