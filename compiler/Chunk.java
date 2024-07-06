package compiler;

public class Chunk {
  private byte[] codes;
  private int codesCapacity;
  private int codesCount;
  private Object[] constants;
  private int constantsCapacity;
  private int constantsCount;
  private int[] lines;

  //Chunk()
  Chunk() {
    codes = new byte[0];
    constants = new Object[0];
    lines = new int[0];

    codesCapacity = 0;
    codesCount = 0;

    constantsCapacity = 0;
    constantsCount = 0;
  }

  //codes()
  public byte[] codes() {
    return codes;
  }

  //codesCount()
  public int codesCount() {
    return codesCount;
  }

  //constants()
  public Object[] constants() {
    return constants;
  }

  //lines()
  public int[] lines() {
    return lines;
  }

  //writeCode(byte, int)
  void writeCode(byte code, int line) {
    if (codesCapacity < codesCount + 1) {
      //grow capacity
      codesCapacity = codesCapacity < 8 ? 8 : codesCapacity * 2;

      byte[] newCodes = new byte[codesCapacity];
      int[] newLines = new int[codesCapacity];

      //copy elements into new, larger array
      System.arraycopy(codes, 0, newCodes, 0, codesCount);
      System.arraycopy(lines, 0, newLines, 0, codesCount);

      codes = newCodes;
      lines = newLines;
    }

    codes[codesCount] = code;
    lines[codesCount] = line;

    codesCount++;
  }

  //writeConstant(Object)
  int writeConstant(Object constant) {
    if (constantsCapacity < constantsCount + 1) {
      //grow capacity
      constantsCapacity = constantsCapacity < 8 ? 8 : constantsCapacity * 2;

      Object[] newConstants = new Object[constantsCapacity];

      //copy elements into new, larger array
      System.arraycopy(constants, 0, newConstants, 0, constantsCount);

      constants = newConstants;
    }

    constants[constantsCount] = constant;

    constantsCount++;

    //return index of newly written constant
    return constantsCount - 1;
  }
}
