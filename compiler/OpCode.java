package compiler;

public final class OpCode {
  public static final byte OP_CONSTANT      = 0x00;
  public static final byte OP_NIL           = 0x01;
  public static final byte OP_TRUE          = 0x02;
  public static final byte OP_FALSE         = 0x03;
  public static final byte OP_POP           = 0x04;
  public static final byte OP_GET_LOCAL     = 0x05;
  public static final byte OP_SET_LOCAL     = 0x06;
  public static final byte OP_GET_GLOBAL    = 0x07;
  public static final byte OP_DEFINE_GLOBAL = 0x08;
  public static final byte OP_SET_GLOBAL    = 0x09;
  public static final byte OP_GET_UPVALUE   = 0x0A;
  public static final byte OP_SET_UPVALUE   = 0x0B;
  public static final byte OP_GET_PROPERTY  = 0x0C;
  public static final byte OP_SET_PROPERTY  = 0x0D;
  public static final byte OP_GET_SUPER     = 0x0E;
  public static final byte OP_EQUAL         = 0x0F;
  public static final byte OP_GREATER       = 0x10;
  public static final byte OP_LESS          = 0x11;
  public static final byte OP_ADD           = 0x12;
  public static final byte OP_SUBTRACT      = 0x13;
  public static final byte OP_MULTIPLY      = 0x14;
  public static final byte OP_DIVIDE        = 0x15;
  public static final byte OP_NOT           = 0x16;
  public static final byte OP_NEGATE        = 0x17;
  public static final byte OP_PRINT         = 0x18;
  public static final byte OP_JUMP          = 0x19;
  public static final byte OP_JUMP_IF_FALSE = 0x1A;
  public static final byte OP_LOOP          = 0x1B;
  public static final byte OP_CALL          = 0x1C;
  public static final byte OP_INVOKE        = 0x1D;
  public static final byte OP_SUPER_INVOKE  = 0x1E;
  public static final byte OP_CLOSURE       = 0x1F;
  public static final byte OP_CLOSE_UPVALUE = 0x20;
  public static final byte OP_RETURN        = 0x21;
  public static final byte OP_CLASS         = 0x22;
  public static final byte OP_INHERIT       = 0x23;
  public static final byte OP_METHOD        = 0x24;

  //OpCode()
  private OpCode() {}
}
