package parser;

import parser.parselet.Parselet;

public class ParseRule {
  private Parselet prefix;
  private Parselet infix;
  private int precedence;

  //ParseRule(Parselet, Parselet, int)
  public ParseRule(Parselet prefix, Parselet infix, int precedence) {
    this.prefix = prefix;
    this.infix = infix;
    this.precedence = precedence;
  }

  //prefix()
  public Parselet prefix() {
    return prefix;
  }

  //infix()
  public Parselet infix() {
    return infix;
  }

  //precedence()
  public int precedence() {
    return precedence;
  }
}
