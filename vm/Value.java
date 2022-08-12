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
  private Object value;

  //Value()
  public Value() {
    type = ValueType.VAL_NIL;
  }

  //Value(boolean)
  public Value(boolean value) {
    this.value = value;

    type = ValueType.VAL_BOOL;
  }

  //Value(double)
  public Value(double value) {
    this.value = value;

    type = ValueType.VAL_NUMBER;
  }

  //Value(Object)
  public Value(ValueType type, Object value) {
    this.type = type;
    this.value = value;
  }

  //Value(String)
  public Value(String value) {
    this.value = value;

    type = ValueType.VAL_STRING;
  }

  //type()
  public ValueType type() {
    return type;
  }

  //asBool()
  public boolean asBool() {
    return (boolean)value;
  }

  //asBoundMethod()
  public BoundMethod asBoundMethod() {
    return (BoundMethod)value;
  }

  //asClosure()
  public Closure asClosure() {
    return (Closure)value;
  }

  //asFunction()
  public Function asFunction() {
    return (Function)value;
  }

  //asLoxClass()
  public LoxClass asLoxClass() {
    return (LoxClass)value;
  }

  //asLoxInstance()
  public LoxInstance asLoxInstance() {
    return (LoxInstance)value;
  }

  //asNativeFn()
  public NativeFn asNativeFn() {
    return (NativeFn)value;
  }

  //asNumber()
  public double asNumber() {
    return (double)value;
  }

  //asString()
  public String asString() {
    return (String)value;
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
  public boolean equals(Value other) {
    if (type != other.type())
      return false;

    switch(type) {
      case VAL_BOOL:
        return asBool() == other.asBool();
      case VAL_NIL:
        return true;
      case VAL_NUMBER:
        return asNumber() == other.asNumber();
      case VAL_STRING:
        return asString().equals(other.asString());
      default:
        return other == this;
    }
  }

  //toString()
  @Override
  public String toString() {
    if (type == ValueType.VAL_BOOL)
      return String.valueOf(value);
    if (type == ValueType.VAL_NIL)
      return "nil";
    else if (type == ValueType.VAL_NUMBER)
      return String.format("%s",value);
    else if (type == ValueType.VAL_STRING)
      return (String)value;
    else
      return value.toString();
  }
}
