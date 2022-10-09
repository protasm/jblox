package jblox.compiler;

public class CompilerClass {
  private CompilerClass enclosing;
  private boolean hasSuperclass;

  //CompilerClass(CompilerClass, boolean)
  public CompilerClass(CompilerClass enclosing, boolean hasSuperclass) {
    this.enclosing = enclosing;
    this.hasSuperclass = hasSuperclass;
  }

  //enclosing()
  public CompilerClass enclosing() {
    return enclosing;
  }

  //hasSuperclass()
  public boolean hasSuperclass() {
    return hasSuperclass;
  }

  //setHasSuperclass(boolean)
  public void setHasSuperclass(boolean hasSuperclass) {
    this.hasSuperclass = hasSuperclass;
  }
}
