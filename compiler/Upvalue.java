package compiler;

public class Upvalue {
  private byte index;
  private boolean isLocal;

  //Upvalue(byte, boolean)
  public Upvalue(byte index, boolean isLocal) {
    this.index = index;
    this.isLocal = isLocal;
  }

  //index()
  public byte index() {
    return index;
  }

  //isLocal()
  public boolean isLocal() {
    return isLocal;
  }
}
