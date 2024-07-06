package vm;

import java.util.HashMap;
import java.util.Map;

public class LoxClass {
  private String name;
  private Map<String, Closure> methods;

  //LoxClass(String)
  public LoxClass(String name) {
    this.name = name;

    methods = new HashMap<>();
  }

  //name()
  public String name() {
    return name;
  }

  //setName(String)
  public void setName(String name) {
    this.name = name;
  }

  //methods()
  public Map<String, Closure> methods() {
    return methods;
  }

  //inheritMethods(Map<String, Closure>)
  public void inheritMethods(Map<String, Closure> methods) {
    this.methods = new HashMap<String, Closure>(methods);
  }

  //toString()
  @Override
  public String toString() {
    return name;
  }
}
