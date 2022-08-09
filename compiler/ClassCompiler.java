package jblox.compiler;

public class ClassCompiler {
  private ClassCompiler enclosing;
  private boolean hasSuperclass;

  //ClassCompiler(ClassCompiler, boolean)
  public ClassCompiler(ClassCompiler enclosing, boolean hasSuperclass) {
    this.enclosing = enclosing;
    this.hasSuperclass = hasSuperclass;
  }

  //enclosing()
  public ClassCompiler enclosing() {
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
