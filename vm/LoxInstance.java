package jblox.vm;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
  private LoxClass klass;
  private Map<String, Object> fields;

  //LoxInstance()
  public LoxInstance(LoxClass klass) {
    this.klass = klass;

    fields = new HashMap<>();
  }

  //klass()
  public LoxClass klass() {
    return klass;
  }

  //fields()
  public Map<String, Object> fields() {
    return fields;
  }

  //toString
  @Override
  public String toString() {
    return klass.name() + " instance";
  }
}
