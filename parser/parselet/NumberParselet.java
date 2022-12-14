package jblox.parser.parselet;

import jblox.compiler.Compiler;

public class NumberParselet implements Parselet {
  //parser(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    Object value = compiler.parser().previous().literal();

    compiler.emitConstant(value);
  }
}
