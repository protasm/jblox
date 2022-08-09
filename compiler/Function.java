package jblox.compiler;

public class Function {
  private String name;
  private int arity;
  private int upvalueCount;
  private Chunk chunk;

  //Function(String)
  public Function(String name) {
    this.name = name;

    arity = 0;
    upvalueCount = 0;
    chunk = new Chunk();
  }

  //name()
  public String name() {
    return name;
  }

  //arity()
  public int arity() {
    return arity;
  }

  //setArity(int)
  public void setArity(int arity) {
    this.arity = arity;
  }

  //upvalueCount()
  public int upvalueCount() {
    return upvalueCount;
  }

  //incrementUpvalueCount()
  public int incrementUpvalueCount() {
    return ++upvalueCount;
  }

  //chunk()
  public Chunk chunk() {
    return chunk;
  }

  //toString()
  @Override
  public String toString() {
    if (name == null)
      return "<script>";
    else
      return "<fn " + name + ">";
  }
}
