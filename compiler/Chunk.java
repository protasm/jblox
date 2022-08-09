package jblox.compiler;

import jblox.util.LoxArray;
import jblox.util.LoxByteArray;

public class Chunk {
  private LoxByteArray codes;
  private LoxArray<Object> constants;
  private LoxArray<Integer> lines;

  //Chunk()
  Chunk() {
    codes = new LoxByteArray();
    constants = new LoxArray<>();
    lines = new LoxArray<>();
  }

  //codes()
  public LoxByteArray codes() {
    return codes;
  }

  //constants()
  public LoxArray constants() {
    return constants;
  }

  //lines()
  public LoxArray lines() {
    return lines;
  }

  //writeCode(byte, int)
  void writeCode(byte code, int line) {
    codes.add(code);
    lines.add(line);
  }

  //writeConstant(Object)
  int writeConstant(Object constant) {
    //return index of newly written constant
    return constants.add(constant);
  }
}
