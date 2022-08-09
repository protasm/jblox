package jblox.vm;

import jblox.compiler.Function;

public class CallFrame {
  private Closure closure;
  private int base;
  private int ip;

  //CallFrame(Closure, int)
  CallFrame(Closure closure, int base) {
    this.closure = closure;
    this.base = base;

    ip = 0;
  }

  //closure()
  public Closure closure() {
    return closure;
  }

  //base()
  public int base() {
    return base;
  }

  //getAndIncrementIP()
  public int getAndIncrementIP() {
    return ip++;
  }

  //ip()
  public int ip() {
    return ip;
  }

  //incrementIP()
  public void incrementIP() {
    ip++;
  }

  //setIP(int)
  public void setIP(int ip) {
    this.ip = ip;
  }
}
