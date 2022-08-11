package jblox.vm;

import jblox.compiler.Function;
import jblox.nativefn.NativeFn;

public class Value {
  public enum ValueType {
    VAL_BOOL,
    VAL_BOUNDMETHOD,
    VAL_CLOSURE,
    VAL_FUNCTION,
    VAL_LOXCLASS,
    VAL_LOXINSTANCE,
    VAL_NATIVEFN,
    VAL_NIL,
    VAL_NUMBER,
    VAL_STRING,
  }

  private ValueType type;
  private boolean boolValue;
  private double numValue;
  private Object objValue;
  private String strValue;

  //Value()
  public Value() {
    type = ValueType.VAL_NIL;
  }

  //Value(boolean)
  public Value(boolean boolValue) {
    this.boolValue = boolValue;

    type = ValueType.VAL_BOOL;
  }

  //Value(double)
  public Value(double numValue) {
    this.numValue = numValue;

    type = ValueType.VAL_NUMBER;
  }

  //Value(Object)
  public Value(ValueType type, Object objValue) {
    this.type = type;
    this.objValue = objValue;
  }

  //Value(String)
  public Value(String strValue) {
    this.strValue = strValue;

    type = ValueType.VAL_STRING;
  }

  //type()
  public ValueType type() {
    return type;
  }

  //asBool()
  public boolean asBool() {
    return boolValue;
  }

  //asBoundMethod()
  public BoundMethod asBoundMethod() {
    return (BoundMethod)objValue;
  }

  //asClosure()
  public Closure asClosure() {
    return (Closure)objValue;
  }

  //asFunction()
  public Function asFunction() {
    return (Function)objValue;
  }

  //asLoxClass()
  public LoxClass asLoxClass() {
    return (LoxClass)objValue;
  }

  //asLoxInstance()
  public LoxInstance asLoxInstance() {
    return (LoxInstance)objValue;
  }

  //asNativeFn()
  public NativeFn asNativeFn() {
    return (NativeFn)objValue;
  }

  //asNumber()
  public double asNumber() {
    return numValue;
  }

  //asString()
  public String asString() {
    return strValue;
  }

  //isBool()
  public boolean isBool() {
    return type == ValueType.VAL_BOOL;
  }

  //isBoundMethod()
  public boolean isBoundMethod() {
    return type == ValueType.VAL_BOUNDMETHOD;
  }

  //isClosure()
  public boolean isClosure() {
    return type == ValueType.VAL_CLOSURE;
  }

  //isFunction()
  public boolean isFunction() {
    return type == ValueType.VAL_FUNCTION;
  }

  //isLoxClass()
  public boolean isLoxClass() {
    return type == ValueType.VAL_LOXCLASS;
  }

  //isLoxInstance()
  public boolean isLoxInstance() {
    return type == ValueType.VAL_LOXINSTANCE;
  }

  //isNativeFn()
  public boolean isNativeFn() {
    return type == ValueType.VAL_NATIVEFN;
  }

  //isNil()
  public boolean isNil() {
    return type == ValueType.VAL_NIL;
  }

  //isNumber()
  public boolean isNumber() {
    return type == ValueType.VAL_NUMBER;
  }

  //isString()
  public boolean isString() {
    return type == ValueType.VAL_STRING;
  }

  //equals()
  public boolean equals(Value otherValue) {
    if (type != otherValue.type())
      return false;

    switch(type) {
      case VAL_BOOL:
        return asBool() == otherValue.asBool();
      case VAL_NIL:
        return true;
      case VAL_NUMBER:
        return asNumber() == otherValue.asNumber();
      case VAL_STRING:
        return asString().equals(otherValue.asString());
      default:
        return otherValue == this;
    }
  }

  //toString()
  @Override
  public String toString() {
    if (type == ValueType.VAL_BOOL)
      return String.valueOf(boolValue);
    else if (type == ValueType.VAL_NUMBER)
      return String.valueOf(numValue);
    else if (type == ValueType.VAL_STRING)
      return strValue;
    else
      return objValue.toString();
  }
}
