package jblox.parser;

import jblox.scanner.Token;

public class Parser {
  public static final class Precedence {
    public static final int PREC_NONE       = 0;
    public static final int PREC_ASSIGNMENT = 1;  // =
    public static final int PREC_OR         = 2;  // or
    public static final int PREC_AND        = 3;  // and
    public static final int PREC_EQUALITY   = 4;  // == !=
    public static final int PREC_COMPARISON = 5;  // < > <= >=
    public static final int PREC_TERM       = 6;  // + -
    public static final int PREC_FACTOR     = 7;  // * /
    public static final int PREC_UNARY      = 8;  // ! -
    public static final int PREC_CALL       = 9;  // . ()
    public static final int PREC_PRIMARY    = 10;

    //Precedence()
    private Precedence() {}
  }

  private Token previous;
  private Token current;
  private boolean hadError;
  private boolean panicMode;

  //Parser()
  public Parser() {
    previous = null;
    current = null;
    hadError = false;
    panicMode = false;
  }

  //previous()
  public Token previous() {
    return previous;
  }

  //setPrevious(Token)
  public void setPrevious(Token previous) {
    this.previous = previous;
  }

  //current()
  public Token current() {
    return current;
  }

  //setCurrent(Token)
  public void setCurrent(Token current) {
    this.current = current;
  }

  //hadError()
  public boolean hadError() {
    return hadError;
  }

  //setHadError(boolean)
  public void setHadError(boolean hadError) {
    this.hadError = hadError;
  }

  //panicMode()
  public boolean panicMode() {
    return panicMode;
  }

  //setPanicMode(boolean)
  public void setPanicMode(boolean panicMode) {
    this.panicMode = panicMode;
  }
}
