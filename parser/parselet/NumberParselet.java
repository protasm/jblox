package parser.parselet;

import compiler.Compiler;

public class NumberParselet implements Parselet {
  //parser(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    Object value = compiler.parser().previous().literal();

    compiler.emitConstant(value);
  }
}
