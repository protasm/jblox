package jblox.compiler;

import jblox.scanner.Token;

public class Local {
  private Token token;
  private int depth;
  private boolean isCaptured;

  //Local()
  Local() {
    this(null, -1);
  }

  //Local(Token, int)
  Local(Token token, int depth) {
    this.token = token;
    this.depth = depth;

    isCaptured = false;
  }

  //token()
  public Token token() {
    return token;
  }

  //depth()
  public int depth() {
    return depth;
  }

  //setDepth(int)
  public void setDepth(int depth) {
    this.depth = depth;
  }

  //isCaptured()
  public boolean isCaptured() {
    return isCaptured;
  }

  //setIsCaptured(boolean)
  public void setIsCaptured(boolean isCaptured) {
    this.isCaptured = isCaptured;
  }
}
