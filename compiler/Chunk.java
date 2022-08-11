package jblox.compiler;

import jblox.util.LoxByteArray;
import jblox.util.LoxIntArray;
import jblox.util.LoxValueArray;
import jblox.vm.Value;

public class Chunk {
  private LoxByteArray codes;
  private LoxValueArray constants;
  private LoxIntArray lines;

  //Chunk()
  Chunk() {
    codes = new LoxByteArray();
    constants = new LoxValueArray();
    lines = new LoxIntArray();
  }

  //codes()
  public LoxByteArray codes() {
    return codes;
  }

  //constants()
  public LoxValueArray constants() {
    return constants;
  }

  //lines()
  public LoxIntArray lines() {
    return lines;
  }

  //writeCode(byte, int)
  void writeCode(byte code, int line) {
    codes.add(code);
    lines.add(line);
  }

  //writeConstant(Value)
  int writeConstant(Value constant) {
    //return index of newly written constant
    return constants.add(constant);
  }
}
